package org.apereo.cas.aup;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;

import lombok.Data;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link AcceptableUsagePolicyStatus}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Data
public class AcceptableUsagePolicyStatus implements Serializable {
    /**
     * The Accepted.
     */
    private final boolean accepted;
    /**
     * The Principal.
     */
    private final Principal principal;

    /**
     * The Properties.
     */
    private final MultiValuedMap<String, Object> properties = new ArrayListValuedHashMap<>();

    /**
     * Factory method. Indicate AUP has been accepted.
     *
     * @param principal the principal
     * @return the acceptable usage policy status
     */
    public static AcceptableUsagePolicyStatus accepted(final Principal principal) {
        return new AcceptableUsagePolicyStatus(true, principal);
    }

    /**
     * Factory method. Indicate AUP has been denied.
     *
     * @param principal the principal
     * @return the acceptable usage policy status
     */
    public static AcceptableUsagePolicyStatus denied(final Principal principal) {
        return new AcceptableUsagePolicyStatus(false, principal);
    }

    /**
     * Sets property.
     *
     * @param name  the name
     * @param value the value
     */
    public void setProperty(final String name, final Object value) {
        this.properties.remove(name);
        addProperty(name, value);
    }

    /**
     * Clear properties.
     */
    public void clearProperties() {
        this.properties.clear();
    }

    /**
     * Add property.
     *
     * @param name  the name
     * @param value the value
     */
    public void addProperty(final String name, final Object value) {
        this.properties.put(name, value);
    }

    /**
     * Gets property.
     *
     * @param name the name
     * @return the property
     */
    public Collection<Object> getProperty(final String name) {
        return this.properties.get(name);
    }

    /**
     * Gets property or default.
     *
     * @param name         the name
     * @param defaultValue the default value
     * @return the property or default
     */
    public Collection<Object> getPropertyOrDefault(final String name, final Object defaultValue) {
        return getPropertyOrDefault(name, CollectionUtils.wrapList(defaultValue));
    }

    /**
     * Gets property or default.
     *
     * @param name          the name
     * @param defaultValues the default values
     * @return the property or default
     */
    public Collection<Object> getPropertyOrDefault(final String name, final Object... defaultValues) {
        if (this.properties.containsKey(name)) {
            return this.properties.get(name);
        }
        return Arrays.stream(defaultValues).collect(Collectors.toList());
    }

    /**
     * Gets property or default.
     *
     * @param name          the name
     * @param defaultValues the default values
     * @return the property or default
     */
    public Collection<Object> getPropertyOrDefault(final String name, final Collection<Object> defaultValues) {
        if (this.properties.containsKey(name)) {
            return this.properties.get(name);
        }
        return defaultValues;
    }
}
