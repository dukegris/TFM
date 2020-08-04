export const environment = {
	production: true,
	numRowsInPage: 20,
	apiUrl: window["env"]["apiUrl"] || "default",
	crnkApiUrl:  window["env"]["crnkApiUrl"] || "default",
	accountLoginUrl: '/account/login',
	accountMeUrl: '/account/me',
	applicationUrl: '/applications',
	moduleUrl: '/modules',
	functionUrl: '/functions',
};
