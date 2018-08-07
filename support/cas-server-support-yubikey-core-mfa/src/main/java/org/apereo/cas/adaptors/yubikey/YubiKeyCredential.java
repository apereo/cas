package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.authentication.Credential;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;

/**
 * This is {@link YubiKeyCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@ToString
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class YubiKeyCredential implements Credential {

    private static final long serialVersionUID = -7570600701132111037L;

    private String token;

    @Override
    public String getId() {
        return this.token;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.token);
    }

    /**
     * Validate.
     *
     * @param context the context
     */
    public void validate(final ValidationContext context) {
        if (!isValid()) {
            final MessageContext messages = context.getMessageContext();
            messages.addMessage(new MessageBuilder()
                .error()
                .source("token")
                .defaultText("Unable to accept credential with an empty or unspecified token")
                .build());
        }
    }
}
