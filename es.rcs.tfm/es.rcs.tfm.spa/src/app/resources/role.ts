import {
	ManyResourceRelationship,
	ManyResult,
	OneResourceRelationship,
	OneResult,
	Resource
} from './crnk';
import { Application } from './application';
import { Authority } from './authority';
import { User } from './user';

export interface Role extends Resource {
	code?: string;
	name?: string;
	release?: number;
	status?: string;
	comment?: string;
	application?: OneResourceRelationship<Application>;
	authorities?: ManyResourceRelationship<Authority>;
	users?: ManyResourceRelationship<User>;
}
export interface RoleResult extends OneResult {
	data?: Role;
}
export interface RoleListResult extends ManyResult {
	data?: Array<Role>;
}
export let createEmptyRole = (id: string): Role => {
	return {
		id: id,
		type: 'Role',
	};
};
