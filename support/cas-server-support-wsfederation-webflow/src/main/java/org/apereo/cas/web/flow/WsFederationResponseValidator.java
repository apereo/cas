package org.apereo.cas.web.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.wsfederation.WsFederationConfiguration;
import org.apereo.cas.support.wsfederation.WsFederationHelper;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManager;
import org.apereo.cas.web.support.WebUtils;
import org.opensaml.saml.saml1.core.Assertion;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;

/**
 * This is {@link WsFederationResponseValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class WsFederationResponseValidator {
    private static final String WRESULT = "wresult";

    private final WsFederationHelper wsFederationHelper;
    private final Collection<WsFederationConfiguration> configurations;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final WsFederationCookieManager wsFederationCookieManager;

    /**
     * Validate ws federation authentication request event.
     *
     * @param context the context
     */
    public void validateWsFederationAuthenticationRequest(final RequestContext context) {
        final var service = wsFederationCookieManager.retrieve(context);
        LOGGER.debug("Retrieved service [{}] from the session cookie", service);

        final var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final var wResult = request.getParameter(WRESULT);
        LOGGER.debug("Parameter [{}] received: [{}]", WRESULT, wResult);
        if (StringUtils.isBlank(wResult)) {
            LOGGER.error("No [{}] parameter is found", WRESULT);
            throw new IllegalArgumentException("Missing parameter " + WRESULT);
        }
        LOGGER.debug("Attempting to create an assertion from the token parameter");
        final var rsToken = wsFederationHelper.getRequestSecurityTokenFromResult(wResult);
        final var assertion = wsFederationHelper.buildAndVerifyAssertion(rsToken, configurations);
        if (assertion == null) {
            LOGGER.error("Could not validate assertion via parsing the token from [{}]", WRESULT);
            throw new IllegalArgumentException("Could not validate assertion via the provided token");
        }
        LOGGER.debug("Attempting to validate the signature on the assertion");
        if (!wsFederationHelper.validateSignature(assertion)) {
            final var msg = "WS Requested Security Token is blank or the signature is not valid.";
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }
        buildCredentialsFromAssertion(context, assertion, service);
    }

    private void buildCredentialsFromAssertion(final RequestContext context,
                                               final Pair<Assertion, WsFederationConfiguration> assertion,
                                               final Service service) {
        try {
            LOGGER.debug("Creating credential based on the provided assertion");
            final var credential = wsFederationHelper.createCredentialFromToken(assertion.getKey());
            final var configuration = assertion.getValue();
            final var rpId = wsFederationHelper.getRelyingPartyIdentifier(service, configuration);

            if (credential == null) {
                LOGGER.error("No credential could be extracted from [{}] based on relying party identifier [{}] and identity provider identifier [{}]",
                    assertion.getKey(), rpId, configuration.getIdentityProviderIdentifier());
                throw new IllegalArgumentException("Could not extract and identify credentials");
            }

            if (credential != null && credential.isValid(rpId, configuration.getIdentityProviderIdentifier(), configuration.getTolerance())) {
                LOGGER.debug("Validated assertion for the created credential successfully");
                if (configuration.getAttributeMutator() != null) {
                    LOGGER.debug("Modifying credential attributes based on [{}]", configuration.getAttributeMutator().getClass().getSimpleName());
                    configuration.getAttributeMutator().modifyAttributes(credential.getAttributes());
                }
            } else {
                LOGGER.error("SAML assertions are blank or no longer valid based on RP identifier [{}] and identity provider identifier [{}]",
                    rpId, configuration.getIdentityProviderIdentifier());
                throw new IllegalArgumentException("Could not validate the provided assertion");
            }
            context.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, service);
            LOGGER.debug("Creating final authentication result based on the given credential");
            final var authenticationResult = this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);
            WebUtils.putAuthenticationResult(authenticationResult, context);
            WebUtils.putAuthentication(authenticationResult.getAuthentication(), context);
            LOGGER.info("Token validated and new [{}] created: [{}]", credential.getClass().getName(), credential);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
}
