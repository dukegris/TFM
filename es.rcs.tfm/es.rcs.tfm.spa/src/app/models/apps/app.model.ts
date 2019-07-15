import { Menu } from './menu.model';
import { Module } from './module.model';

export interface App {
    id: string;
    name: string;
    description?: string;
    img?: string;
    url: string;
    menu?: Menu[];
    modules?: Module[];
}
