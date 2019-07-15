import { Component, HostListener } from '@angular/core';

import { AppsService } from '../../../services/apps/apps.service';
import { SideBarService } from '../../../services/sidebar/sidebar.service';
import { App } from '../../../models/apps/app.model';

@Component({
    selector: 'app-nav',
    templateUrl: './app.nav.component.html',
    styleUrls: ['./app.nav.component.css']
})

export class AppNavComponent {

    constructor(
        private appsService: AppsService,
        private sideBarService: SideBarService
    ) {
        this.app = appsService.getSelectedApp();
    }

    app: App;

    sidenavToggle() {
        this.sideBarService.toggle();
    }

}