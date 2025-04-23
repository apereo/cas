package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MutableCredential;
import org.apereo.cas.authentication.SurrogateAuthenticationPrincipalBuilder;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link LoadSurrogatesListAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class LoadSurrogatesListAction extends BaseCasWebflowAction {

    private final SurrogateAuthenticationService surrogateService;

    private final SurrogateAuthenticationPrincipalBuilder surrogatePrincipalBuilder;

    private boolean loadSurrogates(final RequestContext requestContext) throws Throwable {
        val credential = WebUtils.getCredential(requestContext, Credential.class);
        if (credential instanceof final MutableCredential mc) {
            val username = mc.getId();
            LOGGER.debug("Loading eligible accounts for [{}] to proxy", username);
            val service = Optional.ofNullable(WebUtils.getService(requestContext));
            val surrogates = surrogateService.getImpersonationAccounts(username, service)
                .parallelStream()
                .filter(StringUtils::isNotBlank)
                .sorted()
                .distinct()
                .collect(Collectors.toList());
            LOGGER.debug("Surrogate accounts found are [{}]", surrogates);
            if (!surrogates.isEmpty()) {
                if (!surrogates.contains(username) && !surrogateService.isWildcardedAccount(surrogates, service)) {
                    surrogates.addFirst(username);
                }
                WebUtils.putSurrogateAuthenticationAccounts(requestContext, surrogates);
                return true;
            }
            LOGGER.debug("No surrogate accounts could be located for [{}]", username);
        } else {
            LOGGER.debug("Credential is not supported for surrogate authentication");
        }
        return false;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        try {
            if (WebUtils.hasSurrogateAuthenticationRequest(requestContext)) {
                WebUtils.removeSurrogateAuthenticationRequest(requestContext);
                return loadSurrogateAccounts(requestContext);
            }

            val currentCredential = WebUtils.getCredential(requestContext, Credential.class);
            if (currentCredential instanceof final MutableCredential mc
                && mc.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class)
                .stream()
                .anyMatch(trait -> StringUtils.isNotBlank(trait.getSurrogateUsername()))) {
                val authenticationResultBuilder = WebUtils.getAuthenticationResultBuilder(requestContext);
                val registeredService = WebUtils.getRegisteredService(requestContext);
                val result = surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(
                    authenticationResultBuilder, currentCredential, registeredService);
                result.ifPresent(builder -> WebUtils.putAuthenticationResultBuilder(builder, requestContext));
            }
            return success();
        } catch (final Throwable e) {
            WebUtils.addErrorMessageToContext(requestContext,
                "screen.surrogates.account.selection.error",
                "Unable to accept or authorize selection");
            LoggingUtils.error(LOGGER, e);
            return error(new RuntimeException(e));
        }
    }

    protected Event loadSurrogateAccounts(final RequestContext requestContext) throws Throwable {
        LOGGER.trace("Attempting to load surrogates...");
        val eventFactorySupport = eventFactory;
        if (loadSurrogates(requestContext)) {
            val accounts = WebUtils.getSurrogateAuthenticationAccounts(requestContext);
            val service = Optional.ofNullable(WebUtils.getService(requestContext));
            if (surrogateService.isWildcardedAccount(accounts, service)) {
                return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_SURROGATE_WILDCARD_VIEW);
            }
            return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_SURROGATE_VIEW);
        }
        return eventFactorySupport.event(this, CasWebflowConstants.TRANSITION_ID_SKIP_SURROGATE);
    }
}
