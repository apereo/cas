package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.val;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.io.Serial;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The {@link DefaultRegisteredServiceProperty} represents
 * a single property associated with a registered service.
 * Properties are assumed to be a set a String values.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Embeddable
@Table(name = DefaultRegisteredServiceProperty.TABLE_NAME)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DefaultRegisteredServiceProperty implements RegisteredServiceProperty {
    /**
     * JPA table name.
     */
    public static final String TABLE_NAME = "RegexRegisteredServiceProperty";

    @Serial
    private static final long serialVersionUID = 1349556364689133211L;

    @Lob
    @Column(name = "property_values")
    @ExpressionLanguageCapable
    private HashSet<String> values = new HashSet<>();

    public DefaultRegisteredServiceProperty(final String... propertyValues) {
        setValues(Arrays.stream(propertyValues).collect(Collectors.toSet()));
    }

    public DefaultRegisteredServiceProperty(final Collection<String> propertyValues) {
        setValues(new HashSet<>(propertyValues));
    }

    @Override
    public Set<String> getValues() {
        if (this.values == null) {
            this.values = new HashSet<>();
        }
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        return this.values
            .stream()
            .map(resolver::resolve)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Sets values.
     *
     * @param values the values
     */
    public void setValues(final Set<String> values) {
        this.values.clear();
        if (values == null) {
            return;
        }
        this.values.addAll(values);
    }

    @Override
    @JsonIgnore
    public String value() {
        if (values.isEmpty()) {
            return null;
        }
        return SpringExpressionLanguageValueResolver.getInstance().resolve(values.iterator().next());
    }

    @Override
    public boolean contains(final String value) {
        return getValues().contains(value);
    }

    /**
     * Add value.
     *
     * @param value the value
     */
    @CanIgnoreReturnValue
    public RegisteredServiceProperty addValue(final String value) {
        values.add(value);
        return this;
    }

}
