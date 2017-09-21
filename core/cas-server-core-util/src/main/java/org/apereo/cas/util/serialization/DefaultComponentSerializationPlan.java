package org.apereo.cas.util.serialization;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.ComponentSerializationPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultComponentSerializationPlan.class);

    private final List<Pair<Class, Integer>> registeredClasses = new ArrayList<>();

    @Override
    public void registerSerializableClass(final Class clazz) {
        registerSerializableClass(clazz, Integer.MAX_VALUE);
    }

    @Override
    public void registerSerializableClass(final Class clazz, final Integer order) {
        LOGGER.debug("Registering serializable class [{}] with order [{}]", clazz.getName(), order);
        this.registeredClasses.add(Pair.of(clazz, order));
    }

    @Override
    public Collection<Class> getRegisteredClasses() {
        return this.registeredClasses.stream()
                .sorted(Comparator.comparingInt(Pair::getValue))
                .map(Pair::getKey)
                .collect(Collectors.toSet());
    }
}
