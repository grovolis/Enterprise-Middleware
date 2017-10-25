package org.jboss.quickstarts.wfk.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ConstraintValidatorHelper {

public static <T> T getPropertyValue(Class<T> requiredType, String propertyName, Object instance) {
        if(requiredType == null) {
            throw new IllegalArgumentException("Argument is invalid. Required Type cannot be null");
        }
        if(propertyName == null) {
            throw new IllegalArgumentException("Argument is invalid. Property Name cannot be null");
        }
        if(instance == null) {
            throw new IllegalArgumentException("Argument is invalid. Object Instance cannot be null");
        }
        T returnValue = null;
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor(propertyName, instance.getClass());
            Method readMethod = descriptor.getReadMethod();
            if(readMethod == null) {
                throw new IllegalStateException("Property '" + propertyName + "' of " + instance.getClass().getName() + " is not readable!");
            }
            if(requiredType.isAssignableFrom(readMethod.getReturnType())) {
                try {
                    Object propertyValue = readMethod.invoke(instance);
                    returnValue = requiredType.cast(propertyValue);
                } catch (Exception e) {
                    e.printStackTrace(); // cannot invoke readMethod
                }
            }
        } catch (IntrospectionException e) {
            throw new IllegalArgumentException("Property '" + propertyName + "' is not defined in " + instance.getClass().getName() + "!", e);
        }
        return returnValue; 
    }

    public static boolean isValid(Collection<String> propertyValues, StringComparisonMode comparisonMode) {
        boolean ignoreCase = false;
        switch (comparisonMode) { //comparison mode selection
        case EQUAL_IGNORE_CASE:
        case NOT_EQUAL_IGNORE_CASE:
            ignoreCase = true;
        }

        List<String> values = new ArrayList<String> (propertyValues.size()); //checks property names for upper case
        for (String propertyValue : propertyValues) {
            if(ignoreCase) {
                values.add(propertyValue.toLowerCase());
            } else {
                values.add(propertyValue);
            }
        }

        switch (comparisonMode) {
        case EQUAL:
        case EQUAL_IGNORE_CASE:
            Set<String> uniqueValues = new HashSet<String> (values);
            return uniqueValues.size() == 1 ? true : false;
        case NOT_EQUAL:
        case NOT_EQUAL_IGNORE_CASE:
            Set<String> allValues = new HashSet<String> (values);
            return allValues.size() == values.size() ? true : false;
        }

        return true;
    }

    public static String resolveMessage(String[] propertyNames, StringComparisonMode comparisonMode) {
        StringBuffer buffer = concatPropertyNames(propertyNames);
        buffer.append(" should");
        switch(comparisonMode) {
        case EQUAL:
        case EQUAL_IGNORE_CASE:
            buffer.append(" be equal");
            break;
        case NOT_EQUAL:
        case NOT_EQUAL_IGNORE_CASE:
            buffer.append(" not be equal");
            break;
        }
        buffer.append('.');
        return buffer.toString();
    }

    private static StringBuffer concatPropertyNames(String[] propertyNames) {
        //String concatenation
        StringBuffer buffer = new StringBuffer();
        buffer.append('[');
        for(String propertyName : propertyNames) {
            char firstChar = Character.toUpperCase(propertyName.charAt(0));
            buffer.append(firstChar);
            buffer.append(propertyName.substring(1));
            buffer.append(", ");
        }
        buffer.delete(buffer.length()-2, buffer.length());
        buffer.append("]");
        return buffer;
    }
}