import {
	ManyResourceRelationship,
	ManyResult,
	OneResourceRelationship,
	OneResult,
	Resource
} from './crnk';
import { Authority } from './authority';
import { User } from './user';

export interface Group extends Resource {
	code?: string;
	name?: string;
	release?: number;
	status?: string;
	comment?: string;
	authorities?: ManyResourceRelationship<Authority>;
	users?: ManyResourceRelationship<User>;
}
export interface GroupResult extends OneResult {
	data?: Group;
}
export interface GroupListResult extends ManyResult {
	data?: Array<Group>;
}
export let createEmptyGroup = (id: string): Group => {
	return {
		'id': id,
		'type': 'Group',
	};
};
