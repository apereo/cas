package org.apereo.cas.support.spnego.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.spnego.SpnegoProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.spnego.authentication.principal.SpnegoCredential;

import com.google.common.base.Splitter;
import jcifs.spnego.Authentication;
import jcifs.spnego.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.concurrent.NotThreadSafe;
import javax.security.auth.login.FailedLoginException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
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
@Slf4j
public class JcifsSpnegoAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private final BlockingQueue<List<Authentication>> authenticationsPool;

    private final boolean principalWithDomainName;

    private final boolean ntlmAllowed;

    private final long poolTimeoutInMilliseconds;

    public JcifsSpnegoAuthenticationHandler(final SpnegoProperties spnegoProperties,
                                            final PrincipalFactory principalFactory, final BlockingQueue<List<Authentication>> authenticationsPool) {
        super(spnegoProperties.getName(), principalFactory, spnegoProperties.getOrder());
        this.authenticationsPool = authenticationsPool;
        this.principalWithDomainName = spnegoProperties.isPrincipalWithDomainName();
        this.ntlmAllowed = spnegoProperties.isNtlmAllowed();
        this.poolTimeoutInMilliseconds = Beans.newDuration(spnegoProperties.getPoolTimeout()).toMillis();
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) throws Throwable {
        val spnegoCredential = (SpnegoCredential) credential;
        if (!this.ntlmAllowed && spnegoCredential.isNtlm()) {
            throw new FailedLoginException("NTLM not allowed");
        }

        try {
            LOGGER.debug("Waiting for connection to validate SPNEGO Token");
            val authentications = authenticationsPool.poll(this.poolTimeoutInMilliseconds, TimeUnit.MILLISECONDS);
            if (authentications != null) {
                try {
                    return doInternalAuthentication(authentications, spnegoCredential, service);
                } finally {
                    authenticationsPool.add(authentications);
                    LOGGER.debug("Returned connection to pool");
                }
            }
            throw new FailedLoginException("Cannot get connection from pool to validate SPNEGO Token");
        } catch (final InterruptedException e) {
            throw new FailedLoginException("Thread interrupted while waiting for connection to validate SPNEGO Token");
        }
    }

    protected AuthenticationHandlerExecutionResult doInternalAuthentication(final List<Authentication> authentications,
                                                                            final SpnegoCredential spnegoCredential, final Service service) throws Throwable {
        var principal = (java.security.Principal) null;
        var nextToken = (byte[]) null;
        val it = authentications.iterator();
        while (nextToken == null && it.hasNext()) {
            try {
                val authentication = it.next();
                authentication.reset();
                LOGGER.debug("Processing SPNEGO authentication");
                authentication.process(spnegoCredential.getInitToken());
                principal = authentication.getPrincipal();
                LOGGER.debug("Authenticated SPNEGO principal [{}]. Retrieving the next token for authentication...",
                    Optional.ofNullable(principal).map(java.security.Principal::getName).orElse(null));
                nextToken = authentication.getNextToken();
            } catch (final AuthenticationException e) {
                LOGGER.debug("Processing SPNEGO authentication failed with exception", e);
                throw new FailedLoginException(e.getMessage());
            }
        }

        if (nextToken != null) {
            LOGGER.debug("Setting nextToken in credential");
            spnegoCredential.setNextToken(nextToken);
        } else {
            LOGGER.debug("nextToken is null");
        }
        var success = false;
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
        return new DefaultAuthenticationHandlerExecutionResult(this, spnegoCredential, spnegoCredential.getPrincipal());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof SpnegoCredential;
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return SpnegoCredential.class.isAssignableFrom(clazz);
    }

    /**
     * Gets the principal from the given name. The principal
     * is created by the factory instance.
     *
     * @param name   the name
     * @param isNtlm the is ntlm
     * @return the simple principal
     */
    protected Principal getPrincipal(final String name, final boolean isNtlm) throws Throwable {
        if (this.principalWithDomainName) {
            return this.principalFactory.createPrincipal(name);
        }
        if (isNtlm) {
            if (Pattern.matches("\\S+\\\\\\S+", name)) {
                val splitList = Splitter.on(Pattern.compile("\\\\")).splitToList(name);
                if (splitList.size() == 2) {
                    return this.principalFactory.createPrincipal(splitList.get(1));
                }
            }
            return this.principalFactory.createPrincipal(name);
        }
        val splitList = Splitter.on("@").splitToList(name);
        return this.principalFactory.createPrincipal(splitList.getFirst());
    }
}
