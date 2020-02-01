import { Injectable, Output, EventEmitter } from '@angular/core';
import { BehaviorSubject  } from 'rxjs';

@Injectable({
	providedIn: 'root',
})

export class SideBarService {

	constructor() { }

	isOpen = true;

	private sidebarToogleSource = new BehaviorSubject(true);
	public sidebarCurrentToogle = this.sidebarToogleSource.asObservable();

	toggle() {
		this.isOpen = !this.isOpen;
		this.sidebarToogleSource.next(this.isOpen);
	}

}