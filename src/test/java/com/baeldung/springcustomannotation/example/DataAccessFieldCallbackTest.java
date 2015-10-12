package com.baeldung.springcustomannotation.example;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.baeldung.springcustomannotation.CustomAnnotationConfiguration;
import com.baeldung.springcustomannotation.DataAccessFieldCallback;
import com.baeldung.springcustomannotation.GenericDAO;

import static org.junit.Assert.*;

import java.lang.reflect.Type;

import static org.hamcrest.CoreMatchers.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CustomAnnotationConfiguration.class })
public class DataAccessFieldCallbackTest {

    @Autowired
    private ConfigurableListableBeanFactory configurableListableBeanFactory;

    @Autowired
    private BeanWithGenericDAO beanWithGenericDAO;

    @Rule
    public ExpectedException ex = ExpectedException.none();

    @Test
    public void whenObjectCreated_thenObjectCreationIsSuccessful() {
        DataAccessFieldCallback dataAccessFieldCallback = new DataAccessFieldCallback(configurableListableBeanFactory, this.beanWithGenericDAO);
        assertThat(dataAccessFieldCallback, is(notNullValue()));
    }

    @Test
    public void whenMethodGenericTypeIsValidCalled_thenReturnCorrectValue() 
    throws NoSuchFieldException, SecurityException {
        DataAccessFieldCallback callback = new DataAccessFieldCallback(configurableListableBeanFactory, this.beanWithGenericDAO);
        Type fieldType = BeanWithGenericDAO.class.getDeclaredField("personGenericDAO").getGenericType();
        boolean result = callback.genericTypeIsValid(Person.class, fieldType);
        assertThat(result, is(true));
    }

    @Test
    public void whenMethodGetBeanInstanceCalled_thenReturnCorrectInstance() {
        DataAccessFieldCallback callback = new DataAccessFieldCallback(configurableListableBeanFactory, this.beanWithGenericDAO);
        Object result = callback.getBeanInstance("personGenericDAO", GenericDAO.class, Person.class);
        assertThat((result instanceof GenericDAO), is(true));
    }
}
