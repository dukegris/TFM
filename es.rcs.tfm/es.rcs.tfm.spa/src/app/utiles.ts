
export let notEmpty = (obj: any) => {
	const empty =
		obj === undefined ||
		obj == null ||
		obj === '' || (
		Object.keys(obj).length === 0 &&
		obj.constructor === Object)
			? true
			: false;
	return !empty;
};
