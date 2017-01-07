package org.apereo.cas.pm;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private CasConfigurationProperties casProperties;
    
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

        if (!bean.getPassword().matches(casProperties.getAuthn().getPm().getPolicyPattern())) {
            messages.addMessage(new MessageBuilder().error().source("pm.passwordFailedCriteria").
                    defaultText("Password policy rejected the provided insecure password.").build());
        }
    }
}
