package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationProvider;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Objects;

/**
 * This is {@link CasSimpleMultifactorValidateTokenAction}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class CasSimpleMultifactorValidateTokenAction extends AbstractMultifactorAuthenticationAction<CasSimpleMultifactorAuthenticationProvider> {
    private final CasSimpleMultifactorAuthenticationService casSimpleMultifactorAuthenticationService;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val authentication = Objects.requireNonNull(WebUtils.getInProgressAuthentication());
        val credential = (CasSimpleMultifactorTokenCredential) WebUtils.getCredential(requestContext);
        val credentialPrincipal = FunctionUtils.doAndHandle(() -> casSimpleMultifactorAuthenticationService.fetch(credential));
        if (credentialPrincipal == null || !credentialPrincipal.equals(authentication.getPrincipal())) {
            WebUtils.addErrorMessageToContext(requestContext, "cas.mfa.simple.token.failed");
            return eventFactory.error(this);
        }
        return null;
    }
}
