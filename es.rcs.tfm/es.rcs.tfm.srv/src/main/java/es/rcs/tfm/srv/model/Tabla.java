package es.rcs.tfm.srv.model;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tabla<T, D> {

	private Map<T, D> data = new HashMap<>();
	private D nullValue;
	private D otherValue;
	
	public Tabla(D nullValue, D otherValue) {
		this.nullValue = nullValue;
		this.otherValue = otherValue;
	};
	
	@SuppressWarnings("unchecked")
	public void put(T key, D value) {
		
		if (key == null) return;
			
		T search = key;
		if (key instanceof String) {
			search = (T) ((String) key).toUpperCase();
		}
		if (data.get(search) == null) {
			data.put(search, value);
		} else {
			System.out.println(
					key.toString() + 
					" ya esta en la lista con valor " + 
					data.get(search).toString() + 
					" y se quiere añadir como: " +
					value.toString());
		}
		
	}
	
	public D get(final T key) {
		return get(key, this.otherValue);
	}
	
	@SuppressWarnings("unchecked")
	public D get(final T key, final D defaultValue) {

		if (key == null)
			return nullValue;

		T search = key;
		if (key instanceof String) {
			search = (T) ((String) key).toUpperCase();
		}
		
		D value = data.get(search);
		if (value == null)
			value = defaultValue;
		return value;
		
	}

	public void put(List<SimpleEntry<T, D>> values) {
		if ((values == null) || (values.isEmpty())) return;
		for (SimpleEntry<T, D> value: values)
			put(value.getKey(), value.getValue());
	}
	
	public boolean containsKey(T key) {
		return data.containsKey(key);
	}
	
}
