import { Component, HostListener } from '@angular/core';

import { SideBarService } from '../../../services/sidebar.service';

@Component({
    selector: 'app-nav',
    templateUrl: './app.nav.component.html',
    styleUrls: ['./app.nav.component.css']
})

export class AppNavComponent {

    constructor(
        private sideBarService: SideBarService
    ) {}

    // @HostListener('click')
    sidenavToggle() {
        this.sideBarService.toggle();
    }

}