package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceUsernameProviderContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;

/**
 * Constructs immutable {@link Authentication} objects using the builder pattern.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@Getter
@SuppressWarnings("NullAway.Init")
public class DefaultAuthenticationBuilder implements AuthenticationBuilder {
    @Serial
    private static final long serialVersionUID = -8504842011648432398L;

    private final List<Credential> credentials = new ArrayList<>();

    /**
     * Warnings here are considered global and apply
     * to the authentication event vs individual attempts and results.
     */
    private final List<MessageDescriptor> warnings = new ArrayList<>();

    /**
     * Authentication metadata attributes.
     */
    private final Map<String, List<Object>> attributes = new LinkedHashMap<>();

    /**
     * Map of handler names to authentication successes.
     */
    private final Map<String, AuthenticationHandlerExecutionResult> successes = new LinkedHashMap<>();

    /**
     * Map of handler names to authentication failures.
     */
    private final Map<String, Throwable> failures = new LinkedHashMap<>();

    /**
     * Authenticated principal.
     */
    @Nullable
    private Principal principal;

    /**
     * Authentication date.
     */
    private ZonedDateTime authenticationDate;

    public DefaultAuthenticationBuilder() {
        this.authenticationDate = ZonedDateTime.now(ZoneOffset.UTC);
    }

    public DefaultAuthenticationBuilder(final Principal principal) {
        this();
        this.principal = principal;
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

    /**
     * New instance authentication builder with a principal.
     *
     * @param principal the principal
     * @return the authentication builder
     */
    public static AuthenticationBuilder newInstance(final Principal principal) {
        return new DefaultAuthenticationBuilder(principal);
    }

    /**
     * Factory method.
     *
     * @param applicationContext  the application context
     * @param principal           principal.
     * @param principalFactory    principalFactory.
     * @param principalAttributes principalAttributes.
     * @param service             service.
     * @param registeredService   registeredService.
     * @param authentication      authentication.
     * @return AuthenticationBuilder new AuthenticationBuilder instance.
     * @throws Throwable the throwable
     */
    public static AuthenticationBuilder of(final ApplicationContext applicationContext,
                                           final Principal principal,
                                           final PrincipalFactory principalFactory,
                                           final Map<String, List<Object>> principalAttributes,
                                           @Nullable
                                           final Service service,
                                           final RegisteredService registeredService,
                                           final Authentication authentication) throws Throwable {
        val usernameContext = RegisteredServiceUsernameProviderContext.builder()
            .service(service)
            .principal(principal)
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .build();
        val principalId = registeredService.getUsernameAttributeProvider().resolveUsername(usernameContext);
        val newPrincipal = principalFactory.createPrincipal(principalId, principalAttributes);
        return DefaultAuthenticationBuilder.newInstance(authentication).setPrincipal(newPrincipal);
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder setPrincipal(@Nullable final Principal p) {
        this.principal = p;
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder addCredentials(final List<Credential> credentials) {
        this.credentials.addAll(credentials);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder addCredential(final Credential credential) {
        this.credentials.add(credential);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder addWarnings(final List<MessageDescriptor> warning) {
        this.warnings.addAll(warning);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder addWarning(final MessageDescriptor warning) {
        this.warnings.add(warning);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder setWarnings(final List<MessageDescriptor> warning) {
        this.warnings.clear();
        this.warnings.addAll(warning);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder addAttribute(final String key, final List<Object> value) {
        this.attributes.put(key, value);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder addAttribute(final String key, @Nullable final Object value) {
        if (value instanceof final Map mappedValue) {
            val list = new ArrayList<>();
            list.add(mappedValue);
            this.attributes.put(key, list);
            return this;
        }
        return addAttribute(key, CollectionUtils.toCollection(value, ArrayList.class));
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder addAttributes(final Map<String, Object> attributes) {
        attributes.forEach(this::addAttribute);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder setSuccesses(final Map<String, AuthenticationHandlerExecutionResult> successes) {
        this.successes.clear();
        return addSuccesses(successes);
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder addSuccesses(@Nullable final Map<String, AuthenticationHandlerExecutionResult> successes) {
        if (successes != null) {
            successes.forEach(this::addSuccess);
        }
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder addFailures(@Nullable final Map<String, Throwable> failures) {
        if (failures != null) {
            failures.forEach(this::addFailure);
        }
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder addSuccess(final String key, final AuthenticationHandlerExecutionResult value) {
        LOGGER.trace("Recording authentication handler result success under key [{}]", key);
        if (this.successes.containsKey(key)) {
            LOGGER.trace("Key mapped to authentication handler result [{}] is already recorded in the list of successful attempts. Overriding...",
                key);
        }
        this.successes.put(key, value);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public Authentication build() {
        val resultingCredentials = new LinkedHashMap<String, Credential>();
        credentials.forEach(credential -> {
            val key = credential.getId() + '#' + credential.getClass().getName();
            if (resultingCredentials.containsKey(key)) {
                val current = resultingCredentials.get(key);

                if (current instanceof final MutableCredential currentMutable && credential instanceof final MutableCredential credentialMutable) {
                    FunctionUtils.doIfNull(credential.getCredentialMetadata(), _ -> credentialMutable.setCredentialMetadata(new BasicCredentialMetadata(credentialMutable)));
                    FunctionUtils.doIfNull(current.getCredentialMetadata(), _ -> currentMutable.setCredentialMetadata(new BasicCredentialMetadata(currentMutable)));
                    current.getCredentialMetadata().putProperties(credential.getCredentialMetadata().getProperties());
                }
                resultingCredentials.put(key, current);
            } else {
                resultingCredentials.put(key, credential);
            }
        });

        return new DefaultAuthentication(this.authenticationDate, this.principal,
            this.warnings, new ArrayList<>(resultingCredentials.values()),
            this.attributes, this.successes, this.failures);
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder setFailures(final Map<String, Throwable> failures) {
        this.failures.clear();
        return addFailures(failures);
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder addFailure(final String key, final Throwable value) {
        LOGGER.trace("Recording authentication handler failure under key [{}]", key);
        if (this.successes.containsKey(key)) {
            val newKey = key + System.currentTimeMillis();
            LOGGER.trace("Key mapped to authentication handler failure [{}] is recorded in the list of failed attempts. Overriding with [{}]", key,
                newKey);
            this.failures.put(newKey, value);
        } else {
            this.failures.put(key, value);
        }
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder setAttributes(final Map<String, List<Object>> attributes) {
        this.attributes.clear();
        this.attributes.putAll(attributes);
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public AuthenticationBuilder mergeAttribute(final String key, final Object value) {
        return mergeAttribute(key, CollectionUtils.toCollection(value, ArrayList.class));
    }

    @Override
    @CanIgnoreReturnValue
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
    @CanIgnoreReturnValue
    public AuthenticationBuilder setAuthenticationDate(@Nullable final ZonedDateTime dateTime) {
        if (dateTime != null) {
            this.authenticationDate = dateTime;
        }
        return this;
    }

    @Override
    @CanIgnoreReturnValue
    public boolean hasAttribute(final String name, final Predicate<Object> predicate) {
        if (this.attributes.containsKey(name)) {
            val value = this.attributes.get(name);
            val valueCol = CollectionUtils.toCollection(value);
            return valueCol.stream().anyMatch(predicate);
        }
        return false;
    }

    /**
     * Sets the list of metadata about credentials presented for authentication.
     *
     * @param credentials Non-null list of credential metadata.
     * @return This builder instance.
     */
    @CanIgnoreReturnValue
    public AuthenticationBuilder setCredentials(final List<Credential> credentials) {
        this.credentials.clear();
        this.credentials.addAll(credentials);
        return this;
    }
}
