import { Component, HostBinding, HostListener, OnInit } from '@angular/core';

import { SideBarService } from '../../../services/sidebar.service';

@Component({
    selector: 'app-sidenav',
    templateUrl: './app.sidenav.component.html',
    styleUrls: ['./app.sidenav.component.css']
})

export class AppSidenavComponent implements OnInit {

    constructor(
        private sideBarService: SideBarService
    ) {
        this.isOpen = true;
    }

    events: string[] = [];

    // @HostBinding('class.is-open')
    isOpen: boolean;

    // @HostListener('click')
    sidenavToggle() {
        this.sideBarService.toggle();
    }

    ngOnInit() {
        this.sideBarService.change.subscribe( isOpen => {
            this.isOpen = isOpen;
        });
    }

}