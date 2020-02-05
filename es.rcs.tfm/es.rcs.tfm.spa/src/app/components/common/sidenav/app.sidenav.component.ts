import {
	Component,
	OnInit, OnDestroy, OnChanges, AfterViewInit,
	SimpleChanges, ChangeDetectorRef, 
	HostBinding, HostListener } from '@angular/core';
import { MediaMatcher } from '@angular/cdk/layout';
import { AppsService } from 'src/app/services/security/apps.service';
import { SideBarService } from 'src/app/services/interface/sidebar.service';
import { Subscription } from 'rxjs';
import { AuthService } from 'src/app/services/security/auth.service';
import { Module } from 'src/app/resources/module';
import { Iconos } from 'src/assets/mocks/iconos';

declare var $: any;

@Component({
	selector: 'app-sidenav',
	templateUrl: './app.sidenav.component.html',
	styleUrls: ['./app.sidenav.component.css']
})

export class AppSidenavComponent implements OnInit, OnDestroy, OnChanges, AfterViewInit {

	private isAuthenticatedSubscription: Subscription;
	private modulesSubscription: Subscription;
	private mobileQueryListener: () => void;

	public isAuthenticated = false;
	public modules: Module[];
	public mobileQuery: MediaQueryList;
	public isOpen = true;

	public iconos: Iconos = new Iconos();

	constructor(
		changeDetectorRef: ChangeDetectorRef,
		media: MediaMatcher,
		private sideBarService: SideBarService,
		private authService: AuthService,
		private appService: AppsService
	) {
		this.mobileQueryListener = () => changeDetectorRef.detectChanges();
		this.mobileQuery = media.matchMedia('(max-width: 600px)');
		console.log('AppSidenavComponent: constructor OK');
	}

	ngOnInit() {

		this.mobileQuery.addListener(this.mobileQueryListener);
		this.sideBarService.sidebarCurrentToogle.subscribe( (isOpen: boolean) => {
			this.toggle(isOpen); });
		this.isAuthenticatedSubscription = this.authService.isAuthenticated.subscribe(
			data => this.isAuthenticated = data);
		this.modulesSubscription = this.appService.modules.subscribe(
			data => this.modules = data);

		console.log('AppSidenavComponent: ngOnInit OK');

	}

	ngOnDestroy(): void {
		this.mobileQuery.removeListener(this.mobileQueryListener);
		this.isAuthenticatedSubscription.unsubscribe();
		console.log('AppSidenavComponent: ngOnDestroy OK');
	}

	ngAfterViewInit() {
		console.log('AppSidenavComponent: ngAfterViewInit OK');
	}

	ngOnChanges(changes: SimpleChanges): void {
		console.log('AppSidenavComponent: ngOnChanges OK');
	}

	sidenavToggle() {
		this.sideBarService.toggle();
	}

	private toggle(isOpen: boolean) {
		if (this.isAuthenticated) {
			if (isOpen) {
				$( '.mat-drawer-inner-container' ).css( 'width', '100%' );
				$( '.sidenav-sidenav' ).css( 'width', '200px' );
				$( '.sidenav-content' ).css( 'margin-left', '200px' );
				$( '.cardName' ).css( 'display', 'inline' );
			} else {
				$( '.mat-drawer-inner-container' ).css( 'width', '74px' );
				$( '.sidenav-sidenav' ).css( 'width', '74px' );
				$( '.sidenav-content' ).css( 'margin-left', '74px' );
				$( '.cardName' ).css( 'display', 'none' );
			}

		}
	}

}






/*

    <mat-sidenav
            #sidenav
            mode="side"
            fixedTopGap="64"
            [mode]="mobileQuery.matches ? 'over' : 'side'"
            [fixedInViewport]="mobileQuery.matches"
            [(opened)]="isOpen"
            (opened)="events.push('open!')"            disableClose="true"
            class="sidenav-sidenav"

            (closed)="events.push('close!')"
            disableClose="true"
            class="sidenav-sidenav"
            [style.marginTop.px]="mobileQuery.matches ? 64 : 0">

        <mat-nav-list>
            <div *ngFor="let element of app.modules">
                <a      mat-list-item
                        [routerLink]="[isOpen, element.url.toLowerCase()]"
                        routerLinkActive="active-list-item">
                    <button mat-menu-item>
                        <mat-icon class="cardImg">{{element.icon}}</mat-icon>
                        <span class="cardName">{{element.name}}</span>
                    </button>
                </a>
            </div>
        </mat-nav-list>
    </mat-sidenav>

    <mat-sidenav-content>
        <p><button mat-button (click)="sidenavToggle()">sidenav.toggle()</button></p>
        <p>Eventos:</p>
        <div>
            <div *ngFor="let e of events">{{e}}</div>
        </div>
    </mat-sidenav-content>
 */
