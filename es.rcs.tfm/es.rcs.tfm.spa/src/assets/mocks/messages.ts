export class Messages {
	/**
	 * SEGURIDAD
	 */
	accountValidationMessages = {
		username: [
			{ type: 'required', message: 'Username is required' },
			{ type: 'minlength', message: 'Username must be at least 4 characters long' },
			{ type: 'maxlength', message: 'Username cannot be more than 48 characters long' },
			{ type: 'pattern', message: 'Your username must contain only numbers and letters' },
			{ type: 'validUsername', message: 'Your username has already been taken' },
		],
		email: [
			{ type: 'required', message: 'Email is required' },
			{ type: 'pattern', message: 'Enter a valid email' },
		],
		password: [
			{ type: 'required', message: 'Password is required' },
			{ type: 'minlength', message: 'Password must be at least 5 characters long' },
			{ type: 'minlength', message: 'Password must be at least 48 characters long' },
			{ type: 'pattern', message: 'Your password must contain at least one uppercase, one lowercase, and one number' },
		],
		confirm_password: [
			{ type: 'required', message: 'Confirm password is required' },
			{ type: 'areEqual', message: 'Password mismatch' },
		],
		terms: [
			{ type: 'pattern', message: 'You must accept terms and conditions' },
		]
	};

}
