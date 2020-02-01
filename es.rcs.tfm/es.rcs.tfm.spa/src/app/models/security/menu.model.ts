import { Submenu } from './submenu.model';

export interface Menu {
	name: string;
	icon?: string;
	url?: string;
	isAuthenticated: boolean;
	submenu?: Submenu[];
}
