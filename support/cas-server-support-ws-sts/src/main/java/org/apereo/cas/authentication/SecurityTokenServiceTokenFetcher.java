package org.apereo.cas.authentication;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.rt.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;

import java.util.Map;
import java.util.Optional;

/**
 * This is {@link SecurityTokenServiceTokenFetcher}.
 *
 * @author Misagh Moayyed
 * @since 5.3.7
 */
@Slf4j
@RequiredArgsConstructor
@ToString(callSuper = true)
public class SecurityTokenServiceTokenFetcher {
    private final ServicesManager servicesManager;
    private final AuthenticationServiceSelectionStrategy selectionStrategy;
    private final CipherExecutor<String, String> credentialCipherExecutor;
    private final SecurityTokenServiceClientBuilder clientBuilder;

    private SecurityToken invokeSecurityTokenServiceForToken(final WSFederationRegisteredService rp,
                                                             final SecurityTokenServiceClient sts,
                                                             final String principalId) {

        try {
            final Map<String, Object> properties = sts.getProperties();
            properties.put(SecurityConstants.USERNAME, principalId);
            final String uid = credentialCipherExecutor.encode(principalId);
            properties.put(SecurityConstants.PASSWORD, uid);
            LOGGER.debug("Requesting security token for principal [{}] and registered service [{}]", uid, rp);
            return sts.requestSecurityToken(rp.getAppliesTo());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new AuthenticationException(e.getMessage());
        }
    }

    /**
     * Fetch.
     *
     * @param service     the service
     * @param principalId the principal id
     */
    public Optional<SecurityToken> fetch(final Service service, final String principalId) {
        final Service resolvedService = this.selectionStrategy.resolveServiceFrom(service);
        LOGGER.debug("Resolved resolvedService as [{}]", resolvedService);
        if (resolvedService != null) {
            final WSFederationRegisteredService rp = this.servicesManager.findServiceBy(resolvedService, WSFederationRegisteredService.class);
            if (rp == null || !rp.getAccessStrategy().isServiceAccessAllowed()) {
                LOGGER.warn("Service [{}] is not allowed to use SSO.", rp);
                throw new UnauthorizedSsoServiceException();
            }
            LOGGER.debug("Building security token resolvedService client for registered resolvedService [{}]", rp);
            final SecurityTokenServiceClient sts = clientBuilder.buildClientForSecurityTokenRequests(rp);
            return Optional.ofNullable(invokeSecurityTokenServiceForToken(rp, sts, principalId));
        }
        return Optional.empty();
    }

}
