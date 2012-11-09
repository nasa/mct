package gov.nasa.arc.mct.plot.settings;

import gov.nasa.arc.mct.components.ExtendedProperties;
import gov.nasa.arc.mct.gui.View;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GenericSettings {
	private static final List<Serializer<?>> SERIALIZERS = Arrays.<Serializer<?>>asList(
			new PrimitiveSerializer<Double>(Double.class) {
				@Override
				public Double deserialize(String s) throws Exception {
					return Double.parseDouble(s);
				}				
			},
			new PrimitiveSerializer<String>(String.class) {
				@Override
				public String deserialize(String s) throws Exception {
					return s;
				}				
			},
			new PrimitiveSerializer<Boolean>(Boolean.class) {
				@Override
				public Boolean deserialize(String s) throws Exception {
					return Boolean.parseBoolean(s);
				}				
			},
			new PrimitiveSerializer<Long>(Long.class) {
				@Override
				public Long deserialize(String s) throws Exception {
					return Long.parseLong(s);
				}				
			}
			);
	
	private Map<String, Setting<?>> settingMap = new HashMap<String, Setting<?>>();
	
	protected <T> void create (String name, T defaultValue, Class<T> cls) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Serializer<T> serializer = (cls.isEnum()) ? new EnumSerializer(cls) : findSerializer(cls);
		if (serializer != null) {
			create(name, defaultValue, serializer);
		}
	}
	
	protected <T> void create (String name, T defaultValue, Serializer<T> serializer) {
		if (serializer != null) {
			settingMap.put(name, new Setting<T>(name, defaultValue, serializer));
		}
	}
	
	
	protected boolean allowCreation() { // Don't allow arbitrary settings to be created
		return false;
	}

	@SuppressWarnings("unchecked")
	public <T> boolean set(String name, T value) {
		if (value == null) {
			return false;
		}
		if (value.getClass().isEnum()) {
			return set(name, value, (Class<T>) ((Enum<?>) value).getDeclaringClass());
		} else {
			return set(name, value, (Class<T>) value.getClass());
		}
	}
	
	public <T> boolean set(String name, T value, Class<T> cls) {
		if (allowCreation()) {
			if (!settingMap.containsKey(name)) {
				create(name, null, cls);
			}
		}
		Setting<?> rawSetting = settingMap.get(name);
		if (rawSetting != null && 
				rawSetting.getSettingClass().isAssignableFrom(cls)) {
			@SuppressWarnings("unchecked")
			Setting<T> setting = (Setting<T>) rawSetting;
			setting.setValue(value);
			return true;
		} else {
			return false;
		}
	}
	
	public <T> T get(String name, Class<T> settingClass) {
		Setting<?> rawSetting = settingMap.get(name);
		if (rawSetting != null && 
				rawSetting.getSettingClass().isAssignableFrom(settingClass)) {
			@SuppressWarnings("unchecked")
			Setting<T> setting = (Setting<T>) rawSetting;
			return setting.getValue();
		}
		return null;
	}
	
	public Map<String, String> getPersistableSettings() {
		Map<String, String> persistable = new HashMap<String, String>();
		for (Entry<String, Setting<?>> entry : settingMap.entrySet()) {
			persistable.put(entry.getKey(), entry.getValue().getSerializedValue());
		}
		return persistable;
	}
	
	public void setPersistableSettings(Map<String, String> persisted) {
		for (Entry<String, String> entry : persisted.entrySet()) {
			String name = entry.getKey();
			if (settingMap.containsKey(name)) {
				settingMap.get(name).setSerializedValue(entry.getValue());
			}
		}
	}
	
	public void persist(View view) {
		ExtendedProperties properties = view.getViewProperties();
		for (Entry<String, String> setting : this.getPersistableSettings().entrySet()) {
			properties.setProperty(setting.getKey(), setting.getValue());
		}
		if (view.getManifestedComponent() != null) {
			view.getManifestedComponent().save();
		}
	}
	
	public void loadFrom(View view) {
		ExtendedProperties properties = view.getViewProperties();
		for (Setting<?> setting : settingMap.values()) {
			String property = properties.getProperty(setting.name, String.class);
			if (property != null) {
				setting.setSerializedValue(property);
			}
		}		
	}
	
	@SuppressWarnings("unchecked")
	private <T> Serializer<T> findSerializer(Class<T> cls) {
		for (Serializer<?> serializer : SERIALIZERS) {
			if (serializer.getSerializedClass().isAssignableFrom(cls)) {
				return (Serializer<T>) serializer;
			}
		}
		return null;
	}
	
	private interface Serializer<T> {
		public T deserialize(String s) throws Exception;
		public String serialize(T object);
		public Class<T> getSerializedClass();
	}
	
	private class Setting<T> {
		private Class<T>      cls;
		private String        name;
		private T             value;
		private T             defaultValue;
		private Serializer<T> serializer;

		public Setting(String name, T defaultValue, Serializer<T> serializer) {
			super();
			this.cls = serializer.getSerializedClass();
			this.name = name;
			this.value = defaultValue;
			this.defaultValue = defaultValue;
			this.serializer = serializer;
		}
		
		public T getValue() {
			return value;
		}
		
		public String getSerializedValue() {
			if (serializer != null) {
				return serializer.serialize(value);
			} else {
				return null;
			}			
		}
		
		public void setValue(T value) {
			this.value = value;
		}
		
		public void setSerializedValue(String value) {
			if (value == null) {
				setValue(null);
			} else if (serializer != null) {
				try {
					setValue( serializer.deserialize(value) );					
				} catch (Exception pe) {
					// TODO: Log? Reset to default?
				}
			} else {
				
			}
		}
		
		public Class<T> getSettingClass() {
			return cls;
		}
	}
	
	private static abstract class PrimitiveSerializer<T> implements Serializer<T> {
		private Class<T> serializedClass;

		private PrimitiveSerializer(Class<T> serializedClass) {
			this.serializedClass = serializedClass;
		}

		@Override
		public String serialize(T object) {
			return object.toString();
		}

		/* (non-Javadoc)
		 * @see gov.nasa.arc.mct.fastplot.settings.GenericSettings.Serializer#getSerializedClass()
		 */
		@Override
		public Class<T> getSerializedClass() {
			return serializedClass;
		}
	}
	
	private class EnumSerializer<T extends Enum<T>> extends PrimitiveSerializer<T> {
		private EnumSerializer (Class<T> enumClass) {
			super(enumClass);
		}

		@Override
		public T deserialize(String s) throws Exception {
			return Enum.valueOf(getSerializedClass(), s);			
		}

		/* (non-Javadoc)
		 * @see gov.nasa.arc.mct.fastplot.settings.GenericSettings.PrimitiveSerializer#serialize(java.lang.Object)
		 */
		@Override
		public String serialize(T object) {
			return object.name();
		}
		
		
	}
	
}
