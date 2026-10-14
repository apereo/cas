package org.apereo.cas.token.cipher;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.util.CollectionUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link RegisteredServiceJwtTicketCipherExecutor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@NoArgsConstructor
public class RegisteredServiceJwtTicketCipherExecutor extends JwtTicketCipherExecutor implements RegisteredServiceCipherExecutor {
    private static final int TOKEN_CIPHER_EXECUTORS_CACHE_SIZE = 1_000;

    private static final Duration TOKEN_CIPHER_EXECUTORS_CACHE_EXPIRATION = Duration.ofHours(1);

    private final Cache<TokenCipherExecutorKey, JwtTicketCipherExecutor> tokenCipherExecutors = Caffeine.newBuilder()
        .maximumSize(TOKEN_CIPHER_EXECUTORS_CACHE_SIZE)
        .expireAfterAccess(TOKEN_CIPHER_EXECUTORS_CACHE_EXPIRATION)
        .build();

    @Override
    public @Nullable String decode(final String data, final Optional<RegisteredService> service) {
        final Optional<Function<String, @Nullable String>> decoder = service.flatMap(this::findDecoderFor);
        return decoder.isPresent() ? decoder.get().apply(data) : decode(data, ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY);
    }

    @Override
    public String encode(final String data, final Optional<RegisteredService> service) {
        final Optional<UnaryOperator<String>> encoder = service.flatMap(this::findEncoderFor);
        return encoder.isPresent() ? encoder.get().apply(data) : encode(data, ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY);
    }

    @Override
    public Optional<UnaryOperator<String>> findEncoderFor(final RegisteredService registeredService) {
        return findTokenTicketCipherExecutorForService(registeredService).map(this::encoderFor);
    }

    @Override
    public Optional<Function<String, @Nullable String>> findDecoderFor(final RegisteredService registeredService) {
        return findTokenTicketCipherExecutorForService(registeredService).map(cipher -> decoderFor(cipher, registeredService));
    }

    @Override
    public boolean supports(@Nullable final RegisteredService registeredService) {
        return supports(registeredService, getSigningKey(registeredService), getEncryptionKey(registeredService));
    }

    /*
     * Decides support from keys already looked up, so that encoding and decoding look them up once; the keys of an
     * OpenID Connect service are read from its keystore and serialized each time.
     */
    protected boolean supports(@Nullable final RegisteredService registeredService,
                               final Optional<String> signingKey, final Optional<String> encryptionKey) {
        return signingKey.isPresent() || encryptionKey.isPresent();
    }

    /**
     * Find the token ticket cipher executor for the service, looking up its keys once.
     *
     * @param registeredService the registered service
     * @return the cipher, or empty when the service has no keys of its own
     */
    public Optional<JwtTicketCipherExecutor> findTokenTicketCipherExecutorForService(final RegisteredService registeredService) {
        val signingKey = getSigningKey(registeredService);
        val encryptionKey = getEncryptionKey(registeredService);
        if (!supports(registeredService, signingKey, encryptionKey)) {
            return Optional.empty();
        }
        LOGGER.debug("Found signing and/or encryption keys for [{}] in service registry", registeredService.getServiceId());
        return Optional.of(getTokenTicketCipherExecutorForService(registeredService,
            encryptionKey.orElse(StringUtils.EMPTY), signingKey.orElse(StringUtils.EMPTY)));
    }

    private UnaryOperator<String> encoderFor(final JwtTicketCipherExecutor cipher) {
        return data -> cipher.isEnabled() ? cipher.encode(data) : encode(data, ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY);
    }

    private Function<String, @Nullable String> decoderFor(final JwtTicketCipherExecutor cipher, final RegisteredService registeredService) {
        return data -> cipher.isEnabled()
            ? cipher.decode(data, new Object[]{registeredService})
            : decode(data, ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY);
    }

    /**
     * Gets token ticket cipher executor for service. Ciphers are built once per service definition and key material,
     * then shared, since building one parses its keys. A changed definition or a rotated key is a new cache key.
     *
     * @param registeredService the registered service
     * @return the token ticket cipher executor for service
     */
    public JwtTicketCipherExecutor getTokenTicketCipherExecutorForService(final RegisteredService registeredService) {
        return getTokenTicketCipherExecutorForService(registeredService,
            getEncryptionKey(registeredService).orElse(StringUtils.EMPTY), getSigningKey(registeredService).orElse(StringUtils.EMPTY));
    }

    private JwtTicketCipherExecutor getTokenTicketCipherExecutorForService(final RegisteredService registeredService,
                                                                          final String encryptionKey, final String signingKey) {
        val cacheKey = new TokenCipherExecutorKey(registeredService.getId(), registeredService, encryptionKey, signingKey);
        return tokenCipherExecutors.get(cacheKey, _ -> buildTokenTicketCipherExecutor(encryptionKey, signingKey, registeredService));
    }

