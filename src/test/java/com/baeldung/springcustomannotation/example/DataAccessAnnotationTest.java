package com.baeldung.springcustomannotation.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.baeldung.springcustomannotation.CustomAnnotationConfiguration;
import com.baeldung.springcustomannotation.GenericDAO;
import com.baeldung.springcustomannotation.annotation.DataAccess;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CustomAnnotationConfiguration.class })
public class DataAccessAnnotationTest {

    @DataAccess(entity = Person.class)
    private GenericDAO<Person> personGenericDAO;
    @DataAccess(entity = Account.class)
    private GenericDAO<Account> accountGenericDAO;
    @DataAccess(entity = Person.class)
    private GenericDAO<Person> anotherPersonGenericDAO;

    @Test
    public void testThatGenericDAOsIsNotNull() {
        assertThat(personGenericDAO, is(notNullValue()));
        assertThat(accountGenericDAO, is(notNullValue()));
    }

    @Test
    public void testThatGenericDAOIsSingleton() {
        assertThat(this.personGenericDAO, not(sameInstance(this.accountGenericDAO)));
        assertThat(this.personGenericDAO, not(equalTo(this.accountGenericDAO)));

        assertThat(this.personGenericDAO, sameInstance(this.anotherPersonGenericDAO));
    }

    @Test
    public void testThatFindAllReturnExpectedResult() {
        personGenericDAO.findAll();
        assertThat(personGenericDAO.getMessage(), is("Would create findAll query from Person"));

        accountGenericDAO.findAll();
        assertThat(accountGenericDAO.getMessage(), is("Would create findAll query from Account"));
    }

    @Test
    public void testThatPersistMethodReturnExpectedResult() {
        personGenericDAO.persist(new Person());
        assertThat(personGenericDAO.getMessage(), is("Would create persist query from Person"));

        accountGenericDAO.persist(new Account());
        assertThat(accountGenericDAO.getMessage(), is("Would create persist query from Account"));
    }
}
