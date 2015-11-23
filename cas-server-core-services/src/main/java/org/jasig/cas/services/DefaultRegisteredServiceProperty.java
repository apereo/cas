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
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The {@link DefaultRegisteredServiceProperty} is responsible for...
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Entity
@Table(name = "RegisteredServiceProperty")
public class DefaultRegisteredServiceProperty implements RegisteredServiceProperty {
    private static final long serialVersionUID = 1349556364689133211L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Lob
    @Column(name = "property_values")
    private Set<String> values = new HashSet<>();

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Set<String> getValues() {
        return values;
    }

    public void setValues(final Set<String> values) {
        this.values = values;
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
                .append(this.id, rhs.id)
                .append(this.values, rhs.values)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(values)
                .toHashCode();
    }
}
