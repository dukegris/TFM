import { FormControl, AbstractControl } from '@angular/forms';

export function RequireMatch(control: AbstractControl) {
	const selection: any = control.value;
	if (typeof selection === 'string') {
		return { incorrect: true };
	}
	return null;
}

export class Validation {

	static shouldMatch(control: AbstractControl) {
		const selection: any = control.value;
		if (typeof selection === 'string') {
			return { notMatch: true };
		}
		return null;
	}

	static shouldBeUnique(control: FormControl) {
		return new Promise((resolve, reject) => {
			setTimeout(() => {
				if (control.value === 'andy') {
					resolve({shouldBeUnique: true});
				} else {
					resolve(null);
				}
			}, 1000);
		});
	}

	static cannotContainSpace(control: FormControl) {
		if (control.value.indexOf(' ') >= 0) {
			return { cannotContainSpace: true };
		}
		return null;
	}

}
