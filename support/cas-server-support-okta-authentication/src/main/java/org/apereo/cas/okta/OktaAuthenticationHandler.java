package org.apereo.cas.okta;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.okta.OktaAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;

import com.okta.authn.sdk.client.AuthenticationClients;
import com.okta.sdk.client.Proxy;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang.StringUtils;

import javax.security.auth.login.FailedLoginException;

import java.security.GeneralSecurityException;

/**
 * This is {@link OktaAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class OktaAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private final OktaAuthenticationProperties properties;

    public OktaAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                     final PrincipalFactory principalFactory,
                                     final OktaAuthenticationProperties properties) {
        super(name, servicesManager, principalFactory, properties.getOrder());
        this.properties = properties;
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException {

        try {
            val clientBuilder = AuthenticationClients.builder()
                .setOrgUrl(properties.getOrganizationUrl())
                .setConnectionTimeout(properties.getConnectionTimeout());

            if (StringUtils.isNotBlank(properties.getProxyHost()) && properties.getProxyPort() > 0) {
                if (StringUtils.isNotBlank(properties.getProxyUsername()) && StringUtils.isNotBlank(properties.getProxyPassword())) {
                    clientBuilder.setProxy(new Proxy(properties.getProxyHost(), properties.getProxyPort(),
                        properties.getProxyUsername(), properties.getProxyPassword()));
                } else {
                    clientBuilder.setProxy(new Proxy(properties.getProxyHost(), properties.getProxyPort()));
                }
            }
            val client = clientBuilder.build();

            val username = credential.getUsername();
            val adapter = new OktaAuthenticationStateHandlerAdapter(getPasswordPolicyHandlingStrategy(), getPasswordPolicyConfiguration());
            client.authenticate(username, credential.getPassword().toCharArray(), null, adapter);
            adapter.throwExceptionIfNecessary();

            LOGGER.debug("Created principal for id [{}] and [{}] attributes", adapter.getUsername(), adapter.getUserAttributes());
            val principal = this.principalFactory.createPrincipal(adapter.getUsername(), adapter.getUserAttributes());
            return createHandlerResult(credential, principal, adapter.getWarnings());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new FailedLoginException("Invalid credentials: " + e.getMessage());
        }
    }
}
