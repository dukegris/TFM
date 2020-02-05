import { Injectable, Output, EventEmitter } from '@angular/core';

import { BehaviorSubject, Subscription } from 'rxjs';

import { environment } from 'src/environments/environment';

import { CrnkService } from 'src/app/services/connection/crnk.service';
import { CrnkDatasource } from 'src/app/services/connection/crnk.datasource';
import { Application } from 'src/app/resources/application';
import { Module } from 'src/app/resources/module';
import { Function } from 'src/app/resources/function';

import { Menu } from 'src/app/models/security/menu.model';
import { userMenuList } from 'src/assets/mocks/user_menu';

@Injectable({
	providedIn: 'root',
})

export class AppsService {

	private applicationsDS: CrnkDatasource<Application>;
	private applicationsSubscription: Subscription;
	private applicationsBehaviorSubject: BehaviorSubject<Application[]> = new BehaviorSubject<Application[]>(null);
	readonly applications = this.applicationsBehaviorSubject.asObservable();
	private selectedApplicationBehaviorSubject: BehaviorSubject<Application> = new BehaviorSubject<Application>(null);
	readonly selectedApplication = this.selectedApplicationBehaviorSubject.asObservable();

	private modulesDS: CrnkDatasource<Module>;
	private modulesSubscription: Subscription;
	private modulesBehaviorSubject: BehaviorSubject<Module[]> = new BehaviorSubject<Module[]>(null);
	readonly modules = this.modulesBehaviorSubject.asObservable();
	private functionsBehaviorSubject: BehaviorSubject<Function[]> = new BehaviorSubject<Function[]>(null);
	readonly functions = this.functionsBehaviorSubject.asObservable();

	private functionsDS: CrnkDatasource<Function>;
	private functionsSubscription: Subscription;
	private selectedModuleBehaviorSubject: BehaviorSubject<Module> = new BehaviorSubject<Module>(null);
	readonly selectedModule = this.selectedModuleBehaviorSubject.asObservable();
	private selectedFunctionBehaviorSubject: BehaviorSubject<Function> = new BehaviorSubject<Function>(null);
	readonly selectedFunction = this.selectedFunctionBehaviorSubject.asObservable();

	private userMenuListBehaviorSubject: BehaviorSubject<Menu[]> = new BehaviorSubject<Menu[]>(userMenuList);
	readonly userMenuList = this.userMenuListBehaviorSubject.asObservable();

	constructor(
			private service: CrnkService
	) {

		this.applicationsDS = new CrnkDatasource<Application>(
			this.service,
			environment.crnkApiUrl + environment.applicationUrl,
			null,
			null,
			null,
			null);

		this.applicationsSubscription = this.applicationsDS.instances.subscribe(
			data => {
				this.applicationsBehaviorSubject.next(data);
				if (data.length > 0) {
					this.modulesDS.changeURL(data[0].modules.links.related.href);
					this.modulesDS.refresh();
					this.selectedApplicationBehaviorSubject.next(data[0]);
				}
			}
		);

		this.modulesDS = new CrnkDatasource<Module>(
			this.service,
			environment.crnkApiUrl + environment.moduleUrl,
			null,
			null,
			null,
			null);

		this.modulesSubscription = this.modulesDS.instances.subscribe(
			data => {
				this.modulesBehaviorSubject.next(data);
				if (data.length > 0) {
					this.functionsDS.changeURL(data[0].functions.links.related.href);
					this.functionsDS.refresh();
					this.selectedModuleBehaviorSubject.next(data[0]);
				}
			}
		);

		this.functionsDS = new CrnkDatasource<Function>(
			this.service,
			environment.crnkApiUrl + environment.functionUrl,
			null,
			null,
			null,
			null);

		this.functionsSubscription = this.functionsDS.instances.subscribe(
			data => {
				this.functionsBehaviorSubject.next(data);
				if (data.length > 0) {
					this.selectedFunctionBehaviorSubject.next(data[0]);
				}
			}
		);

		this.getApps();

	}

	getApps() {
		this.applicationsDS.refresh();
	}

	getSelectedApp() {

	}

	getModules() {

	}

	getFunctions() {

	}

}
