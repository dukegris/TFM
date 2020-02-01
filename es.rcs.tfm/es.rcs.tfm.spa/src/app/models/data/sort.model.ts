import { notEmpty } from 'src/app/utiles';

export class SortModel {

	public static OPERATORS = {
		ASC: 'asc',
		DESC: 'desc',
		NONE: 'none'
	};

	text: string;
	field: string;
	order: string = SortModel.OPERATORS.ASC;
	enabled = true;

	constructor(
			text: string,
			field: string,
			order: string  = SortModel.OPERATORS.ASC,
			enabled: boolean = true ) {
		this.text = text;
		this.field = field;
		this.order = order;
		this.enabled = enabled;
	}

	public static getParam(orders: SortModel[]): object {

		if (!notEmpty(orders)) { return {}; }

		const order = orders
			.filter(item => item.enabled && item.order !== SortModel.OPERATORS.NONE)
			.map(item => item.order === SortModel.OPERATORS.DESC ? '-' + item.field : item.field)
			.join();

		const result = {sort: order};
		return result;

	}

}
