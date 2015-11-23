package org.jasig.cas.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * The {@link DefaultRegisteredServiceProperty} is responsible for...
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Entity
@Table(name="RegisteredServiceImplProperty")
public class DefaultRegisteredServiceProperty implements RegisteredServiceProperty {
    private static final long serialVersionUID = 1349556364689133211L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Lob
    @Column(name = "property_values")
    private HashSet<String> values = new HashSet<>();

    @Override
    public Set<String> getValues() {
        if (this.values == null) {
            this.values = new HashSet<>();
        }
        return values;
    }
    /**
     * Sets values.
     *
     * @param values the values
     */
    public void setValues(final Set<String> values) {
        getValues().clear();
        if (values == null) {
            return;
        }
        for (final String handler : values) {
            getValues().add(handler);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final DefaultRegisteredServiceProperty rhs = (DefaultRegisteredServiceProperty) obj;
        return new EqualsBuilder()
                .append(this.values, rhs.values)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.values)
                .toHashCode();
    }
}
