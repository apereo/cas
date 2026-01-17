package org.apereo.cas.support.spnego.authentication.handler.support;

import module java.base;
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
import org.apereo.cas.util.RegexUtils;
import com.google.common.base.Splitter;
import jcifs.spnego.Authentication;
import jcifs.spnego.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;

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
@Slf4j
public class JcifsSpnegoAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private final BlockingQueue<List<Authentication>> authenticationsPool;

    private final SpnegoProperties spnegoProperties;

    public JcifsSpnegoAuthenticationHandler(final SpnegoProperties spnegoProperties,
                                            final PrincipalFactory principalFactory,
                                            final BlockingQueue<List<Authentication>> authenticationsPool) {
        super(spnegoProperties.getName(), principalFactory, spnegoProperties.getOrder());
        this.spnegoProperties = spnegoProperties;
        this.authenticationsPool = authenticationsPool;
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential, final Service service) throws Throwable {
        val spnegoCredential = (SpnegoCredential) credential;
        if (!spnegoProperties.isNtlmAllowed() && spnegoCredential.isNtlm()) {
            throw new FailedLoginException("NTLM not allowed");
        }

        try {
            LOGGER.debug("Waiting for connection to validate SPNEGO Token");
            val poolTimeoutInMilliseconds = Beans.newDuration(spnegoProperties.getPoolTimeout()).toMillis();
            val authentications = authenticationsPool.poll(poolTimeoutInMilliseconds, TimeUnit.MILLISECONDS);
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
                if (!it.hasNext()) {
                    throw new FailedLoginException(e.getMessage());
                }
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

    protected @Nullable Principal getPrincipal(final String name, final boolean isNtlm) throws Throwable {
        if (spnegoProperties.isPrincipalWithDomainName()) {
            return this.principalFactory.createPrincipal(name);
        }
        if (isNtlm) {
            if (RegexUtils.createPattern("\\S+\\\\\\S+").matcher(name).matches()) {
                val splitList = Splitter.on(RegexUtils.createPattern("\\\\")).splitToList(name);
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
