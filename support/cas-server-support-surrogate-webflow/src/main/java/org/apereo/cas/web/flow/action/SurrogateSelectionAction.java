package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.SurrogatePrincipalBuilder;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link SurrogateSelectionAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SurrogateSelectionAction extends AbstractAction {
    /**
     * Surrogate Target parameter name.
     */
    public static final String PARAMETER_NAME_SURROGATE_TARGET = "surrogateTarget";

    private final SurrogatePrincipalBuilder surrogatePrincipalBuilder;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            final Credential credential = WebUtils.getCredential(requestContext);
            if (credential instanceof UsernamePasswordCredential) {
                final String target = requestContext.getExternalContext().getRequestParameterMap().get("surrogateTarget");
                LOGGER.debug("Located surrogate target as [{}]", target);
                if (StringUtils.isNotBlank(target)) {
                    final AuthenticationResultBuilder authenticationResultBuilder = WebUtils.getAuthenticationResultBuilder(requestContext);
                    final Optional<AuthenticationResultBuilder> result =
                        surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(authenticationResultBuilder, credential, target);
                    surrogatePrincipalBuilder.buildSurrogateAuthenticationResult(authenticationResultBuilder, credential, target);
                    result.ifPresent(builder -> WebUtils.putAuthenticationResultBuilder(builder, requestContext));
                } else {
                    LOGGER.warn("No surrogate identifier was selected or provided");
                }
            } else {
                LOGGER.debug("Current credential in the webflow is not one of [{}]", UsernamePasswordCredential.class.getName());
            }
            return success();
        } catch (final Exception e) {
            requestContext.getMessageContext().addMessage(new MessageBuilder()
                .error()
                .source("surrogate")
                .code("screen.surrogates.account.selection.error")
                .defaultText("Unable to accept or authorize selection")
                .build());
            LOGGER.error(e.getMessage(), e);
        }
        return error();
    }
}
