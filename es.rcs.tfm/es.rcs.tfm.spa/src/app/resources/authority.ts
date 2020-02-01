import {
	ManyResourceRelationship,
	ManyResult,
	OneResourceRelationship,
	OneResult,
	Resource
} from './crnk';
import { Application } from './application';
import { Group } from './group';
import { Role } from './role';
import { User } from './user';

export interface Authority extends Resource {
	code?: string;
	name?: string;
	release?: number;
	status?: string;
	comment?: string;
	application?: OneResourceRelationship<Application>;
	groups?: ManyResourceRelationship<Group>;
	roles?: ManyResourceRelationship<Role>;
	users?: ManyResourceRelationship<User>;
}
export interface AuthorityResult extends OneResult {
	data?: Authority;
}
export interface AuthorityListResult extends ManyResult {
	data?: Array<Authority>;
}
export let createEmptyAuthority = (id: string): Authority => {
	return {
		'id': id,
		'type': 'Authority',
	};
};
