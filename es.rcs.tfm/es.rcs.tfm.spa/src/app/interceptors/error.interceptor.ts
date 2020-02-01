import { Injectable } from '@angular/core';
import {
	HttpEvent, HttpInterceptor, HttpHandler,
	HttpRequest, HttpResponse, HttpErrorResponse
} from '@angular/common/http';

import { Observable, throwError, of } from 'rxjs';
import { retry, catchError } from 'rxjs/operators';
import { AuthService } from 'src/app/services/security/auth.service';


@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

	constructor(
		private authService: AuthService) {
	}

	intercept(
			request: HttpRequest<any>,
			next: HttpHandler): Observable<HttpEvent<any>> {

		return next
			.handle(request)
			.pipe(
				catchError((error: HttpErrorResponse) => {
					if (Math.floor(error.status / 500) === 1) {
					} else if (error.status / 400) {
					} else {
					}
					return throwError(error);
				}));

	}

}
