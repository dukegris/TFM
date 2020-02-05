import {
	Component,
	OnInit, OnDestroy, OnChanges, AfterViewInit,
	SimpleChanges, ChangeDetectorRef, HostListener } from '@angular/core';

import { Subscription } from 'rxjs';

import { SideBarService } from 'src/app/services/interface/sidebar.service';
import { AppsService } from 'src/app/services/security/apps.service';
import { AuthService } from 'src/app/services/security/auth.service';
import { Menu } from 'src/app/models/security/menu.model';
import { Application } from 'src/app/resources/application';
import { Module } from 'src/app/resources/module';
import { Iconos } from 'src/assets/mocks/iconos';

@Component({
	selector: 'app-nav',
	templateUrl: './app.nav.component.html',
	styleUrls: ['./app.nav.component.css']
})

export class AppNavComponent implements OnInit, OnDestroy, OnChanges, AfterViewInit {

	private isAuthenticated = false;
	private isAuthenticatedSubscription: Subscription;

	public authorizations: string[] = [];
	private authorizationsSubscription: Subscription;

	public userMenu: Menu[] = [];
	private userMenuSubscription: Subscription;

	public applications: Application[];
	private applicationsSubscription: Subscription;

	public iconos: Iconos = new Iconos();

	constructor(
		private authService: AuthService,
		private appService: AppsService,
		private sideBarService: SideBarService
	) {
		console.log('AppNavComponent: constructor OK');
	}
	
	ngOnInit() {
		//this.appService.getApps();
		this.isAuthenticatedSubscription = this.authService.isAuthenticated.subscribe(
			data => this.isAuthenticated = data);
		this.authorizationsSubscription = this.authService.authorizations.subscribe(
			data => this.authorizations = data);
		this.applicationsSubscription = this.appService.applications.subscribe(
			data => this.applications = data);
		this.userMenuSubscription = this.appService.userMenuList.subscribe(
			data => this.userMenu = data);
		console.log('AppNavComponent: ngOnInit OK');
	}

	ngAfterViewInit() {
		console.log('AppNavComponent: ngAfterViewInit OK');
	}

	ngOnChanges(changes: SimpleChanges): void {
		console.log('AppNavComponent: ngOnChanges OK');
	}

	ngOnDestroy(): void {
		this.isAuthenticatedSubscription.unsubscribe();
		this.authorizationsSubscription.unsubscribe();
		this.applicationsSubscription.unsubscribe();
		this.userMenuSubscription.unsubscribe();
		console.log('AppNavComponent: ngOnDestroy OK');
	}

	isUserMenuPermited(menu: Menu) {
		return (this.isAuthenticated && menu.isAuthenticated) || (!this.isAuthenticated && !menu.isAuthenticated);
	}

	sidenavToggle() {
		this.sideBarService.toggle();
	}

}
