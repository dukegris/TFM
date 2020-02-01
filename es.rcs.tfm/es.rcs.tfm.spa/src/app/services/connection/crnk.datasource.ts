import { CollectionViewer, DataSource, SelectionModel } from '@angular/cdk/collections';

import { of, throwError, Observable, BehaviorSubject, Subscription } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import * as qs from 'qs';

import { environment } from 'src/environments/environment';
import { notEmpty } from 'src/app/utiles';

import { CrnkService } from './crnk.service';
import { PaginationModel } from 'src/app/models/data/pagination.model';
import { FilterModel } from 'src/app/models/data/filter.model';
import { SortModel } from 'src/app/models/data/sort.model';
import {
	Result, OneResult, ManyResult,
	Resource, ResourceError
} from 'src/app/resources/crnk';

export class CrnkDatasource<T extends Resource> implements DataSource<T> {

	private instancesBehaviorSubject: BehaviorSubject<T[]> = new BehaviorSubject<T[]>([]);
	readonly instances = this.instancesBehaviorSubject.asObservable();
	private instancesDS: { instances: T[] } = { instances: ([]) };

	private resultBehaviorSubject: BehaviorSubject<Result> = new BehaviorSubject<Result>(undefined);
	readonly result = this.resultBehaviorSubject.asObservable();
	private resultDS: { result: Result } = { result: (undefined) };

	private instanceBehaviorSubject: BehaviorSubject<T> = new BehaviorSubject<T>(undefined);
	readonly instance = this.instanceBehaviorSubject.asObservable();
	private instanceDS: { instance: T } = { instance: (undefined) };

	private isLoadingBehaviorSubject = new BehaviorSubject<boolean>(false);
	public isLoading = this.isLoadingBehaviorSubject.asObservable();

	selection: SelectionModel<T> = undefined;
	pagination: PaginationModel = undefined;
	sort: SortModel[] = undefined;
	filters: FilterModel[] = undefined;

	constructor(
			private service: CrnkService,
			private baseUrl: string,
			private endpointUrl: string,
			sort: SortModel[],
			filters: FilterModel[],
			pagination: PaginationModel,
			selection: SelectionModel<T> ) {

		this.sort = (sort !== undefined) ?  sort : [];
		this.filters = (filters !== undefined) ?  filters : [];
		this.pagination = (pagination !== undefined) ? pagination : new PaginationModel(1, environment.numRowsInPage);
		this.selection = (selection !== undefined) ?  selection : new SelectionModel<T>(false, []);

	}

	/**
	 * SUSCRIPCION A DATOS
	 */
	private observers: Subscription[] = new Array<Subscription>();
	connect(collectionViewer: CollectionViewer): Observable<T[]> {
		return this.connectInstances(collectionViewer);
	}
	disconnect(collectionViewer: CollectionViewer): void {
		this.disconnectInstances(collectionViewer);
	}
	connectInstances(object: any): Observable<T[]> {
		this.observers.push(object);
		return this.instances;
	}
	disconnectInstances(object: any): void {
		this.observers.splice(this.observers.indexOf(object), 1);
		if (this.observers.length === 0) {
			this.instancesBehaviorSubject.complete();
		}
	}

	/**
	 * INTERFACE SELECTION
	 */
	select(row: any) {
		this.selection.toggle(row);
	}

	/** Whether the number of selected elements matches the total number of rows. */
	isAllSelected() {
		const numSelected = this.selection.selected.length;
		const numRows = this.instancesDS.instances.length;
		return numSelected === numRows;
	}

	/** Selects all rows if they are not all selected; otherwise clear selection. */
	selectAll() {
		this.instancesDS.instances.forEach(row => this.selection.select(row));
	}

	/** Selects all rows if they are not all selected; otherwise clear selection. */
	selectNone() {
		this.selection.clear();
	}

	/** Change the selected elements from . */
	selectTogether() {
		this.isAllSelected()
			? this.selection.clear()
			: this.instancesDS.instances.forEach(row => this.selection.select(row));
	}

	/**
	 * INTERFACE CRUD
	 */

	refresh() {
		const order = SortModel.getParam(this.sort);
		const filters = FilterModel.getParam(this.filters);
		const pagination = PaginationModel.getParam(this.pagination);
		this._findAll(order, filters, pagination);
	}
	save(element: T) {
		this._saveRecord(element);
	}
	delete(element: T) {
		this._deleteRecord(element);
	}

