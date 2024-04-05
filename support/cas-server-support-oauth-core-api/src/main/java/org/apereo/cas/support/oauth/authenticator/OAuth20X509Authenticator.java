package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.http.credentials.X509Credentials;
import org.pac4j.http.credentials.authenticator.X509Authenticator;
import java.util.Optional;

/**
 * This is {@link OAuth20X509Authenticator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class OAuth20X509Authenticator implements Authenticator {
    private final ServicesManager servicesManager;

    private final OAuth20RequestParameterResolver requestParameterResolver;

    @Override
    public Optional<Credentials> validate(final CallContext ctx, final Credentials credentials) {
        val clientIdAndSecret = requestParameterResolver.resolveClientIdAndClientSecret(ctx);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientIdAndSecret.getKey());
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

        if (!isAuthenticationMethodSupported(ctx, registeredService)) {
            LOGGER.warn("TLS authentication method is not supported for service [{}]", registeredService.getName());
            return Optional.empty();
        }

        val allowedSubjectPattern = StringUtils.defaultIfBlank(registeredService.getTlsClientAuthSubjectDn(), "CN=(.*?)(?:,|$)");
        val x509Authenticator = new X509Authenticator(allowedSubjectPattern);
        val result = x509Authenticator.validate(ctx, credentials);
        if (result.isPresent()) {
            val profile = result.get().getUserProfile();
            val certificate = ((X509Credentials) credentials).getCertificate();
            val digest = EncodingUtils.encodeBase64(DigestUtils.digest("SHA-256", certificate.getPublicKey().getEncoded()));
            profile.addAttribute(OAuth20Constants.X509_CERTIFICATE_DIGEST, digest);
            profile.addAttribute(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE, "X.509");
            profile.addAttribute(OAuth20Constants.CLIENT_ID, clientIdAndSecret.getKey());
            
            val attributeMap = CollectionUtils.<String, String>wrap(
                "x509-sanEmail", registeredService.getTlsClientAuthSanEmail(),
                "x509-sanDNS", registeredService.getTlsClientAuthSanDns(),
                "x509-sanIP", registeredService.getTlsClientAuthSanIp(),
                "x509-sanURI", registeredService.getTlsClientAuthSanUri()
            );
            val accepted = attributeMap
                .entrySet()
                .stream()
                .allMatch(entry -> isAcceptableX509Attribute(profile, entry.getKey(), entry.getValue()));
            if (!accepted) {
                throw new CredentialsException("Unable to accept certificate");
            }
        }

        return result;
    }

    protected boolean isAuthenticationMethodSupported(final CallContext ctx, final OAuthRegisteredService registeredService) {
        return OAuth20Utils.isTokenAuthenticationMethodSupportedFor(ctx, registeredService, OAuth20ClientAuthenticationMethods.TLS_CLIENT_AUTH);
    }

    protected boolean isAcceptableX509Attribute(final UserProfile profile, final String pattern, final String attribute) {
        return FunctionUtils.doIfNotBlank(pattern,
            () -> {
                val values = CollectionUtils.toCollection(profile.getAttribute(attribute));
                return !values.isEmpty() && values.stream().anyMatch(email -> RegexUtils.find(pattern, email.toString()));
            },
            () -> Boolean.TRUE);
    }
}
