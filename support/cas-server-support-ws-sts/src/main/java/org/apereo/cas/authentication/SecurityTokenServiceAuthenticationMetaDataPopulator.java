package org.apereo.cas.authentication;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.metadata.BaseAuthenticationMetaDataPopulator;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.cxf.rt.security.SecurityConstants;
import org.springframework.core.Ordered;

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
                                                    final AuthenticationBuilder builder,
                                                    final WSFederationRegisteredService rp,
                                                    final SecurityTokenServiceClient sts) {
        val up = getCredential(transaction);
        if (up != null) {
            try {
                val properties = sts.getProperties();
                properties.put(SecurityConstants.USERNAME, up.getUsername());

                val uid = credentialCipherExecutor.encode(up.getUsername());
                properties.put(SecurityConstants.PASSWORD, uid);
                val token = sts.requestSecurityToken(rp.getAppliesTo());
                val tokenStr = EncodingUtils.encodeBase64(SerializationUtils.serialize(token));
                builder.addAttribute(WSFederationConstants.SECURITY_TOKEN_ATTRIBUTE, tokenStr);
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
                throw new AuthenticationException(e.getMessage());
            }
        }
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    private UsernamePasswordCredential getCredential(final AuthenticationTransaction transaction) {
        return transaction.getCredentials()
            .stream().filter(UsernamePasswordCredential.class::isInstance)
            .map(UsernamePasswordCredential.class::cast)
            .findFirst()
            .orElse(null);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        if (!this.selectionStrategy.supports(transaction.getService())) {
            return;
        }
        val service = this.selectionStrategy.resolveServiceFrom(transaction.getService());
        if (service != null) {
            val rp = this.servicesManager.findServiceBy(service, WSFederationRegisteredService.class);
            if (rp == null || !rp.getAccessStrategy().isServiceAccessAllowed()) {
                LOGGER.warn("Service [{}] is not allowed to use SSO.", rp);
                throw new UnauthorizedSsoServiceException();
            }
            val sts = clientBuilder.buildClientForSecurityTokenRequests(rp);
            invokeSecurityTokenServiceForToken(transaction, builder, rp, sts);
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return true;
    }
}
