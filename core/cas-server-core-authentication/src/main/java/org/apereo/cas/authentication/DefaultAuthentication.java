package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.util.Assert;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Immutable authentication event whose attributes may not change after creation.
 * This class is designed for serialization and is suitable for long-term storage.
 *
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class DefaultAuthentication implements Authentication {

    private static final long serialVersionUID = 3206127526058061391L;

    /**
     * Authentication date stamp.
     */
    private ZonedDateTime authenticationDate;

    /**
     * List of metadata about credentials presented at authentication.
     */
    private List<CredentialMetaData> credentials;

    /**
     * Authenticated principal.
     */
    private final Principal principal;

    /**
     * Authentication metadata attributes.
     */
    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    /**
     * Map of handler name to handler authentication success event.
     */
    private final Map<String, HandlerResult> successes;

    /**
     * Map of handler name to handler authentication failure cause.
     */
    private Map<String, Class<? extends Throwable>> failures;

    /**
     * No-arg constructor for serialization support.
     */
    private DefaultAuthentication() {
        this.authenticationDate = null;
        this.credentials = null;
        this.principal = null;
        this.attributes = null;
        this.successes = null;
        this.failures = null;
    }

    /**
     * Creates a new instance with the given data.
     *
     * @param date       Non-null authentication date.
     * @param principal  Non-null authenticated principal.
     * @param attributes Nullable map of authentication metadata.
     * @param successes  Non-null map of authentication successes containing at least one entry.
     */
    public DefaultAuthentication(
            final ZonedDateTime date,
            final Principal principal,
            final Map<String, Object> attributes,
            final Map<String, HandlerResult> successes) {

        Assert.notNull(date, "Date cannot be null");
        Assert.notNull(principal, "Principal cannot be null");
        Assert.notNull(successes, "Successes cannot be null");
        Assert.notEmpty(successes, "Successes cannot be empty");

        this.authenticationDate = date;
        this.principal = principal;
        this.attributes = attributes;
        this.successes = successes;
        this.credentials = null;
        this.failures = null;
    }

    /**
     * Creates a new instance with the given data.
     *
     * @param date        Non-null authentication date.
     * @param credentials Non-null list of credential metadata containing at least one entry.
     * @param principal   Non-null authenticated principal.
     * @param attributes  Nullable map of authentication metadata.
     * @param successes   Non-null map of authentication successes containing at least one entry.
     * @param failures    Nullable map of authentication failures.
     */
    public DefaultAuthentication(
            final ZonedDateTime date,
            final List<CredentialMetaData> credentials,
            final Principal principal,
            final Map<String, Object> attributes,
            final Map<String, HandlerResult> successes,
            final Map<String, Class<? extends Throwable>> failures) {

        this(date, principal, attributes, successes);

        Assert.notNull(credentials, "Credential cannot be null");
        Assert.notEmpty(credentials, "Credential cannot be empty");

        this.credentials = credentials;
        this.failures = failures.isEmpty() ? new HashMap<>(0) : failures;
    }

    @Override
    public Principal getPrincipal() {
        return this.principal;
    }

    @Override
    public ZonedDateTime getAuthenticationDate() {
        return this.authenticationDate;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return CollectionUtils.wrap(this.attributes);
    }

    @Override
    public List<CredentialMetaData> getCredentials() {
        return CollectionUtils.wrap(this.credentials);
    }

    @Override
    public Map<String, HandlerResult> getSuccesses() {
        return new HashMap<>(this.successes);
    }

    @Override
    public Map<String, Class<? extends Throwable>> getFailures() {
        return CollectionUtils.wrap(this.failures);
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(97, 31);
        builder.append(this.principal);
        builder.append(this.authenticationDate);
        builder.append(this.attributes);
        builder.append(this.credentials);
        builder.append(this.successes);
        builder.append(this.failures);
        return builder.toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Authentication)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        final Authentication other = (Authentication) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.principal, other.getPrincipal());
        builder.append(this.credentials, other.getCredentials());
        builder.append(this.successes, other.getSuccesses());
        builder.append(this.authenticationDate, other.getAuthenticationDate());
        builder.append(CollectionUtils.wrap(this.attributes), other.getAttributes());
        builder.append(CollectionUtils.wrap(this.failures), other.getFailures());
        return builder.isEquals();
    }
    

    @Override
    public void update(final Authentication authn) {
        this.attributes.putAll(authn.getAttributes());
        this.authenticationDate = authn.getAuthenticationDate();
    }

    @Override
    public void updateAll(final Authentication authn) {
        this.attributes.clear();
        update(authn);
    }

    @Override
    public void addAttribute(final String name, final Object value) {
        this.attributes.put(name, value);
    }
}