	/**
	 * DATA
	 */
	private _findAll(
			order?: any,
			filter?: any,
			pagination?: any) {

		const params = [];
		if (notEmpty(order)) {
			params.push(order);
		}
		if (notEmpty(filter)) {
			params.push(filter);
		}
		if (notEmpty(pagination)) {
			params.push(pagination);
		}

		const url: string = this._buildUrl(this.baseUrl, this.endpointUrl, undefined, params);
		this.isLoadingBehaviorSubject.next(true);
		this.service
			.findAll(url)
			.pipe(
				catchError((error) =>
					throwError([error])),
				finalize(() =>
					this.isLoadingBehaviorSubject.next(false)))
			.subscribe(
				(result: ManyResult) => {
					this.resultDS.result = result;
					this.resultBehaviorSubject.next(Object.assign({}, this.resultDS).result);
					this.instancesDS.instances = result.data as T[];
					this.instancesBehaviorSubject.next(Object.assign({}, this.instancesDS).instances);
				} );

	}

	private _findRecord(
			id: string,
			params?: any) {

		const url: string = this._buildUrl(this.baseUrl, this.endpointUrl, id, params);
		this.isLoadingBehaviorSubject.next(true);
		this.service
			.findRecord(url)
			.pipe(
				catchError((error) =>
					throwError(error)),
				finalize(() =>
					this.isLoadingBehaviorSubject.next(false)))
			.subscribe(
				(result: OneResult) => {
					this.resultDS.result = result;
					this.instanceDS.instance = result.data as T;
					this.resultBehaviorSubject.next(Object.assign({}, this.resultDS).result);
					this.instanceBehaviorSubject.next(Object.assign({}, this.instanceDS).instance);
				} );

	}

	private _saveRecord(
			data: T,
			params?: any) {

		const url: string = this._buildUrl(this.baseUrl, this.endpointUrl, data.id, params);
		this.isLoadingBehaviorSubject.next(true);
		this.service
			.saveRecord(url, data)
			.pipe(
				catchError((error) =>
					throwError(error)),
				finalize(() =>
					this.isLoadingBehaviorSubject.next(false)))
			.subscribe(
				(result: OneResult) => {
					if (result.errors) {
						this.instanceDS.instance = data;
						this.instanceDS.instance['errors'] = result.errors;
					} else {
						this.instanceDS.instance = result.data as T;
					}
					this.resultDS.result = result;
					this.resultBehaviorSubject.next(Object.assign({}, this.resultDS).result);
					this.instanceBehaviorSubject.next(Object.assign({}, this.instanceDS).instance);
				} );

	}

	private _deleteRecord(
			data: T,
			params?: any)  {

		const url: string = this._buildUrl(this.baseUrl, this.endpointUrl, data.id, params);
		this.isLoadingBehaviorSubject.next(true);
		this.service
			.deleteRecord(url, data)
			.pipe(
				catchError((error) =>
					throwError(error)),
				finalize(() =>
					this.isLoadingBehaviorSubject.next(false)))
			.subscribe(
				(result: T) => {
					this.instanceDS.instance = undefined;
					this.instanceBehaviorSubject.next(Object.assign({}, this.instanceDS).instance);
				} );

	}

	private _buildUrl(
			baseUrl?: string,
			endpointUrl?: string,
			id?: string,
			params?: any): string {

		const queryParams: string = this._toQueryString(params);
		const url: string = [baseUrl, endpointUrl, id].filter((x) => x).join('/');
		return queryParams ? `${url}?${queryParams}` : url;

	}

	private _toQueryString(params: any): string {

		if (!notEmpty(params)) { return ''; }
		const p = []
			.concat(...params) // equivale a flat(1) de es2019
			.reduce((result, item) => {
				const key = Object.keys(item)[0]; // first property: a, b, c
				result[key] = item[key];
				return result;
			}, {});

		return qs.stringify(p, { arrayFormat: 'brackets' });

	}

}

export function isDataSource(value: any): value is CrnkDatasource<any> {
	return value && value instanceof CrnkDatasource && typeof value.connect === 'function';
}
