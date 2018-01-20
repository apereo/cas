package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;

import java.time.ZonedDateTime;
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
@Slf4j
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class DefaultAuthentication implements Authentication {

    private static final long serialVersionUID = 3206127526058061391L;

    /**
     * Authentication date stamp.
     */
    private ZonedDateTime authenticationDate;

    /**
     * Authenticated principal.
     */
    private Principal principal;

    /**
     * List of metadata about credentials presented at authentication.
     */
    private List<CredentialMetaData> credentials;

    /**
     * Authentication metadata attributes.
     */
    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    /**
     * Map of handler name to handler authentication success event.
     */
    private Map<String, AuthenticationHandlerExecutionResult> successes;

    /**
     * Map of handler name to handler authentication failure cause.
     */
    private Map<String, Throwable> failures;


    /**
     * Creates a new instance with the given data.
     *
     * @param date       Non-null authentication date.
     * @param principal  Non-null authenticated principal.
     * @param attributes Nullable map of authentication metadata.
     * @param successes  Non-null map of authentication successes containing at least one entry.
     */
    public DefaultAuthentication(
        @NonNull final ZonedDateTime date,
        @NonNull final Principal principal,
        @NonNull final Map<String, Object> attributes,
        @NonNull final Map<String, AuthenticationHandlerExecutionResult> successes) {

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
        @NonNull final ZonedDateTime date,
        @NonNull final List<CredentialMetaData> credentials,
        @NonNull final Principal principal,
        @NonNull final Map<String, Object> attributes,
        @NonNull final Map<String, AuthenticationHandlerExecutionResult> successes,
        @NonNull final Map<String, Throwable> failures) {

        this(date, principal, attributes, successes);
        this.credentials = credentials;
        this.failures = failures;
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
