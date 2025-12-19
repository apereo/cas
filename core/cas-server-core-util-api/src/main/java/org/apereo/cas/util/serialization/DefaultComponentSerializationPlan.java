package org.apereo.cas.util.serialization;

import module java.base;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This is {@link DefaultComponentSerializationPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */

@Slf4j
public class DefaultComponentSerializationPlan implements ComponentSerializationPlan {

    private final List<Pair<Class, Integer>> registeredClasses = new ArrayList<>();

    @Override
    public void registerSerializableClass(final Class clazz) {
        registerSerializableClass(clazz, Integer.MAX_VALUE);
    }

    @Override
    public void registerSerializableClass(final Class clazz, final Integer order) {
        LOGGER.trace("Registering serializable class [{}] with order [{}]", clazz.getName(), order);
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
