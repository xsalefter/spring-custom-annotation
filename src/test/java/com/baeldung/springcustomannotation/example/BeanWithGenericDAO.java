package com.baeldung.springcustomannotation.example;

import org.springframework.stereotype.Repository;

import com.baeldung.springcustomannotation.GenericDAO;
import com.baeldung.springcustomannotation.annotation.DataAccess;

@Repository
public class BeanWithGenericDAO {

    @DataAccess(entity=Person.class)
    private GenericDAO<Person> personGenericDAO;

    public BeanWithGenericDAO() {}

    public GenericDAO<Person> getPersonGenericDAO() {
        return personGenericDAO;
    }

}
