import { Menu } from 'src/app/models/security/menu.model';

export const userMenuList: Menu[] = [
	{
		name : 'Entrar',
		icon : 'account_circle',
		url : 'account/login',
		isAuthenticated: false
	},
	{
		name : 'Configuración',
		icon : 'settings',
		url : 'account/settings' ,
		isAuthenticated: true
	},
	{
		name : 'Seguridad',
		icon : 'security',
		url : 'account/security',
		isAuthenticated: true
	},
	{
		name : '-',
		isAuthenticated: true
	},
	{
		name : 'Entrar con un usuario diferente',
		icon : 'touch_app',
		url : 'account/relogin',
		isAuthenticated: true
	},
	{
		name : '-',
		isAuthenticated: true
	},
	{
		name : 'Salir',
		icon : 'exit_to_app',
		url : 'account/logout',
		isAuthenticated: true
	}
];