package org.vaadin.natale.util;

import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.vaadin.natale.util.PropertyNameFormatter.toUpperFirstCharacter;

public class ReflectionUtil {

    private static final Logger logger = Logger.getLogger(ReflectionUtil.class);

    /**
     * Recursively parse the input string and returns the according value of propertyName for obj.
     *
     * @param propertyName field of class(property) name to parse. <br>
     *                     For example: 'artistId'; 'artist.id' (nested property name)
     * @param obj          object to get value.
     * @return result of executing getter method for {@code propertyName} under {@code obj}.
     */
    public static Object getPropertyValueByName(String propertyName, Object obj) {

        Class currentClazz = obj.getClass();

        // If there are '.' it means, that there is a "nested property".
        // Extract class, and property recursively until 'propertyName' will be simple.
        if (propertyName.contains(".")) {
            String nextPropertyName = propertyName.substring(propertyName.indexOf(".") + 1);
            Method getter = getGetterMethodByPropertyName(currentClazz, propertyName.substring(0, propertyName.indexOf(".")));

            Object nextObject = invokeGetterMethodForObject(getter, obj);
            return getPropertyValueByName(nextPropertyName, nextObject);
        }

        // Else if there is a simple property, just return the according getter method.
        Method getter = getGetterMethodByPropertyName(currentClazz, toUpperFirstCharacter(propertyName));

        return invokeGetterMethodForObject(getter, obj);
    }

    public static Method getGetterMethodByPropertyName(Class clazz, String propertyName) {
        Method getter = null;
        try {
            getter = clazz.getDeclaredMethod("get" + toUpperFirstCharacter(propertyName));
        } catch (NoSuchMethodException e) {
            logger.error("No getter method founded in class [" + clazz.getSimpleName() + "] found for property [" + propertyName + "]");
        }
        return getter;
    }

    public static Object invokeGetterMethodForObject(Method getter, Object obj) {
		Object value = null;
		try {
			value = getter.invoke(obj);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error("Couldn't invoke method [" + getter.getName() + " - " + getter + "] for object: " + obj);
			e.printStackTrace();
		}
		return value;
	}

	public static <T> T invokeGetterMethodForObject(Method getter, Object obj, Class<T> returnType) {
		T value = null;

		try {
			value = (T) getter.invoke(obj);
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error("Couldn't invoke method [" + getter.getName() + " - " + getter + "] for object: " + obj);
			e.printStackTrace();
		} catch (ClassCastException e) {
			logger.error("Couldn't cast " + value + " to return type - " + returnType);
			e.printStackTrace();
		}

		return value;
	}

    public static Class getPropertyType(Class clazz, String propertyName) {
        if (propertyName.contains(".")) {
            Method getter = getGetterMethodByPropertyName(clazz, propertyName.substring(0, propertyName.indexOf(".")));

            String nextPropertyName = propertyName.substring(propertyName.indexOf(".") + 1);
            Class nextClazz = getter.getReturnType();
            return getPropertyType(nextClazz, nextPropertyName);
        }

        return getGetterMethodByPropertyName(clazz, propertyName).getReturnType();
    }
}
