import { notEmpty } from 'src/app/utiles';

export class FilterModel {

	public static OPERATORS = {
		EQ: 'EQ',
		NEQ: 'NEQ',
		LIKE: 'LIKE',
		LT: 'LT',
		LE: 'LE',
		GT: 'GT',
		GE: 'GE',

		OR: 'OR',
		AND: 'AND',

		IN: 'IN',
	};

	public static TYPE = {
		TEXT: 'TEXT',
		CHIP: 'CHIP',
		DATE: 'DATE',
	};

	title: string;
	field: string;
	operator: string = FilterModel.OPERATORS.EQ;
	value: string = undefined;
	type: string = undefined;
	enabled = true;


	constructor(
			title: string,
			field: string,
			operator: string = FilterModel.OPERATORS.EQ,
			type: string = FilterModel.TYPE.TEXT,
			enabled: boolean = true ) {
		this.title = title;
		this.field = field;
		this.operator = operator;
		this.type = type;
		this.enabled = enabled;
	}

	public static getParam(filters: FilterModel[]): any {

		if (!notEmpty(filters)) { return {}; }

		const filter = filters
			.filter(e => e.enabled && notEmpty(e.value) )
			.map(e => {
				const r = {};
				if ( [	FilterModel.OPERATORS.IN,
						FilterModel.OPERATORS.OR,
						FilterModel.OPERATORS.AND].includes (e.operator)) {
					r['filter'] = {};
					// Sin solución en CRNK para OR de campos repetido codigo=BU OR codigo = VA
					// r["filter"][Filter.OPERATORS.OR] = {};
					// r["filter"][Filter.OPERATORS.OR][e.field] = e.value.split(",");
					// { "OR": [ {"id": [12, 13, 14]}, {"completed": "true"} ] }
					// { "AND": [ {"id": [12, 13, 14]}, {"completed": "true"} ] }
				} else {
					r['filter[' + e.field + '][' + e.operator + ']'] = e.value;
				}
				return r;
			});

		return filter;
	}

}
