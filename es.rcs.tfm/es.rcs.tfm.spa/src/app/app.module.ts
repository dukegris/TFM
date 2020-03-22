import { AppRoutingModule } from './app.routing.module';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { FlexLayoutModule } from '@angular/flex-layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { MaterialConfig } from './app.material.config';
import { MatPaginatorIntl } from '@angular/material/paginator';
import { MAT_DATE_FORMATS, MAT_DATE_LOCALE, DateAdapter } from '@angular/material/core';
import { MAT_SNACK_BAR_DEFAULT_OPTIONS } from '@angular/material/snack-bar';
import { MomentDateAdapter } from '@angular/material-moment-adapter';
import { NgModule, ErrorHandler} from '@angular/core';

import { AppsService } from './services/security/apps.service';
import { AuthService } from './services/security/auth.service';
import { ErrorService } from './services/security/error.service';
import { LoggingService } from './services/security/logging.service';

import { AppComponent } from './app.component';
import { AppIndexComponent } from './components/common/index/app.index.component';
import { AppNavComponent } from './components/common/nav/app.nav.component';
import { AppSidenavComponent } from './components/common/sidenav/app.sidenav.component';

import { CrnkService } from './services/connection/crnk.service';
// import { CrnkDatasource } from './services/connection/crnk.datasource';

import { SideBarService } from './services/interface/sidebar.service';
import { SpanishPaginatorService } from './services/interface/spanish.paginator.service';
import { NotificationService } from './services/interface/notification.service';
import { UserLoginComponent } from './components/security/user-login/user-login.component';
import { TokenInterceptor } from './interceptors/token.interceptor';
import { ErrorInterceptor } from './interceptors/error.interceptor';
import { GlobalErrorHandler } from './interceptors/global-error.handler';

export const MY_FORMATS = {
	parse: {
		dateInput: 'DD/MM/YYYY',
	},
	display: {
		dateInput: 'DD/MM/YYYY',
		monthYearLabel: 'MM YYYY',
		dateA11yLabel: 'DD/MM/YYYY',	MatPaginatorIntl,

		monthYearA11yLabel: 'MM YYYY',
	},
};

@NgModule({
	declarations: [
		AppComponent,
		AppIndexComponent,
		AppNavComponent,
		AppSidenavComponent,
		UserLoginComponent
	],
	imports: [
		AppRoutingModule,
		BrowserAnimationsModule,
		BrowserModule,
		DragDropModule,
		FlexLayoutModule.withConfig({
			useColumnBasisZero: false,
			printWithBreakpoints: ['md', 'lt-lg', 'lt-xl', 'gt-sm', 'gt-xs']
		}),
		FormsModule,
		HttpClientModule,
		MaterialConfig,
		ReactiveFormsModule,
	],
	providers: [

		{ provide: MAT_DATE_LOCALE, useValue: 'es-ES' },
		{ provide: MAT_DATE_FORMATS, useValue: MY_FORMATS },
		{ provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: { duration: 2500 } },
		{ provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE] },
		{ provide: MatPaginatorIntl, useClass: SpanishPaginatorService },
		{ provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true },
		{ provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
		{ provide: ErrorHandler, useClass: GlobalErrorHandler },

		AppsService,
		AuthService,
		ErrorService,
		LoggingService,
		NotificationService,

		CrnkService,

		SideBarService,
		SpanishPaginatorService,

	],
	bootstrap: [
		AppComponent
	]
})

export class AppModule { }
