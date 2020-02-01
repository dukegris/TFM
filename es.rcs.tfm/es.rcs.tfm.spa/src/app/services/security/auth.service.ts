import { Injectable } from '@angular/core';
import {
	HttpClient, HttpHeaders,
	HttpRequest,
	HttpResponse, HttpErrorResponse, HttpResponseBase } from '@angular/common/http';

import { of, from, Observable, BehaviorSubject, Subscription, throwError } from 'rxjs';
import { map, catchError, finalize } from 'rxjs/operators';

import { notEmpty } from 'src/app/utiles';

import { environment } from 'src/environments/environment';
import { AccountModel } from 'src/app/models/security/account.model';

@Injectable({
	providedIn: 'root',
})

export class AuthService {

	private static readonly TOKEN: string = 'token';
	private static readonly BEARER: string = 'Bearer ';
	private static readonly BASIC: string = 'Basic ';
	private static readonly AUTHORIZATION: string = 'Authorization';

	private isAuthenticatedBehaviorSubject: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
	readonly isAuthenticated = this.isAuthenticatedBehaviorSubject.asObservable();
	private isAuthenticatedDS: { isAuthenticated: boolean } = { isAuthenticated: (false) };

	private authorizationsBehaviorSubject: BehaviorSubject<string[]> = new BehaviorSubject<string[]>([]);
	readonly authorizations = this.authorizationsBehaviorSubject.asObservable();
	private authorizationsDS: { authorizations: string[] } = { authorizations: ([]) };

	private credentials: AccountModel = new AccountModel();

	private cachedRequests: Array<HttpRequest<any>> = [];

	static getBearerToken(tokenStr: string) {
		let token = null;
		if (notEmpty(tokenStr)) {
			const index = tokenStr.indexOf(AuthService.BEARER);
			if ((index >= 0) && (tokenStr.length >= 7)) {
				token = tokenStr.slice(7);
			}
		}
		return token;
	}

	constructor(
			private http: HttpClient) {
		localStorage.setItem(AuthService.TOKEN, null);
	}

	public login(
		username: string,
		password: string) {

		const requestHeaders: HttpHeaders = new HttpHeaders({
			Authorization : (username && password) ? AuthService.BASIC + btoa(username + ':' + password) : '',
		});

		const requestOptions: object = {
			headers: requestHeaders,
			observe: 'response'
		};

		const body: any = {};

		const observable: Observable<any> = this.http
			.post<object>(environment.accountLoginUrl, body, requestOptions)
			.pipe(
				catchError((error: HttpErrorResponse) =>
					throwError(error))
				);

		const subscription = observable
			.subscribe(
				(result) => {
					let response: HttpResponseBase = null;
					if (result instanceof HttpResponse) {
						response = result as HttpResponse<object>;
					} else if (result instanceof HttpErrorResponse) {
						response = result as HttpErrorResponse;
					}
					this.isAuthenticatedDS.isAuthenticated = false;
					if ((response != null) && (response.status === 200)) {
						const token = AuthService.getBearerToken(response.headers.get(AuthService.AUTHORIZATION));
						if (notEmpty(token)) {

							this.credentials = new AccountModel(username, password);

							this.isAuthenticatedDS.isAuthenticated = true;
							localStorage.setItem(AuthService.TOKEN, token);

							this.me();

						}
					}
					this.isAuthenticatedBehaviorSubject.next(Object.assign({}, this.isAuthenticatedDS).isAuthenticated);
			});

	}

	public me() {

		const requestHeaders: HttpHeaders = new HttpHeaders();

		const requestOptions: object = {
			headers: requestHeaders,
			observe: 'response'
		};

		const observable: Observable<any> = this.http
			.get<object>(environment.accountMeUrl, requestOptions)
			.pipe(
				catchError((error: HttpErrorResponse) =>
					throwError(error))
				);

		const subscription = observable
			.subscribe(
				(result) => {
					let response: HttpResponseBase = null;
					if (result instanceof HttpResponse) {
						response = result as HttpResponse<object>;
					} else if (result instanceof HttpErrorResponse) {
						response = result as HttpErrorResponse;
					}
					this.authorizationsDS.authorizations = [];
					if ((response != null) && (response.status === 200)) {

						this.authorizationsDS.authorizations = response['body']['authorities'];

						this.credentials.authorities = response['body']['authorities'];
						this.credentials.groups = [];
						this.credentials.roles = [];

					} else {
						this.credentials.authorities = [];
						this.credentials.groups = [];
						this.credentials.roles = [];
					}
					this.authorizationsBehaviorSubject.next(Object.assign({}, this.authorizationsDS).authorizations);
				});
	}

	public getAuthToken(): string {
		let token = localStorage.getItem(AuthService.TOKEN);
		if ('null'.localeCompare(token) === 0) { token = null; }
		return token;
	}

	public authenticated(): boolean {
		// get the token
		const token = this.getAuthToken();
		// return a boolean reflecting
		// whether or not the token is expired
		return true; // tokenNotExpired(null, token);
	}

	public collectFailedRequest(request): void {
		this.cachedRequests.push(request);
	}

	public retryFailedRequests(): void {
		// retry the requests. this method can
		// be called after the token is refreshed
	}

}
