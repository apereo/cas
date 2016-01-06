package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.NullPrincipal;
import org.jasig.cas.authentication.principal.PrincipalResolver;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides an authentication manager that is inherently aware of multiple credentials and supports pluggable
 * security policy via the {@link AuthenticationPolicy} component. The authentication process is as follows:
 * <p/>
 * <ul>
 * <li>For each given credential do the following:
 * <ul>
 * <li>Iterate over all configured authentication handlers.</li>
 * <li>Attempt to authenticate a credential if a handler supports it.</li>
 * <li>On success attempt to resolve a principal by doing the following:
 * <ul>
 * <li>Check whether a resolver is configured for the handler that authenticated the credential.</li>
 * <li>If a suitable resolver is found, attempt to resolve the principal.</li>
 * <li>If a suitable resolver is not found, use the principal resolved by the authentication handler.</li>
 * </ul>
 * </li>
 * <li>Check whether the security policy (e.g. any, all) is satisfied.
 * <ul>
 * <li>If security policy is met return immediately.</li>
 * <li>Continue if security policy is not met.</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * <li>
 * After all credentials have been attempted check security policy again.
 * Note there is an implicit security policy that requires at least one credential to be authenticated.
 * Then the security policy given by {@link #setAuthenticationPolicy(AuthenticationPolicy)} is applied.
 * In all cases {@link AuthenticationException} is raised if security policy is not met.
 * </li>
 * </ul>
 * <p/>
 * It is an error condition to fail to resolve a principal.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Component("authenticationManager")
public class PolicyBasedAuthenticationManager extends AbstractAuthenticationManager {

    /**
     * Authentication security policy.
     */
    @NotNull
    protected AuthenticationPolicy authenticationPolicy = new AnyAuthenticationPolicy();

    /**
     * Instantiates a new Policy based authentication manager.
     */
    public PolicyBasedAuthenticationManager() {
    }

    /**
     * Instantiates a new Policy based authentication manager.
     *
     * @param handlers the handlers
     */
    public PolicyBasedAuthenticationManager(final AuthenticationHandler... handlers) {
        super(handlers);
    }

    /**
     * Instantiates a new Policy based authentication manager.
     *
     * @param handlers the handlers
     */
    public PolicyBasedAuthenticationManager(final List<AuthenticationHandler> handlers) {
        super(handlers);
    }

    /**
     * Instantiates a new Policy based authentication manager.
     *
     * @param map the map
     */
    public PolicyBasedAuthenticationManager(final Map<AuthenticationHandler, PrincipalResolver> map) {
        super(map);
    }

    @Override
    protected AuthenticationBuilder authenticateInternal(final AuthenticationTransaction transaction)
            throws AuthenticationException {

        final Collection<Credential> credentials = transaction.getCredentials();
        final AuthenticationBuilder builder = new DefaultAuthenticationBuilder(NullPrincipal.getInstance());
        for (final Credential c : credentials) {
            builder.addCredential(new BasicCredentialMetaData(c));
        }
        boolean found;
        final Set<AuthenticationHandler> handlerSet = this.authenticationHandlerResolver
                .resolve(this.handlerResolverMap.keySet(), transaction);

        for (final Credential credential : credentials) {
            found = false;

            for (final AuthenticationHandler handler : handlerSet) {
                if (handler.supports(credential)) {
                    found = true;
                    try {
                        authenticateAndResolvePrincipal(builder, credential, this.handlerResolverMap.get(handler), handler);
                        if (this.authenticationPolicy.isSatisfiedBy(builder.build())) {
                            return builder;
                        }
                    } catch (final GeneralSecurityException e) {
                        logger.info("{} failed authenticating {}", handler.getName(), credential);
                        logger.debug("{} exception details: {}", handler.getName(), e.getMessage());
                        builder.addFailure(handler.getName(), e.getClass());
                    } catch (final PreventedException e) {
                        logger.error("{}: {}  (Details: {})", handler.getName(), e.getMessage(), e.getCause().getMessage());
                        builder.addFailure(handler.getName(), e.getClass());
                    }
                }
            }
            if (!found) {
                logger.warn(
                        "Cannot find authentication handler that supports [{}] of type [{}], which suggests a configuration problem.",
                        credential, credential.getClass().getSimpleName());
            }
        }
        evaluateProducedAuthenticationContext(builder);

        return builder;
    }

    /**
     * Evaluate produced authentication context.
     *
     * @param builder the builder
     * @throws AuthenticationException the authentication exception
     */
    protected void evaluateProducedAuthenticationContext(final AuthenticationBuilder builder) throws AuthenticationException {
        // We apply an implicit security policy of at least one successful authentication
        if (builder.getSuccesses().isEmpty()) {
            throw new AuthenticationException(builder.getFailures(), builder.getSuccesses());
        }
        // Apply the configured security policy
        if (!this.authenticationPolicy.isSatisfiedBy(builder.build())) {
            throw new AuthenticationException(builder.getFailures(), builder.getSuccesses());
        }
    }

    /**
     * Sets the authentication policy used by this component.
     *
     * @param policy Non-null authentication policy. The default policy is {@link AnyAuthenticationPolicy}.
     */
    @Resource(name = "authenticationPolicy")
    public void setAuthenticationPolicy(final AuthenticationPolicy policy) {
        this.authenticationPolicy = policy;
    }

}
