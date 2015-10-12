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
    public void whenGenericDAOInitialized_thenNotNull() {
        assertThat(personGenericDAO, is(notNullValue()));
        assertThat(accountGenericDAO, is(notNullValue()));
    }

    @Test
    public void whenGenericDAOInjected_thenItIsSingleton() {
        assertThat(personGenericDAO, not(sameInstance(accountGenericDAO)));
        assertThat(personGenericDAO, not(equalTo(accountGenericDAO)));

        assertThat(personGenericDAO, sameInstance(anotherPersonGenericDAO));
    }

    @Test
    public void whenFindAll_thenMessagesIsCorrect() {
        personGenericDAO.findAll();
        assertThat(personGenericDAO.getMessage(), is("Would create findAll query from Person"));

        accountGenericDAO.findAll();
        assertThat(accountGenericDAO.getMessage(), is("Would create findAll query from Account"));
    }

    @Test
    public void whenPersist_thenMakeSureThatMessagesIsCorrect() {
        personGenericDAO.persist(new Person());
        assertThat(personGenericDAO.getMessage(), is("Would create persist query from Person"));

        accountGenericDAO.persist(new Account());
        assertThat(accountGenericDAO.getMessage(), is("Would create persist query from Account"));
    }
}
