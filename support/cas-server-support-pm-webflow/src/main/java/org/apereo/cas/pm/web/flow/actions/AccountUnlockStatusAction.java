package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.security.auth.login.AccountLockedException;

/**
 * This is {@link AccountUnlockStatusAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@RequiredArgsConstructor
public class AccountUnlockStatusAction extends BaseCasWebflowAction {

    private final PasswordManagementService passwordManagementService;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        try {
            val credential = requestContext.getConversationScope().get(Credential.class.getName(), Credential.class);
            LOGGER.debug("Attempting to unlock account for [{}]", credential);
            val givenValue = requestContext.getConversationScope().get("captchaValue", String.class);
            val providedValue = WebUtils.getRequestParameterOrAttribute(requestContext, "captchaValue").orElseThrow();
            LOGGER.debug("Comparing captcha value [{}] with user entry [{}]", givenValue, providedValue);
            if (!givenValue.equals(providedValue) || !passwordManagementService.unlockAccount(credential)) {
                throw new AccountLockedException("Captcha value does not match, or CAS cannot unlock the account for " + credential.getId());
            }
            WebUtils.addInfoMessageToContext(requestContext, "screen.account.unlock.success");
            return success();
        } catch (final Throwable e) {
            WebUtils.addErrorMessageToContext(requestContext, "screen.account.unlock.fail");
            LoggingUtils.error(LOGGER, e);
            return error();
        }
    }
}
