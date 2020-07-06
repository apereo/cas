package org.apereo.cas.configuration.support;

/**
 * This is {@link CasConfigurationPropertiesCategory}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface CasConfigurationPropertiesCategory {

    /**
     * Is defined?
     *
     * @return true/false
     */
    default boolean isDefined() {
//        Arrays.stream(this.getClass().getDeclaredFields());/
        return true;
    }

    /**
     * Is undefined ?.
     *
     * @return true/false
     */
    default boolean isUndefined() {
        return !isDefined();
    }
}
