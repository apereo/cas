package org.apereo.cas.web;

import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

/**
 * This is {@link PasswordValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PasswordValidator {

    private String passwordPolicyPattern;

    public PasswordValidator(final String passwordPolicyPattern) {
        this.passwordPolicyPattern = passwordPolicyPattern;
    }

    /**
     * Validate cas must change pass view.
     *
     * @param bean    the bean
     * @param context the context
     */
    public void validateCasMustChangePassView(final PasswordChangeBean bean, final ValidationContext context) {
        final MessageContext messages = context.getMessageContext();
        if (!bean.getPassword().equals(bean.getConfirmedPassword())) {
            messages.addMessage(new MessageBuilder().error().source("pm.passwordsMustMatch").
                    defaultText("Provided passwords do not match.").build());
            return;
        }

        if (!bean.getPassword().matches(this.passwordPolicyPattern)) {
            messages.addMessage(new MessageBuilder().error().source("pm.passwordFailedCriteria").
                    defaultText("Password policy rejected the provided insecure password.").build());
            return;
        }

    }
}
