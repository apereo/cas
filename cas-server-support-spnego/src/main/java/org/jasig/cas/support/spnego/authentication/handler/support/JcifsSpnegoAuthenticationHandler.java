package org.jasig.cas.support.spnego.authentication.handler.support;

import jcifs.spnego.Authentication;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
@Component("spnegoHandler")
public final class JcifsSpnegoAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    @Autowired
    @Qualifier("spnegoAuthentication")
    private Authentication authentication;

    /**
     * Principal contains the DomainName ? (true by default).
     */
    @Value("${cas.spengo.use.principal.domain:false}")
    private boolean principalWithDomainName = true;

    /**
     * Allow SPNEGO/NTLM Token as valid credentials. (false by default)
     */
    @Value("${cas.spnego.ntlm.allowed:true}")
    private boolean isNTLMallowed;

    @Override
    protected HandlerResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        final SpnegoCredential spnegoCredential = (SpnegoCredential) credential;
        final java.security.Principal principal;
        final byte[] nextToken;
        if (!this.isNTLMallowed && spnegoCredential.isNtlm()) {
            throw new FailedLoginException("NTLM not allowed");
        }
        try {
            // proceed authentication using jcifs
            synchronized (this) {
                this.authentication.reset();
                this.authentication.process(spnegoCredential.getInitToken());
                principal = this.authentication.getPrincipal();
                nextToken = this.authentication.getNextToken();
            }
        } catch (final jcifs.spnego.AuthenticationException e) {
            throw new FailedLoginException(e.getMessage());
        }

        // evaluate jcifs response
        if (nextToken != null) {
            logger.debug("Setting nextToken in credential");
            spnegoCredential.setNextToken(nextToken);
        } else {
            logger.debug("nextToken is null");
        }

        boolean success = false;
        if (principal != null) {
            if (spnegoCredential.isNtlm()) {
                logger.debug("NTLM Credential is valid for user [{}]", principal.getName());
            } else {
                logger.debug("Kerberos Credential is valid for user [{}]", principal.getName());
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
