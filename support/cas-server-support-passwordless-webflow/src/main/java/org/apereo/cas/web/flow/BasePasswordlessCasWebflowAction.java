package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link BasePasswordlessCasWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BasePasswordlessCasWebflowAction extends BaseCasWebflowAction {

    protected final CasConfigurationProperties casProperties;

    protected final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy;

    protected final PrincipalFactory passwordlessPrincipalFactory;

    protected final AuthenticationSystemSupport authenticationSystemSupport;

    /**
     * Should activate multifactor authentication for user?
     *
     * @param requestContext the request context
     * @param user           the user
     * @return true/false
     */
    protected boolean shouldActivateMultifactorAuthenticationFor(final RequestContext requestContext,
                                                                 final PasswordlessUserAccount user) {
        val status = user.getMultifactorAuthenticationEligible();
        if (status.isTrue()) {
            LOGGER.trace("Passwordless account [{}] is eligible for multifactor authentication", user);
            return true;
        }
        if (status.isFalse()) {
            LOGGER.trace("Passwordless account [{}] is not eligible for multifactor authentication", user);
            return false;
        }
        return casProperties.getAuthn().getPasswordless().getCore().isMultifactorAuthenticationActivated();

    }
    
    /**
     * Should delegate authentication for user?
     *
     * @param requestContext the request context
     * @param user           the user
     * @return true/false
     */
    protected boolean isDelegatedAuthenticationActiveFor(final RequestContext requestContext,
                                                         final PasswordlessUserAccount user) {
        val status = user.getDelegatedAuthenticationEligible();
        if (status.isTrue()) {
            LOGGER.trace("Passwordless account [{}] is eligible for delegated authentication", user);
            return true;
        }
        if (status.isFalse()) {
            LOGGER.trace("Passwordless account [{}] is not eligible for delegated authentication", user);
            return false;
        }
        return casProperties.getAuthn().getPasswordless().getCore().isDelegatedAuthenticationActivated();
    }

    protected boolean doesPasswordlessAccountRequestPassword(final PasswordlessUserAccount user) {
        return user.isRequestPassword();
    }

    protected Authentication buildAuthentication(final PasswordlessUserAccount user) throws Throwable {
        val userAttributes = CollectionUtils.toMultiValuedMap((Map) user.getAttributes());
        val resolvedPrincipal = authenticationSystemSupport.getPrincipalResolver()
            .resolve(new BasicIdentifiableCredential(user.getUsername()));
        val attributes = CoreAuthenticationUtils.mergeAttributes(userAttributes, resolvedPrincipal.getAttributes());
        val principalId = resolvedPrincipal instanceof NullPrincipal ? user.getUsername() : resolvedPrincipal.getId();
        val principal = passwordlessPrincipalFactory.createPrincipal(principalId, attributes);
        return DefaultAuthenticationBuilder.newInstance().setPrincipal(principal).build();
    }

    protected void populateContextWithAuthenticationResult(final RequestContext requestContext, final Authentication authentication,
                                                           final WebApplicationService service) throws Throwable {
        val builder = authenticationSystemSupport.getAuthenticationResultBuilderFactory().newBuilder();
        val authenticationResult = builder.collect(authentication).build(service);
        WebUtils.putAuthenticationResultBuilder(builder, requestContext);
        WebUtils.putAuthenticationResult(authenticationResult, requestContext);
        WebUtils.putAuthentication(authentication, requestContext);
    }

    protected Optional<MultifactorAuthenticationProvider> resolveMultifactorAuthenticationProvider(
        final RequestContext requestContext, final Authentication authentication,
        final WebApplicationService service) {
        try {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            val registeredService = WebUtils.getRegisteredService(requestContext);
            return multifactorTriggerSelectionStrategy.resolve(request, response, registeredService, authentication, service);
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        return Optional.empty();
    }
}
