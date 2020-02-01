import {
	ManyResourceRelationship,
	ManyResult,
	OneResult,
	Resource
} from './crnk';
import { Authority } from './authority';
import { Group } from './group';
import { Role } from './role';

export interface User extends Resource {
	username?: string;
	password?: string;
	passwordExpired?: boolean;
	email?: string;
	emailConfirmed?: boolean;
	enabled?: boolean;
	expired?: boolean;
	locked?: boolean;
	release?: number;
	status?: string;
	comment?: string;
	authorities?: ManyResourceRelationship<Authority>;
	groups?: ManyResourceRelationship<Group>;
	roles?: ManyResourceRelationship<Role>;
}
export interface UserResult extends OneResult {
	data?: User;
}
export interface UserListResult extends ManyResult {
	data?: Array<User>;
}
export let createEmptyUser = (id: string): User => {
	return {
		id: id,
		type: 'User',
	};
};
