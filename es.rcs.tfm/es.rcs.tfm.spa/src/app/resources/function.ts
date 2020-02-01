import {
	ManyResult,
	OneResourceRelationship,
	OneResult,
	Resource
} from './crnk';
import { Module } from './module';

export interface Function extends Resource {
	code?: string;
	name?: string;
	icon?: string;
	url?: string;
	release?: number;
	status?: string;
	comment?: string;
	module?: OneResourceRelationship<Module>;
}
export interface FunctionResult extends OneResult {
	data?: Function;
}
export interface FunctionListResult extends ManyResult {
	data?: Array<Function>;
}
export let createEmptyFunction = (id: string): Function => {
	return {
		'id': id,
		'type': 'Function',
	};
};
