export class AccountModel {

	username = '';
	password = '';
	authorities: string[] = [];
	roles: string[] = [];
	groups: string[] = [];

	constructor(
			username: string = null,
			password: string = null) {

		this.username = username;
		this.password = password;

	}

}
