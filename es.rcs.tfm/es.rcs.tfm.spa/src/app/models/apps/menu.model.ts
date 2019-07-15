import { Submenu } from './submenu.model';

export interface Menu {
    id: string;
    name: string;
    img: string;
    url: string;
    submenu?: Submenu[];
}