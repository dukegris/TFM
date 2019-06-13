import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FlexLayoutModule } from '@angular/flex-layout';

import { AppRoutingModule } from './app.routing.module';
import { AppComponent } from './app.component';

import { MaterialConfig } from './app.material.config';

import { AppNavComponent } from './components/app/nav/app.nav.component';
import { AppSidenavComponent } from './components/app/sidenav/app.sidenav.component';

import { SideBarService } from './services/sidebar.service';

@NgModule({
  declarations: [
    AppComponent,
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
    SideBarService
  ],
  bootstrap: [
    AppComponent
  ]
})
export class AppModule { }
