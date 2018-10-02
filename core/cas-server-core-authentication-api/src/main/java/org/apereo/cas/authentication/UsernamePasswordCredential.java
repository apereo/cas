package org.apereo.cas.authentication;

import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.validation.ValidationContext;

import javax.validation.constraints.Size;

/**
 * Credential for authenticating with a username and password.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@ToString(exclude = {"password"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UsernamePasswordCredential implements Credential {
    /**
     * Authentication attribute name for password.
     **/
    public static final String AUTHENTICATION_ATTRIBUTE_PASSWORD = "credential";

    private static final long serialVersionUID = -700605081472810939L;

    @Size(min = 1, message = "username.required")
    private String username;

    @Size(min = 1, message = "password.required")
    private String password;

    private String source;

    public UsernamePasswordCredential(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String getId() {
        return this.username;
    }

    /**
     * Validate.
     *
     * @param context the context
     */
    public void validate(final ValidationContext context) {
        if (!context.getUserEvent().equalsIgnoreCase("submit")) {
            return;
        }

        val messages = context.getMessageContext();
        if (StringUtils.isBlank(username)) {
            messages.addMessage(new MessageBuilder()
                .error()
                .source("username")
                .code("username.required")
                .build());
        }
        if (StringUtils.isBlank(password)) {
            messages.addMessage(new MessageBuilder()
                .error()
                .source("password")
                .code("password.required")
                .build());
        }
        val casProperties = ApplicationContextProvider.getCasProperties();
        if (StringUtils.isBlank(source) && casProperties.getAuthn().getPolicy().isSourceSelectionEnabled()) {
            messages.addMessage(new MessageBuilder()
                .error()
                .source("source")
                .code("source.required")
                .build());
        }
    }
}
