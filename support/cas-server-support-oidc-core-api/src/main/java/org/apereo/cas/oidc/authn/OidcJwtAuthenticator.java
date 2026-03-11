package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreUtils;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyUsage;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationEntry;
import org.apereo.cas.oidc.jwks.register.ClientJwksRegistrationStore;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jspecify.annotations.Nullable;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;

/**
 * This is {@link OidcJwtAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OidcJwtAuthenticator implements Authenticator {
    protected final OidcIssuerService issuerService;

    protected final ServicesManager servicesManager;

    protected final AuditableExecution registeredServiceAccessStrategyEnforcer;

    protected final TicketRegistry ticketRegistry;

    protected final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    protected final CasConfigurationProperties casProperties;

    protected final ApplicationContext applicationContext;

    protected final OidcServerDiscoverySettings oidcServerDiscoverySettings;

    protected final ObjectProvider<ClientJwksRegistrationStore> clientJwksRegistrationStore;

    protected @Nullable JWT verifyCredentials(final UsernamePasswordCredentials credentials,
                                              final WebContext webContext) {
        if (!Strings.CI.equals(OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER, credentials.getUsername())) {
            LOGGER.debug("client assertion type is not set to [{}]", OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER);
            return null;
        }
        if (StringUtils.isBlank(credentials.getPassword())) {
            LOGGER.debug("No assertion is available in the provided credentials");
            return null;
        }

        try {
            val jwt = JWTParser.parse(credentials.getPassword());
            val alg = jwt.getHeader().getAlgorithm();
            if (!validateJwtAlgorithm(alg)) {
                LOGGER.debug("No assertion is available in the provided credentials");
                return null;
            }
            return jwt;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }


    @Override
    public Optional<Credentials> validate(final CallContext callContext, final Credentials creds) {
        return FunctionUtils.doAndHandle(() -> {
            val registeredService = getOidcRegisteredService(callContext);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
            Objects.requireNonNull(registeredService, "regisetered service is null");

            if (OAuth20Utils.isAccessTokenRequest(callContext.webContext())) {
                val authMethodDisabled = oidcServerDiscoverySettings.getTokenEndpointAuthMethodsSupported()
                    .stream()
                    .map(OAuth20ClientAuthenticationMethods::parse)
                    .noneMatch(method -> method == OAuth20ClientAuthenticationMethods.CLIENT_SECRET_JWT || method == OAuth20ClientAuthenticationMethods.PRIVATE_KEY_JWT);
                if (authMethodDisabled || !OAuth20Utils.isTokenAuthenticationMethodSupportedFor(callContext, registeredService,
                    OAuth20ClientAuthenticationMethods.CLIENT_SECRET_JWT, OAuth20ClientAuthenticationMethods.PRIVATE_KEY_JWT)) {
                    LOGGER.warn("Private key JWT authentication method is not enabled for CAS, or is not supported for service [{}]", registeredService.getName());
                    return Optional.<Credentials>empty();
                }
            }

            val credentials = (UsernamePasswordCredentials) creds;
            val jwt = verifyCredentials(credentials, callContext.webContext());
            if (jwt == null) {
                LOGGER.warn("Unable to verify credentials");
                return Optional.<Credentials>empty();
            }

            val keys = new JsonWebKeySet();
            clientJwksRegistrationStore.ifAvailable(Unchecked.consumer(store -> {
                if (jwt instanceof final SignedJWT signedJWT) {
                    val jwk = signedJWT.getHeader().getJWK();
                    val kid = signedJWT.getHeader().getKeyID();
                    val jkt = jwk != null ? jwk.computeThumbprint().toString() : StringUtils.EMPTY;
                    store.findByJkt(jkt)
                        .or(() -> store.findByJkt(kid))
                        .map(ClientJwksRegistrationEntry::jwk)
                        .ifPresent(registereredKey -> {
                            val webKey = EncodingUtils.newJsonWebKey(registereredKey);
                            keys.addJsonWebKey(webKey);
                        });
                }
            }));

            OidcJsonWebKeyStoreUtils.getJsonWebKeySet(registeredService,
                    applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING))
                .ifPresent(set -> set.getJsonWebKeys().forEach(keys::addJsonWebKey));

            val issuer = issuerService.determineIssuer(Optional.of(registeredService));
            for (var i = 0; credentials.getUserProfile() == null && i < keys.getJsonWebKeys().size(); i++) {
                val jsonWebKey = keys.getJsonWebKeys().get(i);
                val consumer = new JwtConsumerBuilder()
                    .setVerificationKey(jsonWebKey.getKey())
                    .setRequireJwtId()
                    .setRequireExpirationTime()
                    .setRequireSubject()
                    .setExpectedIssuer(true, issuer)
                    .setExpectedAudience(true, registeredService.getClientId())
                    .build();
                determineUserProfile(credentials, consumer);
            }
            return Optional.<Credentials>of(credentials);
        }, e -> {
            LoggingUtils.error(LOGGER, e);
            return Optional.<Credentials>empty();
        }).get();
    }

    protected @Nullable OidcRegisteredService getOidcRegisteredService(final CallContext callContext) throws Throwable {
        val webContext = callContext.webContext();
        val code = webContext.getRequestParameter(OAuth20Constants.CODE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val oauthCode = FunctionUtils.doAndHandle(() -> {
            val givenCode = ticketRegistry.getTicket(code, OAuth20Code.class);
            return givenCode == null || givenCode.isExpired() ? null : givenCode;
        });
        val clientId = oauthCode == null ? webContext.getRequestParameter(OAuth20Constants.CLIENT_ID).orElse(null) : oauthCode.getClientId();
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId, OidcRegisteredService.class);
        val audit = AuditableContext.builder()
            .registeredService(registeredService)
            .build();
        val accessResult = registeredServiceAccessStrategyEnforcer.execute(audit);
        return accessResult.isExecutionFailure() ? null : registeredService;
    }

    protected void determineUserProfile(final UsernamePasswordCredentials credentials,
                                        final JwtConsumer consumer) {
        FunctionUtils.doAndHandle(_ -> {
            val jwt = consumer.processToClaims(credentials.getPassword());
            val userProfile = new CommonProfile(true);
            userProfile.setId(jwt.getSubject());
            userProfile.addAttributes(jwt.getClaimsMap());
            credentials.setUserProfile(userProfile);
        });
    }

    protected boolean validateJwtAlgorithm(final Algorithm alg) {
        val jwsAlgorithm = JWSAlgorithm.parse(alg.getName());
        return JWSAlgorithm.Family.HMAC_SHA.contains(jwsAlgorithm)
            || JWSAlgorithm.Family.RSA.contains(jwsAlgorithm)
            || JWSAlgorithm.Family.EC.contains(jwsAlgorithm)
            || JWSAlgorithm.Family.ED.contains(jwsAlgorithm);
    }
}
