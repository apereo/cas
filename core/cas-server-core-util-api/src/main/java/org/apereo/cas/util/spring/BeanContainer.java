package org.apereo.cas.util.spring;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link BeanContainer}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface BeanContainer<T> {

    /**
     * To list container bean.
     *
     * @return the container bean
     */
    static <T> BeanContainer<T> toList(final T... entries) {
        return new ListBeanContainer<>(Arrays.stream(entries).collect(Collectors.toList()));
    }

    /**
     * Contain items in a list..
     *
     * @param <T>     the type parameter
     * @param entries the entries
     * @return the bean container
     */
    static <T> BeanContainer<T> toList(final List<T> entries) {
        return new ListBeanContainer<>(new ArrayList<>(entries));
    }

    /**
     * Gets items.
     *
     * @return the items
     */
    List<T> get();

    /**
     * And include a single item..
     *
     * @param entry the entry
     * @return the container bean
     */
    BeanContainer<T> and(T entry);

    @RequiredArgsConstructor
    class ListBeanContainer<T> implements BeanContainer<T> {
        private final List<T> items;

        @Override
        public List<T> get() {
            return this.items;
        }

        @Override
        public BeanContainer<T> and(final T entry) {
            items.add(entry);
            return this;
        }
    }
}
