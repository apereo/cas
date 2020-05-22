package org.apereo.cas.token;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceCipherExecutor;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.PlainHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.PlainJWT;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hjson.JsonValue;
import org.hjson.Stringify;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private static final int MAP_SIZE = 8;

    private final String casSeverPrefix;

    private final CipherExecutor<Serializable, String> defaultTokenCipherExecutor;

    private final ServicesManager servicesManager;

    private final RegisteredServiceCipherExecutor registeredServiceCipherExecutor;

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
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error(e.getMessage(), ex);
                } else {
                    LOGGER.error(ex.getMessage());
                }
                throw new IllegalArgumentException("Unable to parse JWT");
            }
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
     * Unpack jwt.
     *
     * @param service the service
     * @param jwtJson the jwt json
     * @return the string
     */
    @SneakyThrows
    public JWTClaimsSet unpack(final Optional<RegisteredService> service, final String jwtJson) {
        service.ifPresent(svc -> {
            LOGGER.trace("Located service [{}] in service registry", svc);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(svc);
        });

        if (service.isPresent()) {
            val registeredService = service.get();
            LOGGER.trace("Locating service specific signing and encryption keys for [{}] in service registry", registeredService);
            if (registeredServiceCipherExecutor.supports(registeredService)) {
                LOGGER.trace("Decoding JWT based on keys provided by service [{}]", registeredService.getServiceId());
                return parse(registeredServiceCipherExecutor.decode(jwtJson, Optional.of(registeredService)));
            }
        }

        if (defaultTokenCipherExecutor.isEnabled()) {
            LOGGER.trace("Decoding JWT based on default global keys");
            return parse(defaultTokenCipherExecutor.decode(jwtJson));
        }

        return parse(jwtJson);
    }

    /**
     * Build JWT.
     *
     * @param payload the payload
     * @return the jwt
     */
    public String build(final JwtRequest payload) {
        val serviceAudience = payload.getServiceAudience();
        val claims = new JWTClaimsSet.Builder()
            .audience(serviceAudience)
            .issuer(casSeverPrefix)
            .jwtID(payload.getJwtId())
            .issueTime(payload.getIssueDate())
            .subject(payload.getSubject());

        payload.getAttributes().forEach((k, v) -> {
            if (v.size() == 1) {
                claims.claim(k, CollectionUtils.firstElement(v).get());
            } else {
                claims.claim(k, v);
            }
        });
        claims.expirationTime(payload.getValidUntilDate());

        val claimsSet = claims.build();
        val object = claimsSet.toJSONObject();
        val jwtJson = object.toJSONString();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Generated JWT [{}]", JsonValue.readJSON(jwtJson).toString(Stringify.FORMATTED));
        }
        LOGGER.trace("Locating service [{}] in service registry", serviceAudience);
        val registeredService = payload.getRegisteredService().isEmpty()
            ? locateRegisteredService(serviceAudience)
            : payload.getRegisteredService().get();
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

        LOGGER.trace("Locating service specific signing and encryption keys for [{}] in service registry", serviceAudience);
        if (registeredServiceCipherExecutor.supports(registeredService)) {
            LOGGER.trace("Encoding JWT based on keys provided by service [{}]", registeredService.getServiceId());
            return registeredServiceCipherExecutor.encode(jwtJson, Optional.of(registeredService));
        }

        if (defaultTokenCipherExecutor.isEnabled()) {
            LOGGER.trace("Encoding JWT based on default global keys for [{}]", serviceAudience);
            return defaultTokenCipherExecutor.encode(jwtJson);
        }
        val token = buildPlain(claimsSet, Optional.of(registeredService));
        LOGGER.trace("Generating plain JWT as the ticket: [{}]", token);
        return token;
    }

    /**
     * Locate registered service.
     *
     * @param serviceAudience the service audience
     * @return the registered service
     */
    protected RegisteredService locateRegisteredService(final String serviceAudience) {
        return this.servicesManager.findServiceBy(serviceAudience);
    }

    /**
     * The type Jwt request that allows the builder to create JWTs.
     */
    @Builder
    @Getter
    public static class JwtRequest {
        private final String jwtId;

        private final String serviceAudience;

        private final Date issueDate;

        private final String subject;

        private final Date validUntilDate;

        @Builder.Default
        private final Map<String, List<Object>> attributes = new LinkedHashMap<>(MAP_SIZE);

        @Builder.Default
        private Optional<RegisteredService> registeredService = Optional.empty();

    }
}
