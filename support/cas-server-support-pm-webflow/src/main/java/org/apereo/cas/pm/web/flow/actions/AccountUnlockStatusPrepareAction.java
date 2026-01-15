package org.apereo.cas.pm.web.flow.actions;

import module java.base;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AccountUnlockStatusPrepareAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class AccountUnlockStatusPrepareAction extends BaseCasWebflowAction {
    private static final int CAPTCHA_LENGTH = 6;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        FunctionUtils.doIf(!requestContext.getCurrentEvent().getId().equals(CasWebflowConstants.TRANSITION_ID_ERROR),
            u -> requestContext.getMessageContext().clearMessages()).accept(requestContext);
        requestContext.getConversationScope().put("captchaValue", RandomUtils.randomAlphanumeric(CAPTCHA_LENGTH));
        return null;
    }
}
