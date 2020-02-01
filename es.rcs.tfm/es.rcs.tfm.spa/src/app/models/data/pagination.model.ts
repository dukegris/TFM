import { notEmpty } from 'src/app/utiles';

export class PaginationModel {

	number = 1;
	size = 50;

	constructor(
			numero: number = 1,
			size: number = 14) {

		this.number = numero;
		this.size = size;

	}

	public static getParam(p: PaginationModel): any {

		if (!notEmpty(p)) { return {}; }

		const page = Object
			.entries(p)
			.filter(e => e[1] != null)
			.map(e => {
				const r = {};
				r['page[' + e[0] + ']'] = e[1];
				return r; });

		return page;

	}

}
