package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Constructs immutable {@link Authentication} objects using the builder pattern.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@Getter
public class DefaultAuthenticationBuilder implements AuthenticationBuilder {
    private static final int MAP_SIZE = 8;

    private static final long serialVersionUID = -8504842011648432398L;

    private final List<CredentialMetaData> credentials = new ArrayList<>(MAP_SIZE);

    /**
     * Warnings here are considered global and apply
     * to the authentication event vs individual attempts and results.
     */
    private final List<MessageDescriptor> warnings = new ArrayList<>(MAP_SIZE);

    /**
     * Authentication metadata attributes.
     */
    private final Map<String, List<Object>> attributes = new LinkedHashMap<>(MAP_SIZE);

    /**
     * Map of handler names to authentication successes.
     */
    private final Map<String, AuthenticationHandlerExecutionResult> successes = new LinkedHashMap<>(MAP_SIZE);
    /**
     * Map of handler names to authentication failures.
     */
    private final Map<String, Throwable> failures = new LinkedHashMap<>(MAP_SIZE);
    /**
     * Authenticated principal.
     */
    private Principal principal;
    /**
     * Authentication date.
     */
    private ZonedDateTime authenticationDate;

    /**
     * Creates a new instance using the current date for the authentication date.
     */
    public DefaultAuthenticationBuilder() {
        this.authenticationDate = ZonedDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Creates a new instance using the current date for the authentication date and the given
     * principal for the authenticated principal.
     *
     * @param p Authenticated principal.
     */
    public DefaultAuthenticationBuilder(final Principal p) {
        this();
        this.principal = p;
    }

    /**
     * Creates a new builder initialized with data from the given authentication source.
     *
     * @param source Authentication source.
     * @return New builder instance initialized with all fields in the given authentication source.
     */
    public static AuthenticationBuilder newInstance(final Authentication source) {
        val builder = new DefaultAuthenticationBuilder(source.getPrincipal());
        builder.setAuthenticationDate(source.getAuthenticationDate());
        builder.setCredentials(source.getCredentials());
        builder.setSuccesses(source.getSuccesses());
        builder.setFailures(source.getFailures());
        builder.setAttributes(source.getAttributes());
        builder.setWarnings(source.getWarnings());
        return builder;
    }

    /**
     * Creates a new builder.
     *
     * @return New builder instance
     */
    public static AuthenticationBuilder newInstance() {
        return new DefaultAuthenticationBuilder();
    }

    @Override
    public AuthenticationBuilder setWarnings(final List<MessageDescriptor> warning) {
        this.warnings.clear();
        this.warnings.addAll(warning);
        return this;
    }

    @Override
    public AuthenticationBuilder setAuthenticationDate(final ZonedDateTime d) {
        if (d != null) {
            this.authenticationDate = d;
        }
        return this;
    }

    @Override
    public AuthenticationBuilder addWarnings(final List<MessageDescriptor> warning) {
        this.warnings.addAll(warning);
        return this;
    }

    @Override
    public AuthenticationBuilder addWarning(final MessageDescriptor warning) {
        this.warnings.add(warning);
        return this;
    }

    @Override
    public AuthenticationBuilder addCredentials(final List<CredentialMetaData> credentials) {
        this.credentials.addAll(credentials);
        return this;
    }

    @Override
    public AuthenticationBuilder setPrincipal(final Principal p) {
        this.principal = p;
        return this;
    }

    /**
     * Sets the list of metadata about credentials presented for authentication.
     *
     * @param credentials Non-null list of credential metadata.
     * @return This builder instance.
     */
    public AuthenticationBuilder setCredentials(final @NonNull List<CredentialMetaData> credentials) {
        this.credentials.clear();
        this.credentials.addAll(credentials);
        return this;
    }

    @Override
    public AuthenticationBuilder addCredential(final CredentialMetaData credential) {
        this.credentials.add(credential);
        return this;
    }

    @Override
    public AuthenticationBuilder setAttributes(final Map<String, List<Object>> attributes) {
        this.attributes.clear();
        this.attributes.putAll(attributes);
        return this;
    }

    @Override
    public AuthenticationBuilder mergeAttribute(final String key, final Object value) {
        return mergeAttribute(key, CollectionUtils.toCollection(value, ArrayList.class));
    }

    @Override
    public AuthenticationBuilder mergeAttribute(final String key, final List<Object> value) {
        val currentValue = this.attributes.get(key);
        if (currentValue == null) {
            return addAttribute(key, value);
        }
        val collection = CollectionUtils.toCollection(currentValue, ArrayList.class);
        collection.addAll(CollectionUtils.toCollection(value));
        return addAttribute(key, collection);
    }

    @Override
    public boolean hasAttribute(final String name, final Predicate<Object> predicate) {
        if (this.attributes.containsKey(name)) {
            val value = this.attributes.get(name);
            val valueCol = CollectionUtils.toCollection(value);
            return valueCol.stream().anyMatch(predicate);
        }
        return false;
    }

    @Override
    public AuthenticationBuilder addAttribute(final String key, final List<Object> value) {
        this.attributes.put(key, value);
        return this;
    }

    @Override
    public AuthenticationBuilder addAttribute(final String key, final Object value) {
        return addAttribute(key, CollectionUtils.toCollection(value, ArrayList.class));
    }

    @Override
    public AuthenticationBuilder setSuccesses(final @NonNull Map<String, AuthenticationHandlerExecutionResult> successes) {
        this.successes.clear();
        return addSuccesses(successes);
    }

    @Override
    public AuthenticationBuilder addSuccesses(final @NonNull Map<String, AuthenticationHandlerExecutionResult> successes) {
        if (successes != null) {
            successes.forEach(this::addSuccess);
        }
        return this;
    }

    @Override
    public AuthenticationBuilder addSuccess(final String key, final AuthenticationHandlerExecutionResult value) {
        LOGGER.trace("Recording authentication handler result success under key [{}]", key);
        if (this.successes.containsKey(key)) {
            LOGGER.trace("Key mapped to authentication handler result [{}] is already recorded in the list of successful attempts. Overriding...", key);
        }
        this.successes.put(key, value);
        return this;
    }


    @Override
    public AuthenticationBuilder setFailures(final @NonNull Map<String, Throwable> failures) {
        this.failures.clear();
        return addFailures(failures);
    }

    @Override
    public AuthenticationBuilder addFailures(final @NonNull Map<String, Throwable> failures) {
        if (failures != null) {
            failures.forEach(this::addFailure);
        }
        return this;
    }

    @Override
    public AuthenticationBuilder addFailure(final String key, final Throwable value) {
        LOGGER.trace("Recording authentication handler failure under key [{}]", key);
        if (this.successes.containsKey(key)) {
            val newKey = key + System.currentTimeMillis();
            LOGGER.trace("Key mapped to authentication handler failure [{}] is recorded in the list of failed attempts. Overriding with [{}]", key, newKey);
            this.failures.put(newKey, value);
        } else {
            this.failures.put(key, value);
        }
        return this;
    }

    @Override
    public Authentication build() {
        return new DefaultAuthentication(this.authenticationDate, this.credentials, this.principal,
            this.attributes, this.successes, this.failures, this.warnings);
    }

    /**
     * Factory method.
     *
     * @param principal           principal.
     * @param principalFactory    principalFactory.
     * @param principalAttributes principalAttributes.
     * @param service             service.
     * @param registeredService   registeredService.
     * @param authentication      authentication.
     * @return AuthenticationBuilder new AuthenticationBuilder instance.
     */
    public static AuthenticationBuilder of(final Principal principal,
                                           final PrincipalFactory principalFactory,
                                           final Map<String, List<Object>> principalAttributes,
                                           final Service service,
                                           final RegisteredService registeredService,
                                           final Authentication authentication) {

        val principalId = registeredService.getUsernameAttributeProvider().resolveUsername(principal, service, registeredService);
        val newPrincipal = principalFactory.createPrincipal(principalId, principalAttributes);
        return DefaultAuthenticationBuilder.newInstance(authentication).setPrincipal(newPrincipal);
    }
}
