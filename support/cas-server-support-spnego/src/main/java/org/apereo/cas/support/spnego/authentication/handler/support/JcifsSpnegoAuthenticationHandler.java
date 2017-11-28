package org.apereo.cas.support.spnego.authentication.handler.support;

import jcifs.spnego.Authentication;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import javax.security.auth.login.FailedLoginException;
import java.security.GeneralSecurityException;
import java.util.regex.Pattern;

/**
 * Implementation of an AuthenticationHandler for SPNEGO supports. This Handler
 * support both NTLM and Kerberos. NTLM is disabled by default.
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
@NotThreadSafe
public class JcifsSpnegoAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(JcifsSpnegoAuthenticationHandler.class);
    
    private Authentication authentication;
    private boolean principalWithDomainName;
    private boolean isNTLMallowed;

    private final Object lock = new Object();
    
    public JcifsSpnegoAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                            final Authentication authentication, final boolean principalWithDomainName, final boolean isNTLMallowed) {
        super(name, servicesManager, principalFactory, null);
        this.authentication = authentication;
        this.principalWithDomainName = principalWithDomainName;
        this.isNTLMallowed = isNTLMallowed;
    }

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException {
        final SpnegoCredential spnegoCredential = (SpnegoCredential) credential;
        final java.security.Principal principal;
        final byte[] nextToken;
        if (!this.isNTLMallowed && spnegoCredential.isNtlm()) {
            throw new FailedLoginException("NTLM not allowed");
        }
        try {
            // proceed authentication using jcifs
            synchronized (this.lock) {
                this.authentication.reset();
                
                LOGGER.debug("Processing SPNEGO authentication");
                this.authentication.process(spnegoCredential.getInitToken());
                
                principal = this.authentication.getPrincipal();
                LOGGER.debug("Authenticated SPNEGO principal [{}]", principal != null ? principal.getName() : null);

                LOGGER.debug("Retrieving the next token for authentication");
                nextToken = this.authentication.getNextToken();
            }
        } catch (final jcifs.spnego.AuthenticationException e) {
            LOGGER.debug("Processing SPNEGO authentication failed with exception", e);
            throw new FailedLoginException(e.getMessage());
        }

        // evaluate jcifs response
        if (nextToken != null) {
            LOGGER.debug("Setting nextToken in credential");
            spnegoCredential.setNextToken(nextToken);
        } else {
            LOGGER.debug("nextToken is null");
        }

        boolean success = false;
        if (principal != null) {
            if (spnegoCredential.isNtlm()) {
                LOGGER.debug("NTLM Credential is valid for user [{}]", principal.getName());
            } else {
                LOGGER.debug("Kerberos Credential is valid for user [{}]", principal.getName());
            }
            spnegoCredential.setPrincipal(getPrincipal(principal.getName(), spnegoCredential.isNtlm()));
            success = true;
        }

        if (!success) {
            throw new FailedLoginException("Principal is null, the processing of the SPNEGO Token failed");
        }
        return new DefaultHandlerResult(this, new BasicCredentialMetaData(credential), spnegoCredential.getPrincipal());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof SpnegoCredential;
    }

    public void setAuthentication(final Authentication authentication) {
        this.authentication = authentication;
    }

    public void setPrincipalWithDomainName(final boolean principalWithDomainName) {
        this.principalWithDomainName = principalWithDomainName;
    }

    public void setNTLMallowed(final boolean isNTLMallowed) {
        this.isNTLMallowed = isNTLMallowed;
    }

    /**
     * Gets the principal from the given name. The principal
     * is created by the factory instance.
     *
     * @param name the name
     * @param isNtlm the is ntlm
     * @return the simple principal
     */
    protected Principal getPrincipal(final String name, final boolean isNtlm) {
        if (this.principalWithDomainName) {
            return this.principalFactory.createPrincipal(name);
        }
        if (isNtlm) {
            return Pattern.matches("\\S+\\\\\\S+", name)
                    ? this.principalFactory.createPrincipal(name.split("\\\\")[1])
                    : this.principalFactory.createPrincipal(name);
        }
        return this.principalFactory.createPrincipal(name.split("@")[0]);
    }
}