    private JwtTicketCipherExecutor buildTokenTicketCipherExecutor(final String encryptionKey, final String signingKey,
                                                                   final RegisteredService registeredService) {
        val cipher = createCipherExecutorInstance(encryptionKey, signingKey, registeredService);
        val order = getCipherOperationsStrategyType(registeredService).orElse(CipherOperationsStrategyType.ENCRYPT_AND_SIGN);
        cipher.setStrategyType(order);
        cipher.setSigningEnabled(isSigningEnabledForRegisteredService(registeredService));
        cipher.setEncryptionEnabled(isEncryptionEnabledForRegisteredService(registeredService));
        return cipher;
    }


    protected JwtTicketCipherExecutor createCipherExecutorInstance(final String encryptionKey, final String signingKey,
                                                                   final RegisteredService registeredService) {
        val cipher = new JwtTicketCipherExecutor(encryptionKey, signingKey,
            StringUtils.isNotBlank(encryptionKey), StringUtils.isNotBlank(signingKey), 0, 0);

        val encryptionAlg = getEncryptionAlgRegisteredServiceProperty().isAssignedTo(registeredService)
            ? Objects.requireNonNull(getEncryptionAlgRegisteredServiceProperty().getPropertyValue(registeredService)).value()
            : EncryptionJwtCryptoProperties.DEFAULT_CONTENT_ENCRYPTION_ALGORITHM;
        cipher.setContentEncryptionAlgorithmIdentifier(encryptionAlg);
        cipher.getCommonHeaders().putAll(CollectionUtils.wrap(CUSTOM_HEADER_REGISTERED_SERVICE_ID, registeredService.getId()));
        return cipher;
    }

    /**
     * Gets signing key.
     *
     * @param registeredService the registered service
     * @return the signing key
     */
    public Optional<String> getSigningKey(@Nullable final RegisteredService registeredService) {
        val property = getSigningKeyRegisteredServiceProperty();
        if (property.isAssignedTo(registeredService)) {
            val signingKey = Objects.requireNonNull(property.getPropertyValue(registeredService)).value();
            return Optional.of(signingKey);
        }
        return Optional.empty();
    }

    /**
     * Gets encryption key.
     *
     * @param registeredService the registered service
     * @return the encryption key
     */
    public Optional<String> getEncryptionKey(@Nullable final RegisteredService registeredService) {
        val property = getEncryptionKeyRegisteredServiceProperty();
        if (property.isAssignedTo(registeredService)) {
            val key = Objects.requireNonNull(property.getPropertyValue(registeredService)).value();
            return Optional.of(key);
        }
        return Optional.empty();
    }

    /**
     * Determines whether signing is enabled for the registered service.
     *
     * @param registeredService the registered service
     * @return true/false
     */
    protected boolean isSigningEnabledForRegisteredService(final RegisteredService registeredService) {
        val prop = getCipherOperationRegisteredServiceSigningEnabledProperty();
        if (prop.isAssignedTo(registeredService)) {
            return prop.getPropertyBooleanValue(registeredService);
        }
        return BooleanUtils.toBoolean(prop.getDefaultValue());
    }

    /**
     * Determines whether encryption is enabled for the registered service.
     *
     * @param registeredService the registered service
     * @return true/false
     */
    protected boolean isEncryptionEnabledForRegisteredService(final RegisteredService registeredService) {
        val prop = getCipherOperationRegisteredServiceEncryptionEnabledProperty();
        if (prop.isAssignedTo(registeredService)) {
            return prop.getPropertyBooleanValue(registeredService);
        }
        return BooleanUtils.toBoolean(prop.getDefaultValue());
    }

    protected RegisteredServiceProperties getSigningKeyRegisteredServiceProperty() {
        return RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_SIGNING_KEY;
    }

    protected RegisteredServiceProperties getEncryptionKeyRegisteredServiceProperty() {
        return RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_ENCRYPTION_KEY;
    }

    protected RegisteredServiceProperties getEncryptionAlgRegisteredServiceProperty() {
        return RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_ENCRYPTION_ALG;
    }

    protected RegisteredServiceProperty.RegisteredServiceProperties getCipherOperationRegisteredServiceSigningEnabledProperty() {
        return RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_SIGNING_ENABLED;
    }

    protected RegisteredServiceProperty.RegisteredServiceProperties getCipherOperationRegisteredServiceEncryptionEnabledProperty() {
        return RegisteredServiceProperties.TOKEN_AS_SERVICE_TICKET_ENCRYPTION_ENABLED;
    }

    private record TokenCipherExecutorKey(long serviceId, RegisteredService registeredService,
                                          String encryptionKey, String signingKey) {
    }
}
