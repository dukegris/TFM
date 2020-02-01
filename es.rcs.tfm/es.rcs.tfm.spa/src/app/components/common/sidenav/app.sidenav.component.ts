import { Component, OnInit, OnDestroy } from '@angular/core';
import { ChangeDetectorRef, HostBinding, HostListener } from '@angular/core';
import { MediaMatcher } from '@angular/cdk/layout';
import { AppsService } from 'src/app/services/security/apps.service';
import { SideBarService } from 'src/app/services/interface/sidebar.service';

declare var $: any;

@Component({
	selector: 'app-sidenav',
	templateUrl: './app.sidenav.component.html',
	styleUrls: ['./app.sidenav.component.css']
})

export class AppSidenavComponent implements OnInit, OnDestroy {





/*

    <mat-sidenav
            #sidenav
            mode="side"
            fixedTopGap="64"
            [mode]="mobileQuery.matches ? 'over' : 'side'"
            [fixedInViewport]="mobileQuery.matches"
            [(opened)]="isOpen"
            (opened)="events.push('open!')"
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

	constructor(
		changeDetectorRef: ChangeDetectorRef,
		media: MediaMatcher,
		private appsService: AppsService,
		private sideBarService: SideBarService
	) {
		this.mobileQueryListener = () => changeDetectorRef.detectChanges();
		this.mobileQuery = media.matchMedia('(max-width: 600px)');
		this.app = appsService.getSelectedApp();
	}

	mobileQuery: MediaQueryList;
	private mobileQueryListener: () => void;

	isOpen = true;
	events: string[] = [];
	app;

	sidenavToggle() {
		this.sideBarService.toggle();
	}

	private toggle(isOpen: boolean) {
		if (isOpen) {
			$( '.mat-list-item-content' ).css( 'padding', '16px 0px 16px 0px' );
			$( '.mat-drawer-inner-container' ).css( 'width', '60px' );
			$( '.mat-sidenav-content' ).css( 'margin-left', '70px' );
			$( '.cardImg' ).css( 'margin-right', '0px' );
			$( '.cardName' ).css( 'display', 'none' );
		} else {
			$( '.mat-list-item-content' ).css( 'padding', '16px 0px 16px 0px' );
			$( '.mat-drawer-inner-container' ).css( 'width', '100%' );
			$( '.mat-sidenav-content' ).css( 'margin-left', '170px' );
			$( '.cardImg' ).css( 'margin-right', '16px' );
			$( '.cardName' ).css( 'display', 'inline' );
		}
	}

	ngOnInit() {
		this.mobileQuery.addListener(this.mobileQueryListener);
		this.sideBarService.sidebarCurrentToogle.subscribe( (isOpen: boolean) => {
			this.toggle(isOpen);
		});
	}

	ngOnDestroy(): void {
		this.mobileQuery.removeListener(this.mobileQueryListener);
	  }
}
