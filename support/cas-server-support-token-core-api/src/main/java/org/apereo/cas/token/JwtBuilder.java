package org.apereo.cas.token;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jose.Header;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.PlainHeader;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link JwtBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class JwtBuilder {
    /**
     * Bean name of the builder that builds CAS tickets as JWTs.
     */
    public static final String TICKET_JWT_BUILDER_BEAN_NAME = "tokenTicketJwtBuilder";
    /**
     * Bean name of the builder that builds access tokens as JWTs.
     */
    public static final String ACCESS_TOKEN_JWT_BUILDER_BEAN_NAME = "accessTokenJwtBuilder";

    private final CipherExecutor<Serializable, String> defaultTokenCipherExecutor;

    private final ApplicationContext applicationContext;

    private final ServicesManager servicesManager;

    private final PrincipalResolver principalResolver;

    private final RegisteredServiceCipherExecutor registeredServiceCipherExecutor;

    private final ServiceFactory webApplicationServiceFactory;

    private final CasConfigurationProperties casProperties;

    public JwtBuilder(final CipherExecutor<Serializable, String> cipherExecutor,
                      final ApplicationContext applicationContext,
                      final ServicesManager servicesManager,
                      final PrincipalResolver principalResolver,
                      final CasConfigurationProperties properties,
                      final ServiceFactory webApplicationServiceFactory) {
        this(cipherExecutor, applicationContext, servicesManager, principalResolver,
            RegisteredServiceCipherExecutor.noOp(), webApplicationServiceFactory, properties);
    }

    /**
     * Parse jwt.
     *
     * @param jwt the jwt
     * @return the jwt
     */
    public static JWTClaimsSet parse(final String jwt) {
        try {
            return JWTParser.parse(jwt).getJWTClaimsSet();
        } catch (final Exception e) {
            LOGGER.trace("Unable to parse [{}] JWT; trying JWT claim set...", jwt);
            try {
                return JWTClaimsSet.parse(jwt);
            } catch (final Exception ex) {
                LoggingUtils.error(LOGGER, ex);
                throw new IllegalArgumentException("Unable to parse JWT");
            }
        }
    }

    /**
     * Parse header from signed jwt.
     *
     * @param jwt the jwt
     * @return the jws header
     * @throws Exception the exception
     */
    public static Header parseHeader(final String jwt) throws Exception {
        try {
            val signedJWT = SignedJWT.parse(jwt);
            return signedJWT.getHeader();
        } catch (final Exception e) {
            val encryptedJWT = EncryptedJWT.parse(jwt);
            return encryptedJWT.getHeader();
        }
    }
    
    /**
     * Build plain string.
     *
     * @param claimsSet         the claims set
     * @param registeredService the registered service
     * @return the jwt
     */
    public static String buildPlain(final JWTClaimsSet claimsSet,
                                    final Optional<RegisteredService> registeredService) {
        val header = new PlainHeader.Builder().type(JOSEObjectType.JWT);
        registeredService.ifPresent(svc ->
            header.customParam(RegisteredServiceCipherExecutor.CUSTOM_HEADER_REGISTERED_SERVICE_ID, svc.getId()));
        return new PlainJWT(header.build(), claimsSet).serialize();
    }

    /**
     * Unpack jwt claims set.
     *
     * @param jwtJson the jwt json
     * @return the jwt claims set
     */
    public JWTClaimsSet unpack(final String jwtJson) {
        return unpack(Optional.empty(), jwtJson);
    }

    /**
     * Unpack jwt.
     *
     * @param service the service
     * @param jwtJson the jwt json
     * @return the string
     */
    public JWTClaimsSet unpack(final Optional<RegisteredService> service, final String jwtJson) {
        return FunctionUtils.doUnchecked(() -> {
            service.ifPresent(svc -> {
                LOGGER.trace("Located service [{}] in service registry", svc);
                RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(svc);
            });

            val jwt = JWTParser.parse(jwtJson);
            if (jwt instanceof SignedJWT || jwt instanceof EncryptedJWT) {
                if (service.isPresent()) {
                    val registeredService = service.get();
                    LOGGER.trace("Locating service signing and encryption keys for [{}]", registeredService.getServiceId());
                    if (registeredServiceCipherExecutor.supports(registeredService)) {
                        LOGGER.trace("Decoding JWT based on keys provided by service [{}]", registeredService.getServiceId());
                        return parse(registeredServiceCipherExecutor.decode(jwtJson, Optional.of(registeredService)));
                    }
                }

                return FunctionUtils.doIf(defaultTokenCipherExecutor.isEnabled(),
                    () -> {
                        LOGGER.trace("Decoding JWT based on default global keys");
                        return parse(defaultTokenCipherExecutor.decode(jwtJson));
                    }, () -> {
                        throw new IllegalArgumentException("Unable to validate JWT signature");
                    }).get();
            }
            return parse(jwtJson);
        });
    }

    /**
     * Build JWT.
     *
     * @param payload the payload
     * @return the jwt
     * @throws Throwable the throwable
     */
    public String build(final JwtRequest payload) throws Throwable {
        Objects.requireNonNull(payload.getIssuer(), "Issuer cannot be undefined");
        val targetAudience = new ArrayList<>(payload.getServiceAudience());
        FunctionUtils.throwIf(targetAudience.isEmpty() && payload.getRegisteredService().isEmpty(),
            () -> new IllegalArgumentException("Service audience cannot be empty"));
        val claims = new JWTClaimsSet.Builder()
            .audience(targetAudience)
            .issuer(payload.getIssuer())
            .jwtID(payload.getJwtId())
            .issueTime(payload.getIssueDate())
            .subject(payload.getSubject());

        val attributes = collectClaims(payload);
        attributes
            .entrySet()
            .stream()
            .filter(entry -> !entry.getKey().startsWith(CentralAuthenticationService.NAMESPACE))
            .filter(entry -> !entry.getValue().isEmpty())
            .forEach(entry -> {
                val value = entry.getValue();
                var claimValue = value.size() == 1 ? CollectionUtils.firstElement(value).orElseThrow() : value;
                if (claimValue instanceof ZonedDateTime) {
                    claimValue = claimValue.toString();
                }
                claims.claim(entry.getKey(), claimValue);
            });
        claims.expirationTime(payload.getValidUntilDate());
        val claimsSet = finalizeClaims(claims.build(), payload);

        LOGGER.trace("Locating service [{}] in service registry", targetAudience);
        if (targetAudience.size() == 1 && targetAudience.getFirst().equals(casProperties.getServer().getPrefix())) {
            return build(null, claimsSet);
        }
        val registeredService = payload.getRegisteredService()
            .orElseGet(() -> targetAudience.stream()
                .map(this::locateRegisteredService)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> {
                    val formatted = "There is no application record registered with the CAS service registry that would match %s. "
                        + "Review the applications registered with the CAS service registry and make sure a matching record exists.";
                    return UnauthorizedServiceException.denied(formatted.formatted(targetAudience));
                }));
        return build(registeredService, claimsSet);
    }


    /**
     * Build JWT.
     *
     * @param registeredService the registered service
     * @param claimsSet         the claims set
     * @return the string
     */
    public String build(final RegisteredService registeredService,
                        final JWTClaimsSet claimsSet) {
        val jwtJson = claimsSet.toString();
        LOGGER.debug("Generated JWT [{}]", jwtJson);

        if (registeredServiceCipherExecutor.supports(registeredService)) {
            LOGGER.trace("Encoding JWT based on keys provided by service [{}]", registeredService.getServiceId());
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
            return registeredServiceCipherExecutor.encode(jwtJson, Optional.of(registeredService));
        }

        if (defaultTokenCipherExecutor.isEnabled()) {
            LOGGER.trace("Encoding JWT based on default global keys");
            return defaultTokenCipherExecutor.encode(jwtJson);
        }
        val token = buildPlain(claimsSet, Optional.ofNullable(registeredService));
        LOGGER.trace("Generating plain JWT as the ticket: [{}]", token);
        return token;
    }

    protected RegisteredService locateRegisteredService(final String serviceAudience) {
        val service = webApplicationServiceFactory.createService(serviceAudience);
        return servicesManager.findServiceBy(service);
    }

    protected JWTClaimsSet finalizeClaims(final JWTClaimsSet claimsSet, final JwtRequest payload) throws Exception {
        return claimsSet;
    }

    protected Map<String, List<Object>> collectClaims(final JwtRequest payload) throws Throwable {
        return payload.getAttributes();
    }

    /**
     * The type Jwt request that allows the builder to create JWTs.
     */
    @SuperBuilder
    @Getter
    @ToString
    @AllArgsConstructor
    @With
    public static class JwtRequest {
        private final String jwtId;

        @Builder.Default
        private final Set<String> serviceAudience = new HashSet<>();

        @Builder.Default
        private final Date issueDate = new Date();

        private final String subject;

        private final Date validUntilDate;

        private final String issuer;

        private boolean resolveSubject;

        @Builder.Default
        private final Map<String, List<Object>> attributes = new LinkedHashMap<>();

        @Builder.Default
        private Optional<RegisteredService> registeredService = Optional.empty();

        @Builder.Default
        private Optional<Service> service = Optional.empty();
    }
}
