package org.apereo.cas.acct.webflow;

import module java.base;
import org.apereo.cas.acct.AccountRegistrationService;
import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link FinalizeAccountRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public class FinalizeAccountRegistrationAction extends BaseCasWebflowAction {
    private final AccountRegistrationService accountRegistrationService;
    private final AuthenticationSystemSupport authenticationSystemSupport;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        try {
            val registrationRequest = AccountRegistrationUtils.getAccountRegistrationRequest(requestContext);
            Objects.requireNonNull(registrationRequest).putProperties(requestContext.getRequestParameters().asAttributeMap().asMap());
            val response = accountRegistrationService.getAccountRegistrationProvisioner().provision(registrationRequest);
            if (response.isSuccess()) {
                val principal = PrincipalFactoryUtils.newPrincipalFactory()
                    .createPrincipal(Objects.requireNonNull(registrationRequest.getUsername()));
                val authentication = DefaultAuthenticationBuilder.newInstance()
                    .setPrincipal(principal)
                    .build();
                val builder = authenticationSystemSupport.establishAuthenticationContextFromInitial(authentication);
                WebUtils.putAuthenticationResultBuilder(builder, requestContext);
                return success(response);
            }
        } catch (final Throwable e) {
            LoggingUtils.error(LOGGER, e);
        }
        WebUtils.addErrorMessageToContext(requestContext, "cas.screen.acct.error.provision");
        return error();
    }
}
