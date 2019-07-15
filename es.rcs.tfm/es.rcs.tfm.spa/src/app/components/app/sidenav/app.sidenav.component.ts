import { Component, OnInit, OnDestroy } from '@angular/core';
import { ChangeDetectorRef, HostBinding, HostListener } from '@angular/core';
import { MediaMatcher } from '@angular/cdk/layout';
import { AppsService } from '../../../services/apps/apps.service';
import { SideBarService } from '../../../services/sidebar/sidebar.service';

declare var $: any;

@Component({
    selector: 'app-sidenav',
    templateUrl: './app.sidenav.component.html',
    styleUrls: ['./app.sidenav.component.css']
})

export class AppSidenavComponent implements OnInit, OnDestroy {

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
            $( '.mat-drawer-inner-container' ).css( 'width', '70px' );
            $( '.mat-sidenav-content' ).css( 'margin-left', '70px' );
            $( '.cardImg' ).css( 'margin-right', '0px' );
            $( '.cardName' ).css( 'display', 'none' );
        } else {
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
