import { Injectable } from '@angular/core';
import {
	HttpClient, HttpHeaders,
	HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { of, throwError, Observable } from 'rxjs';
import { map, catchError, finalize } from 'rxjs/operators';

import {
	Result,
	OneResult, ManyResult,
	Resource, ResourceError
} from 'src/app/resources/crnk';

export class ErrorResponse {

	errors?: ResourceError[] = [];
	constructor(errors ?: ResourceError[]) {
		if (errors) {
			this.errors = errors;
		}
	}

}

@Injectable({
	providedIn: 'root'
})
export class CrnkService {

	private globalHeaders: HttpHeaders;
	private globalRequestOptions: object = {};

	constructor(
		private http: HttpClient) {
	}

	public findAll(
			url: string,
			headers?: HttpHeaders): Observable<ManyResult>  {

		const requestOptions: object = this._buildRequestOptions({ headers, observe: 'response' });
		return this.http
			.get<HttpResponse<object>>(url, requestOptions)
			.pipe(
				map((result: any) =>
					this._extractBody<ManyResult>(result)),
				catchError((error: any) =>
					throwError(this._extractError<ManyResult>(error)))
			);

	}

	public findRecord(
			url: string,
			headers?: HttpHeaders): Observable<OneResult>  {

		const requestOptions: object = this._buildRequestOptions({ headers, observe: 'response' });
		return this.http
			.get<HttpResponse<object>>(url, requestOptions)
			.pipe(
				map((result: any) =>
					this._extractBody<OneResult>(result)),
				catchError((error: any) =>
					throwError(this._extractError<OneResult>(error)))
			);

	}

	public saveRecord<T extends Resource>(
			url: string,
			data: T,
			headers?: HttpHeaders): Observable<OneResult>  {

		const requestOptions: object = this._buildRequestOptions({ headers, observe: 'response' });
		let httpCall: Observable<HttpResponse<object>>;
		const body: any = {
			data: data
		};
		if (data.id) {
			httpCall = this.http.patch<object>(url, body, requestOptions) as Observable<HttpResponse<object>>;
		} else {
			httpCall = this.http.post<object>(url, body, requestOptions) as Observable<HttpResponse<object>>;
		}
		return httpCall
			.pipe(
				map((result: any) =>
					this._extractBody<OneResult>(result)),
				catchError((error: any) =>
					throwError(this._extractError<OneResult>(error)))
			);

	}

	public findAllData<T extends Resource>(
			url: string,
			headers?: HttpHeaders): Observable<T[]>  {

		const requestOptions: object = this._buildRequestOptions({ headers, observe: 'response' });
		return this.http
			.get<HttpResponse<object>>(url, requestOptions)
			.pipe(
				map((result: any) =>
					this._extractBodyData<T[]>(result)),
				catchError((error: any) =>
					throwError(this._extractError<T[]>(error)))
			);

	}

	public findRecordData<T extends Resource>(
			url: string,
			headers?: HttpHeaders): Observable<T>  {

		const requestOptions: object = this._buildRequestOptions({ headers, observe: 'response' });
		return this.http
			.get<HttpResponse<object>>(url, requestOptions)
			.pipe(
				map((result: any) =>
					this._extractBodyData<T>(result)),
				catchError((error: any) =>
					throwError(this._extractError<T>(error)))
			);

	}

	public saveRecordData<T extends Resource>(
			url: string,
			data: T,
			headers?: HttpHeaders): Observable<T>  {

		const requestOptions: object = this._buildRequestOptions({ headers, observe: 'response' });
		let httpCall: Observable<HttpResponse<object>>;
		if (data.id) {
			httpCall = this.http.patch<object>(url, data, requestOptions) as Observable<HttpResponse<object>>;
		} else {
			httpCall = this.http.post<object>(url, data, requestOptions) as Observable<HttpResponse<object>>;
		}
		return httpCall
			.pipe(
				map((result: any) =>
					this._extractBodyData<T>(result)),
				catchError((error: any) =>
					throwError(this._extractError<T>(error)))
			);

	}

	public deleteRecord<T extends Resource>(

			url: string,
			data: T,
			headers?: HttpHeaders)  {

		const requestOptions: object = this._buildRequestOptions({ headers });
		return this.http
			.delete(url, requestOptions);

	}

	private _buildRequestOptions(customOptions: any = {}): object {

		const httpHeaders: HttpHeaders = this._buildHttpHeaders(customOptions.headers);
		const requestOptions: object = Object.assign(
				customOptions, {
				headers: httpHeaders
		});
		return Object.assign(this.globalRequestOptions, requestOptions);

	}

	private _buildHttpHeaders(customHeaders?: HttpHeaders): HttpHeaders {

		/*
		let requestHeaders: HttpHeaders = new HttpHeaders({
				'Accept': 'application/vnd.api+json',
				'Content-Type': 'application/vnd.api+json'
		});
		*/
		let requestHeaders: HttpHeaders = new HttpHeaders({
			Accept: 'application/json',
			'Content-Type': 'application/json'
		});
		if (this.globalHeaders) {
			this.globalHeaders.keys().forEach((key) => {
				if (this.globalHeaders.has(key)) {
					requestHeaders = requestHeaders.set(key, this.globalHeaders.get(key));
				}
			});
		}
		if (customHeaders) {
			customHeaders.keys().forEach((key) => {
				if (customHeaders.has(key)) {
					requestHeaders = requestHeaders.set(key, customHeaders.get(key));
				}
			 });
		}
		return requestHeaders;

	}

	private _extractBody<T>(
			response: HttpResponse<object>): T {

		const body: any = response.body;
		if (!body || body === 'null') {
			throw new Error('No body in response');
		}
		const models: T = body;
		if (!models) {
			if (response.status === 201 || !models) {
				throw new Error('Expected data in response');
			}
		}
		return models;

	}

	private _extractBodyData<T>(
			response: HttpResponse<object>): T {

		const body: any = response.body;
		if (!body || body === 'null') {
			throw new Error('No body in response');
		}
		const models: T = body.data;
		if (!models) {
			if (response.status === 201 || !models) {
				throw new Error('Expected data in response');
			}
		}
		return models;

	}

	private _extractError<T>(
			response: HttpErrorResponse): T {

		const error: any = response.error;
		if (!error || error === 'null') {
			throw new Error('No error in response');
		}
		const models: T = error;
		if (!models) {
			if (response.status === 201 || !models) {
				throw new Error('Expected error in response');
			}
		}
		return models;

	}

}
