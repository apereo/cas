package org.apereo.cas.ticket;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link TicketMetadata}. Ticket metadata describes additional Properties and misc settings
 * that may be associated with a given ticket to be used by registries. Each CAS module on start up
 * has the ability to register/alter ticket metadata that may be requires for its own specific functionality.
 * Given each CAS module may decide to create many forms of tickets, this facility is specifically provided
 * to dynamically register ticket types and associated properties so modules that deal with registry functionality
 * wouldn't have to statically link to all modules and APIs.
 *
 * @author Misagh Moayyed
 * @see TicketMetadataRegistrationPlan
 * @since 5.1.0
 */
public class TicketMetadata {

    private Class<? extends Ticket> implementationClass;

    private String prefix;

    private Map<String, Object> properties = new HashMap<>();

    /**
     * Instantiates a new Ticket metadata.
     *
     * @param implementationClass the implementation class
     * @param prefix              the prefix
     */
    public TicketMetadata(final Class<? extends Ticket> implementationClass, final String prefix) {
        this.implementationClass = implementationClass;
        this.prefix = prefix;
    }

    /**
     * Instantiates a new Ticket metadata.
     *
     * @param implementationClass the implementation class
     * @param prefix              the prefix
     * @param properties          the properties
     */
    public TicketMetadata(final Class<? extends Ticket> implementationClass, final String prefix,
                          final Map<String, Object> properties) {
        this.implementationClass = implementationClass;
        this.prefix = prefix;
        this.properties = properties;
    }

    /**
     * Gets property as string.
     *
     * @param key the key
     * @return the property as string
     */
    public String getPropertyAsString(final String key) {
        return getProperty(key, String.class);
    }

    /**
     * Gets property as number.
     *
     * @param key the key
     * @return the property as number
     */
    public String getPropertyAsNumber(final String key) {
        return getProperty(key, Long.class);
    }

    /**
     * Gets property as boolean.
     *
     * @param key the key
     * @return the property as boolean
     */
    public Boolean getPropertyAsBoolean(final String key) {
        final Boolean b = getProperty(key, Boolean.class);
        return b != null ? b : Boolean.FALSE;
    }

    /**
     * Gets property.
     *
     * @param <T>   the type parameter
     * @param key   the key
     * @param clazz the clazz
     * @return the property
     */
    public <T> T getProperty(final String key, final Class<?> clazz) {
        return (T) clazz.cast(properties.get(key));
    }

    /**
     * Sets property.
     *
     * @param key   the key
     * @param value the value
     */
    public void setProperty(final String key, final Object value) {
        properties.put(key, value);
    }

    /**
     * Gets implementation class.
     *
     * @return the implementation class
     */
    public Class<? extends Ticket> getImplementationClass() {
        return implementationClass;
    }

    /**
     * Gets prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("implementationClass", implementationClass)
                .append("prefix", prefix)
                .toString();
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
        final TicketMetadata rhs = (TicketMetadata) obj;
        return new EqualsBuilder()
                .append(this.implementationClass, rhs.implementationClass)
                .append(this.prefix, rhs.prefix)
                .append(this.properties, rhs.properties)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(implementationClass)
                .append(prefix)
                .append(properties)
                .toHashCode();
    }

    /**
     * Generic property names and constants used for and by various modules
     * to attach metadata to a solid ticket implementation.
     */
    public interface TicketMetadataProperties {
        /**
         * Property name registered in ticket metadata to note tickets (i.e TGTs) must be cascaded on removals.
         */
        String CASCADE_TICKET = "cascadeTicket";

        /**
         * Generic term used to indicate the cache/storage name that would hold onto this ticket.
         */
        String TICKET_CACHE_NAME = "ticketCacheName";
    }
}
