package org.jasig.cas.support.wsfederation.authentication.principal;

import org.jasig.cas.support.wsfederation.WsFederationConfiguration;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.PersonDirectoryPrincipalResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This class resolves the principal id regarding the WsFederation credentials.
 *
 * @author John Gasper
 * @since 4.2.0
 */
@Component("adfsPrincipalResolver")
public final class WsFederationCredentialsToPrincipalResolver extends PersonDirectoryPrincipalResolver {

    private final Logger logger = LoggerFactory.getLogger(WsFederationCredentialsToPrincipalResolver.class);

    @Autowired
    @Qualifier("wsFedConfig")
    private WsFederationConfiguration configuration;

    /**
     * Extracts the principalId.
     *
     * @param credentials the credentials
     * @return the principal id
     */
    @Override
    protected String extractPrincipalId(final Credential credentials) {

        final WsFederationCredential wsFedCredentials = (WsFederationCredential) credentials;
        final String principalId = wsFedCredentials.getAttributes().get(
                this.configuration.getIdentityAttribute()
        ).toString();
        logger.debug("principalId : {}", principalId);
        return principalId;
    }



    /**
     * Sets the configuration.
     *
     * @param configuration a configuration
     */
    public void setConfiguration(final WsFederationConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && (WsFederationCredential.class.isAssignableFrom(credential.getClass()));
    }

}
