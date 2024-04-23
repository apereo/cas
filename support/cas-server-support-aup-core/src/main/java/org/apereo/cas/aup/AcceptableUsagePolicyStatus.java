package org.apereo.cas.aup;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.util.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Data;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = -5552830592634074877L;

    private final TriStateBoolean status;

    private final Principal principal;

    private final MultiValuedMap<String, Object> properties = new ArrayListValuedHashMap<>(0);

    /**
     * Factory method. Indicate AUP has been accepted.
     *
     * @param principal the principal
     * @return the acceptable usage policy status
     */
    public static AcceptableUsagePolicyStatus accepted(final Principal principal) {
        return new AcceptableUsagePolicyStatus(TriStateBoolean.TRUE, principal);
    }

    /**
     * Factory method. Indicate AUP has been denied.
     *
     * @param principal the principal
     * @return the acceptable usage policy status
     */
    public static AcceptableUsagePolicyStatus denied(final Principal principal) {
        return new AcceptableUsagePolicyStatus(TriStateBoolean.FALSE, principal);
    }

    /**
     * Factory method. Indicate AUP has been skipped with unknown/undefined status.
     *
     * @param principal the principal
     * @return the acceptable usage policy status
     */
    public static AcceptableUsagePolicyStatus skipped(final Principal principal) {
        return new AcceptableUsagePolicyStatus(TriStateBoolean.UNDEFINED, principal);
    }

    /**
     * Sets property.
     *
     * @param name  the name
     * @param value the value
     * @return the property
     */
    @CanIgnoreReturnValue
    public AcceptableUsagePolicyStatus setProperty(final String name, final Object value) {
        this.properties.remove(name);
        addProperty(name, value);
        return this;
    }

    /**
     * Clear properties.
     *
     * @return the acceptable usage policy status
     */
    @CanIgnoreReturnValue
    public AcceptableUsagePolicyStatus clearProperties() {
        this.properties.clear();
        return this;
    }

    /**
     * Add property.
     *
     * @param name  the name
     * @param value the value
     * @return the acceptable usage policy status
     */
    @CanIgnoreReturnValue
    public AcceptableUsagePolicyStatus addProperty(final String name, final Object value) {
        this.properties.put(name, value);
        return this;
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
        return getPropertyOrDefault(name, CollectionUtils.toCollection(defaultValue));
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

    /**
     * Is accepted status.
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isAccepted() {
        return status != null && this.status.isTrue();
    }

    /**
     * Is denied status.
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isDenied() {
        return status != null && this.status.isFalse();
    }
}
