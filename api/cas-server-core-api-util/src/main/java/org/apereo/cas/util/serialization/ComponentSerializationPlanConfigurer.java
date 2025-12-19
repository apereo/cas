package org.apereo.cas.util.serialization;

import module java.base;
import org.apereo.cas.util.NamedObject;

/**
 * This is {@link ComponentSerializationPlanConfigurer}, to be implemented
 * by modules that wish to register serializable classes into the plan.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface ComponentSerializationPlanConfigurer extends NamedObject {
    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    void configureComponentSerializationPlan(ComponentSerializationPlan plan);
}
