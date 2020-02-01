import {
	ManyResourceRelationship,
	ManyResult,
	OneResult,
	Resource
} from './crnk';
import { Authority } from './authority';
import { Module } from './module';

export interface Application extends Resource {
	code?: string;
	name?: string;
	icon?: string;
	url?: string;
	release?: number;
	status?: string;
	comment?: string;
	authorities?: ManyResourceRelationship<Authority>;
	modules?: ManyResourceRelationship<Module>;
}
export interface ApplicationResult extends OneResult {
	data?: Application;
}
export interface ApplicationListResult extends ManyResult {
	data?: Array<Application>;
}
export let createEmptyApplication = (id: string): Application => {
	return {
		'id': id,
		'type': 'Application',
	};
};
