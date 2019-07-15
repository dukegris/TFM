import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FlexLayoutModule } from '@angular/flex-layout';

import { AppRoutingModule } from './app.routing.module';
import { MaterialConfig } from './app.material.config';

import * as $ from 'jquery';

import { AppComponent } from './app.component';
import { AppIndexComponent } from './components/app/index/app.index.component';
import { AppNavComponent } from './components/app/nav/app.nav.component';
import { AppSidenavComponent } from './components/app/sidenav/app.sidenav.component';

import { AppsService } from './services/apps/apps.service';
import { SideBarService } from './services/sidebar/sidebar.service';

@NgModule({
  declarations: [
    AppComponent,
    AppIndexComponent,
    AppNavComponent,
    AppSidenavComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MaterialConfig,
    BrowserAnimationsModule,
    FlexLayoutModule.withConfig({
      useColumnBasisZero: false,
      printWithBreakpoints: ['md', 'lt-lg', 'lt-xl', 'gt-sm', 'gt-xs']
    })
  ],
  providers: [
    AppsService,
    SideBarService
  ],
  bootstrap: [
    AppComponent
  ]
})
export class AppModule { }
