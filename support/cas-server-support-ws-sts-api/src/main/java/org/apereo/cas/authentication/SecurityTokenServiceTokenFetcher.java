package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.cxf.rt.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;

import java.util.Optional;

/**
 * This is {@link SecurityTokenServiceTokenFetcher}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
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
            val properties = sts.getProperties();
            properties.put(SecurityConstants.USERNAME, principalId);
            val uid = credentialCipherExecutor.encode(principalId);
            properties.put(SecurityConstants.PASSWORD, uid);
            LOGGER.debug("Requesting security token for principal [{}] and registered service [{}]", uid, rp);
            return sts.requestSecurityToken(rp.getAppliesTo());
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
            throw new AuthenticationException(e.getMessage());
        }
    }

    /**
     * Fetch.
     *
     * @param service     the service
     * @param principalId the principal id
     * @return the security token
     */
    public Optional<SecurityToken> fetch(final Service service, final String principalId) {
        val resolvedService = this.selectionStrategy.resolveServiceFrom(service);
        LOGGER.debug("Resolved service as [{}]", resolvedService);
        if (resolvedService != null) {
            val rp = this.servicesManager.findServiceBy(resolvedService, WSFederationRegisteredService.class);
            if (rp == null || !rp.getAccessStrategy().isServiceAccessAllowed()) {
                LOGGER.warn("Service [{}] is not allowed to use SSO.", rp);
                throw new UnauthorizedSsoServiceException();
            }
            LOGGER.debug("Building security token service client for registered service [{}]", rp);
            val sts = clientBuilder.buildClientForSecurityTokenRequests(rp);
            return Optional.ofNullable(invokeSecurityTokenServiceForToken(rp, sts, principalId));
        }
        return Optional.empty();
    }
}
