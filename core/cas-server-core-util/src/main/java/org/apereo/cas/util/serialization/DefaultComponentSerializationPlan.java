package org.apereo.cas.util.serialization;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.ComponentSerializationPlan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultComponentSerializationPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultComponentSerializationPlan implements ComponentSerializationPlan {
    private final List<Pair<Class, Integer>> registeredClasses = new ArrayList<>();

    @Override
    public void registerSerializableClass(final Class clazz) {
        registerSerializableClass(clazz, Integer.MAX_VALUE);
    }

    @Override
    public void registerSerializableClass(final Class clazz, final Integer order) {
        this.registeredClasses.add(Pair.of(clazz, order));
    }

    @Override
    public Collection<Class> getRegisteredClasses() {
        return (Collection) this.registeredClasses.stream()
                .sorted(Comparator.comparingInt(Pair::getValue))
                .collect(Collectors.toSet());
    }
}
