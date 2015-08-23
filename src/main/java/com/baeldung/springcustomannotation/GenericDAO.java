package com.baeldung.springcustomannotation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GenericDAO<E> {

    private Class<E> entityClass;
    private String message;

    public GenericDAO(final Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    public List<E> findAll() {
        this.message = "Would create findAll query from " + this.entityClass.getSimpleName();
        return Collections.emptyList();
    }

    public Optional<E> persist(E entityClass) {
        this.message = "Would create persist query from " + this.entityClass.getSimpleName();
        return Optional.empty();
    }

    /** Only used for unit-testing. */
    public final String getMessage() {
        return this.message;
    }
}
