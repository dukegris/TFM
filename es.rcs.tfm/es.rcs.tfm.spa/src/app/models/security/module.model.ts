import { Submodule } from './submodule.model';

export interface Module {
	id: string;
	name: string;
	img: string;
	url: string;
	submodule?: Submodule[];
}
