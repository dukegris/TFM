import { Injectable, Output, EventEmitter } from '@angular/core';
import { App } from '../../models/apps/app.model';
import { apps } from '../../../assets/mocks/apps';

@Injectable({
    providedIn: 'root',
})

export class AppsService {

    constructor() {
        this.selectedApp = apps.find(item => item.id === 'search');
    }

    selectedApp: App;

    getApps() {
        return apps;
    }

    getSelectedApp() {
        return this.selectedApp;
    }

}