package jp.w3ch.examples.bp;

import java.lang.reflect.Field;
import java.util.Map;

public class BlueprintInjections {
	
	public final Map<String, Object> map;

	public BlueprintInjections(Map<String, Object> map) {
		this.map = map;
	}
	
	public void inject(Object object) {
		for (Class<?> cls = object.getClass(); cls != null; cls = cls.getSuperclass()) {
			injectDeclaredFields(cls, object);
		}
	}

	private void injectDeclaredFields(Class<?> cls, Object object) {
		for (Field f : cls.getDeclaredFields()) {
			if ( ! f.isAnnotationPresent(Service.class)) continue;
			
			boolean accessible = f.isAccessible();
			f.setAccessible(true);
			try {
				f.set(object, map.get(f.getName()));
				
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalStateException(e);
			} finally {
				f.setAccessible(accessible);
			}
		}
	}
}
