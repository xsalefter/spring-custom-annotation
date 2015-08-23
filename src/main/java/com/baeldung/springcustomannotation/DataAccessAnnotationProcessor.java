package com.baeldung.springcustomannotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import com.baeldung.springcustomannotation.annotation.DataAccess;

@Component
public class DataAccessAnnotationProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DataAccessAnnotationProcessor.class);

    private final ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Autowired
    public DataAccessAnnotationProcessor(final ConfigurableListableBeanFactory beanFactory) {
        this.configurableListableBeanFactory = beanFactory;

        logger.info(">>> DataAccessAnnoatationProcessor instance created.");
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        this.scanDataAccessAnnotation(bean, beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    protected void scanDataAccessAnnotation(final Object bean, final String beanName) {
        this.configureFieldInjection(bean);
    }

    private void configureFieldInjection(Object bean) {
        final Class<?> managedBeanClass = bean.getClass();
        ReflectionUtils.doWithFields(managedBeanClass, new FieldCallback() {
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (field.isAnnotationPresent(DataAccess.class)) {
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
                    } catch (NoSuchMethodException | SecurityException | InstantiationException | InvocationTargetException e) {
                        logger.error(">>> Error: {}", e.toString());
                        e.printStackTrace();
                    }

                    /**
                     * In our case, this would be, for example: "PersonGenericDAO". 
                     * Why? Because we create our instance auto-wire by name. 
                     * Why auto-wire by name? Because we need to create different 
                     * instance of GenericDAO based on its generic type.
                     */
                    final String beanName = entityClassInDataAccessAnnotation.getSimpleName() + fieldDataType.getSimpleName();
                    Object genericDAOInstance = null;
                    if (!configurableListableBeanFactory.containsBean(beanName)) {
                        logger.info(">>> Will creating DataAccess bean with name {} that exist in class {}", beanName, field.getDeclaringClass());

                        final int autowireMode = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;
                        genericDAOInstance = configurableListableBeanFactory.initializeBean(beanToRegister, beanName);
                        configurableListableBeanFactory.autowireBeanProperties(genericDAOInstance, autowireMode, true);
                        configurableListableBeanFactory.registerSingleton(beanName, genericDAOInstance);
                    } else {
                        genericDAOInstance = configurableListableBeanFactory.getBean(beanName);
                    }
                    field.set(bean, genericDAOInstance);

                    logger.info("Bean {} is {}", beanName, configurableListableBeanFactory.isSingleton(beanName) ? "Singleton" : "Not Singleton");
                }
            }
        });
    }
}
