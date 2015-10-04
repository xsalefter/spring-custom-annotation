package com.baeldung.springcustomannotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import com.baeldung.springcustomannotation.annotation.DataAccess;


public final class DataAccessFieldCallback implements FieldCallback {

    private static final Logger logger = LoggerFactory.getLogger(DataAccessFieldCallback.class);
    private static final int AUTOWIRE_MODE = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

    private ConfigurableListableBeanFactory configurableListableBeanFactory;
    private Object bean;

    public DataAccessFieldCallback(final ConfigurableListableBeanFactory beanFactory, final Object bean) {
        this.configurableListableBeanFactory = beanFactory;
        this.bean = bean;
    }

    @Override
    public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {
        if (!field.isAnnotationPresent(DataAccess.class)) {
            return;
        }
        ReflectionUtils.makeAccessible(field);
        final Type fieldGenericType = field.getGenericType();
        final Class<?> injectableGenericClass = field.getType(); // In this example, get actual "GenericDAO' type.
        final Class<?> entityClassValue = field.getDeclaredAnnotation(DataAccess.class).entity();

        if (genericTypeIsValid(entityClassValue, fieldGenericType)) {
            final String beanName = entityClassValue.getSimpleName() + injectableGenericClass.getSimpleName();
            final Object genericDAOInstance = getBeanInstance(beanName, injectableGenericClass, entityClassValue);
            field.set(this.bean, genericDAOInstance);
        } else {
            final String msg = "@DataAccess(entity) value should have same type with injected generic type.";
            throw new IllegalArgumentException(msg);
        }
    }


    /**
     * For example, if user write:
     * <pre>
     * &#064;DataAccess(entity=Person.class) 
     * private GenericDAO&lt;Account&gt; personGenericDAO;
     * </pre>
     * then this is should be failed.
     */
    public final boolean genericTypeIsValid(final Class<?> entityClassInDataAccessAnnotation, final Type fieldGenericType) {
        if (fieldGenericType instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) fieldGenericType;
            final Type parameterizedFieldGenericType = parameterizedType.getActualTypeArguments()[0];

            return parameterizedFieldGenericType.equals(entityClassInDataAccessAnnotation);
        } else {
            logger.warn("@DataAccess annotation assigned to raw (non-generic) declaration. This will make your code less type-safe.");
            return true;
        }
    }



    public final Object getBeanInstance(final String beanName, final Class<?> injectableGenericClass, final Class<?> parameterizedClass) {
        Object genericDAOInstance = null;
        if (!configurableListableBeanFactory.containsBean(beanName)) {
            logger.info("Will creating new DataAccess bean with name '{}'.", beanName);

            Object toRegister = null;
            try {
                final Constructor<?> constructor = injectableGenericClass.getConstructor(Class.class);
                toRegister = constructor.newInstance(parameterizedClass);
            } catch (Exception e) {
                logger.error("Cannot create instance of type '{}' or instance creation is failed because: {}", injectableGenericClass.getTypeName(), e);
                throw new RuntimeException(e);
            }
            
            genericDAOInstance = configurableListableBeanFactory.initializeBean(toRegister, beanName);
            configurableListableBeanFactory.autowireBeanProperties(genericDAOInstance, AUTOWIRE_MODE, true);
            configurableListableBeanFactory.registerSingleton(beanName, genericDAOInstance);
            logger.info("Bean named '{}' created successfully.", beanName);
        } else {
            genericDAOInstance = configurableListableBeanFactory.getBean(beanName);
            logger.info("Bean named '{}' already exist and would be used as current bean reference.", beanName);
        }
        return genericDAOInstance;
    }
}
