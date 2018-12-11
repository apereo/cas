package org.apereo.cas.authentication;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.metadata.BaseAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.cxf.rt.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.springframework.core.Ordered;

import java.util.Map;

/**
 * This is {@link SecurityTokenServiceAuthenticationMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
@ToString(callSuper = true)
public class SecurityTokenServiceAuthenticationMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {

    private final ServicesManager servicesManager;
    private final AuthenticationServiceSelectionStrategy selectionStrategy;
    private final CipherExecutor<String, String> credentialCipherExecutor;
    private final SecurityTokenServiceClientBuilder clientBuilder;

    private void invokeSecurityTokenServiceForToken(final AuthenticationTransaction transaction,
                                                    final AuthenticationBuilder builder, final WSFederationRegisteredService rp,
                                                    final SecurityTokenServiceClient sts) {
        final UsernamePasswordCredential up = transaction.getCredentials()
            .stream()
            .filter(UsernamePasswordCredential.class::isInstance)
            .map(UsernamePasswordCredential.class::cast)
            .findFirst()
            .orElse(null);
        if (up != null) {

            try {
                final Map<String, Object> properties = sts.getProperties();
                properties.put(SecurityConstants.USERNAME, up.getUsername());
                final String uid = credentialCipherExecutor.encode(up.getUsername());
                properties.put(SecurityConstants.PASSWORD, uid);
                LOGGER.debug("Requesting security token for principal [{}] and registered service [{}]", uid, rp);
                final SecurityToken token = sts.requestSecurityToken(rp.getAppliesTo());
                final String tokenStr = EncodingUtils.encodeBase64(SerializationUtils.serialize(token));
                LOGGER.debug("Encoded security token attribute as [{}]", tokenStr);
                builder.addAttribute(WSFederationConstants.SECURITY_TOKEN_ATTRIBUTE, tokenStr);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new AuthenticationException(e.getMessage());
            }
        } else {
            LOGGER.debug("Ignoring security token service for token without a valid credential");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        final Service svc = transaction.getService();
        if (!this.selectionStrategy.supports(svc)) {
            LOGGER.debug("Service selection strategy does not support service [{}]", svc);
            return;
        }
        final Service service = this.selectionStrategy.resolveServiceFrom(svc);
        LOGGER.debug("Resolved service as [{}]", service);
        if (service != null) {
            final WSFederationRegisteredService rp = this.servicesManager.findServiceBy(service, WSFederationRegisteredService.class);
            if (rp == null || !rp.getAccessStrategy().isServiceAccessAllowed()) {
                LOGGER.warn("Service [{}] is not allowed to use SSO.", rp);
                throw new UnauthorizedSsoServiceException();
            }
            LOGGER.debug("Building security token service client for registered service [{}]", rp);
            final SecurityTokenServiceClient sts = clientBuilder.buildClientForSecurityTokenRequests(rp);
            invokeSecurityTokenServiceForToken(transaction, builder, rp, sts);
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return true;
    }
}
