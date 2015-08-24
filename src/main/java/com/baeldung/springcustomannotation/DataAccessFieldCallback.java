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

public class DataAccessFieldCallback implements FieldCallback {

    private static Logger logger = LoggerFactory.getLogger(DataAccessFieldCallback.class);

    private ConfigurableListableBeanFactory configurableListableBeanFactory;
    private Object bean;

    public DataAccessFieldCallback(ConfigurableListableBeanFactory beanFactory, Object bean) {
        this.configurableListableBeanFactory = beanFactory;
        this.bean = bean;
    }

    @Override
    public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        if (!field.isAnnotationPresent(DataAccess.class)) {
            return;
        }
        ReflectionUtils.makeAccessible(field);
        final Class<?> entityClassInDataAccessAnnotation = field.getDeclaredAnnotation(DataAccess.class).entity();
        final Class<?> fieldDataType = field.getType(); // Get actual, "GenericDAO' type.
        final Type fieldGenericType = field.getGenericType();

        /**
         * if @DataAccess(entity=Person.class) private GenericDAO<Account> personGenericDAO, 
         * then this is should be failed.
         */
        if (fieldGenericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldGenericType;
            Type parameterizedFieldGenericType = parameterizedType.getActualTypeArguments()[0];

            if (!parameterizedFieldGenericType.equals(entityClassInDataAccessAnnotation)) {
                throw new IllegalStateException("@DataAccess(entity) value should have same type with injected generic type.");
            }
        } else {
            logger.warn("@DataAccess annotation assigned to raw (non-generic) declaration. This will make your code less type-safe.");
        }

        Object beanToRegister = null;
        Constructor<?> constructor;
        try {
            constructor = fieldDataType.getConstructor(Class.class);
            beanToRegister = constructor.newInstance(entityClassInDataAccessAnnotation);
        } catch (Exception e) {
            logger.error(">>> Error: {}", e.toString());
            e.printStackTrace();
        }

        /**
         * In our case, this would be, for example: "PersonGenericDAO". Why?
         * Because we create our instance auto-wire by name. Why auto-wire by 
         * name? Because we need to create different instance of GenericDAO 
         * based on its generic type.
         */
        String beanName = entityClassInDataAccessAnnotation.getSimpleName() + fieldDataType.getSimpleName();
        Object genericDAOInstance = null;
        if (!configurableListableBeanFactory.containsBean(beanName)) {
            logger.info("Will creating DataAccess bean with name {} that exist in class {}", beanName, field.getDeclaringClass());

            int autowireMode = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;
            genericDAOInstance = configurableListableBeanFactory.initializeBean(beanToRegister, beanName);
            configurableListableBeanFactory.autowireBeanProperties(genericDAOInstance, autowireMode, true);
            configurableListableBeanFactory.registerSingleton(beanName, genericDAOInstance);
        } else {
            genericDAOInstance = configurableListableBeanFactory.getBean(beanName);
        }
        boolean isSingleton = configurableListableBeanFactory.isSingleton(beanName);
        logger.info("Bean {} is {}", beanName, isSingleton ? "Singleton" : "Not Singleton");
        field.set(bean, genericDAOInstance);
    }

}
