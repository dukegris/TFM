import { Injectable } from '@angular/core';
import {
	HttpClient, HttpHeaders,
	HttpRequest,
	HttpResponse, HttpErrorResponse, HttpResponseBase } from '@angular/common/http';

import { of, from, Observable, BehaviorSubject, Subscription, throwError } from 'rxjs';
import { map, catchError, finalize } from 'rxjs/operators';

import * as qs from 'qs';

import { notEmpty } from 'src/app/utiles';

import { environment } from 'src/environments/environment';
import { AccountModel } from 'src/app/models/security/account.model';

@Injectable({
	providedIn: 'root',
})

export class AuthService {

	private static readonly BEARER: string = 'Bearer ';
	private static readonly BASIC: string = 'Basic ';
	private static readonly AUTHORIZATION: string = 'Authorization';

	private static readonly BEARER_TOKEN: string = 'BearerToken';
	private static readonly REFRESH_TOKEN: string = 'RefreshToken';
	private static readonly CREDENTIALS: string = 'UserDetails';

	private globalHeaders: HttpHeaders;
	private globalRequestOptions: object = {};

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
			private service: HttpClient) {
		localStorage.setItem(AuthService.BEARER_TOKEN, null);
	}

	public login(
		username: string,
		password: string) {

		const requestHeaders: HttpHeaders = new HttpHeaders({
			Accept: 'application/json',
			Authorization : (username && password) ? AuthService.BASIC + btoa(username + ':' + password) : '',
		});

		const requestOptions: object = this._buildRequestOptions({
			headers: requestHeaders,
			observe: 'response' });

		const body: any = {};

		this.service
			.post<object>(environment.apiUrl + environment.accountLoginUrl, body, requestOptions)
			.pipe(
				catchError((error: HttpErrorResponse) =>
					throwError(error))
				)
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
							this.isAuthenticatedDS.isAuthenticated = true;
							localStorage.setItem(AuthService.BEARER_TOKEN, token);

							this.credentials = new AccountModel(username, password);
							this.me();

						}
					}
					this.isAuthenticatedBehaviorSubject.next(Object.assign({}, this.isAuthenticatedDS).isAuthenticated);
			});

	}

	public me(
			headers?: HttpHeaders
	) {

		const requestOptions: object = this._buildRequestOptions({ headers, observe: 'response' });

		this.service
			.get<object>(environment.apiUrl + environment.accountMeUrl, requestOptions)
			.pipe(
				catchError((error: HttpErrorResponse) =>
					throwError(error))
				)
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
					localStorage.setItem(AuthService.CREDENTIALS, JSON.stringify(this.credentials));
					this.authorizationsBehaviorSubject.next(Object.assign({}, this.authorizationsDS).authorizations);
				});
	}

	public getAuthToken(): string {
		let token = localStorage.getItem(AuthService.BEARER_TOKEN);
		if ('null'.localeCompare(token) === 0) { token = null; }
		return token;
	}

	public getUserDetails() {
		if (localStorage.getItem(AuthService.CREDENTIALS)) {
			return JSON.parse(sessionStorage.getItem(AuthService.CREDENTIALS));
		} else {
			return null;
		}
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

	private _buildRequestOptions(customOptions: any = {}): object {

		const httpHeaders: HttpHeaders = this._buildHttpHeaders(customOptions.headers);
		const requestOptions: object = Object.assign(
				customOptions, {
				headers: httpHeaders
		});
		return Object.assign(this.globalRequestOptions, requestOptions);

	}

	private _buildHttpHeaders(customHeaders?: HttpHeaders): HttpHeaders {

		/*
		let requestHeaders: HttpHeaders = new HttpHeaders({
				'Accept': 'application/vnd.api+json',
				'Content-Type': 'application/vnd.api+json'
		});
		*/
		let requestHeaders: HttpHeaders = new HttpHeaders({
			Accept: 'application/json',
			'Content-Type': 'application/json'
		});
		if (this.globalHeaders) {
			this.globalHeaders.keys().forEach((key) => {
				if (this.globalHeaders.has(key)) {
					requestHeaders = requestHeaders.set(key, this.globalHeaders.get(key));
				}
			});
		}
		if (customHeaders) {
			customHeaders.keys().forEach((key) => {
				if (customHeaders.has(key)) {
					requestHeaders = requestHeaders.set(key, customHeaders.get(key));
				}
			 });
		}
		return requestHeaders;

	}

}
