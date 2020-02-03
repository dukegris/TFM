import { Injectable } from '@angular/core';
import {
	MatSnackBar, MatSnackBarConfig,
	MatSnackBarRef, MatSnackBarHorizontalPosition,
	MatSnackBarVerticalPosition } from '@angular/material/snack-bar';


@Injectable({
	providedIn: 'root'
})
export class NotificationService {

	snackBarConfig: MatSnackBarConfig;
	snackBarRef: MatSnackBarRef<any>;
	horizontalPosition: MatSnackBarHorizontalPosition = 'right';
	verticalPosition: MatSnackBarVerticalPosition = 'bottom';
	snackBarAutoHide = '1500';

	constructor(
			public snackBar: MatSnackBar) {
		this.snackBarConfig = new MatSnackBarConfig();
		this.snackBarConfig.horizontalPosition = this.horizontalPosition;
		this.snackBarConfig.verticalPosition = this.verticalPosition;
		this.snackBarConfig.duration = parseInt(this.snackBarAutoHide, 0);
	}

	showSuccess(message: string): void {
		this.snackBarConfig.panelClass = 'glam-snackbar';
		this.snackBar.open(message, '', this.snackBarConfig);
	}

	showError(message: string): void {
		// The second parameter is the text in the button.
		// In the third, we send in the css class for the snack bar.
		this.snackBarConfig.panelClass = 'error';
		const snackBarRef = this.snackBar.open(message, 'X', this.snackBarConfig);
		snackBarRef.afterDismissed().subscribe(() => {
			console.log('The snack-bar was dismissed');
		});

		// Button click
		snackBarRef.onAction().subscribe(() => {
			console.log('The snack-bar action was triggered!');
		});

	}

}
