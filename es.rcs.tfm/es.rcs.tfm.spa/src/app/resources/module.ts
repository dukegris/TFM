import {
	ManyResourceRelationship,
	ManyResult,
	OneResourceRelationship,
	OneResult,
	Resource
} from './crnk';
import { Application } from './application';
import { Function } from './function';

export interface Module extends Resource {
	code?: string;
	name?: string;
	icon?: string;
	url?: string;
	release?: number;
	status?: string;
	comment?: string;
	application?: OneResourceRelationship<Application>;
	functions?: ManyResourceRelationship<Function>;
}
export interface ModuleResult extends OneResult {
	data?: Module;
}
export interface ModuleListResult extends ManyResult {
	data?: Array<Module>;
}
export let createEmptyModule = (id: string): Module => {
	return {
		'id': id,
		'type': 'Module',
	};
};
