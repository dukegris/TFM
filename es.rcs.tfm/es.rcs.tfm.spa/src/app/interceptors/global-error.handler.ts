import { Injectable, ErrorHandler, Injector } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { NotificationService } from 'src/app/services/interface/notification.service';
import { ErrorService } from 'src/app/services/security/error.service';
import { LoggingService } from 'src/app/services/security/logging.service';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {

	constructor(
		private errorService: ErrorService,
		private loggerService: LoggingService,
		private notifierService: NotificationService) {
	}

	handleError(error: Error | HttpErrorResponse) {

		let message: string;
		let stackTrace: string;

		if (error instanceof HttpErrorResponse) {
			// Server Error
			message = this.errorService.getServerMessage(error);
			stackTrace = this.errorService.getServerStack(error);
		} else {
			// Client Error
			message = this.errorService.getClientMessage(error);
			stackTrace = this.errorService.getClientStack(error);
		}

		// Always log errors
		this.notifierService.showError(message);
		this.loggerService.logError(message, stackTrace);

	}

}
