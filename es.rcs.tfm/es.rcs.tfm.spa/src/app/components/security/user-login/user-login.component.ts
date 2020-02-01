import {
	Component,
	OnInit, OnDestroy, OnChanges, AfterViewInit,
	Inject,
	SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';
import { FormGroup, FormBuilder, Validators, FormControl, ReactiveFormsModule } from '@angular/forms';

import { Subscription  } from 'rxjs';

import { environment } from 'src/environments/environment';
import { AuthService } from 'src/app/services/security/auth.service';

import { Messages } from 'src/assets/mocks/messages';
import { AccountModel } from 'src/app/models/security/account.model';


@Component({
	selector: 'app-user-login',
	templateUrl: './user-login.component.html',
	styleUrls: ['./user-login.component.css']
})
export class UserLoginComponent implements OnInit, OnDestroy, OnChanges, AfterViewInit {

	private account: AccountModel = new AccountModel();
	public messages: Messages = new Messages();
	public form: FormGroup;
	public icon: string;

	constructor(
		private formBuilder: FormBuilder,
		private service: AuthService,
		private router: Router) {
		this.icon = '/assets/header.png';
	}

	ngOnInit(): void {

		const controls = {
			username:		[this.account.username,	Validators.compose([Validators.maxLength(64), Validators.required])],
			password:		[this.account.password,	Validators.compose([Validators.maxLength(64), Validators.required])],
		};
		this.form = this.formBuilder.group(controls);

		console.log('LoginDialogComponent: ngOnInit');

	}

	ngOnDestroy(): void {
		console.log('LoginDialogComponent: ngOnDestroy');
	}

	ngOnChanges(changes: SimpleChanges): void {
		console.log('LoginDialogComponent: ngOnChanges');
	}

	ngAfterViewInit(): void {
		console.log('LoginDialogComponent: ngAfterViewInit');
	}

	onOk() {

		this.service.login(
			this.form.get('username').value,
			this.form.get('password').value );

		const sus = this.service.isAuthenticated.subscribe(result => {
			if (result) {
				this.router.navigateByUrl('/');
				sus.unsubscribe();
			} else {
				// ERROR
			}
		});
		console.log('LoginDialogComponent: OK');

	}

}
