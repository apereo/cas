package org.apereo.cas.authentication.credential;

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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Credential for authenticating with a username and password.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
@ToString(exclude = "password")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UsernamePasswordCredential extends AbstractCredential {
    /**
     * Authentication attribute name for password.
     **/
    public static final String AUTHENTICATION_ATTRIBUTE_PASSWORD = "credential";

    private static final int MAP_SIZE = 8;

    private static final long serialVersionUID = -700605081472810939L;

    private @Size(min = 1, message = "username.required") String username;

    private @Size(min = 1, message = "password.required") String password;

    private String source;

    private Map<String, Object> customFields = new LinkedHashMap<>(MAP_SIZE);

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
    @Override
    public void validate(final ValidationContext context) {
        val messages = context.getMessageContext();
        if (!context.getUserEvent().equalsIgnoreCase("submit") || messages.hasErrorMessages()) {
            return;
        }

        ApplicationContextProvider.getCasConfigurationProperties().ifPresent(props -> {
            if (StringUtils.isBlank(source) && props.getAuthn().getPolicy().isSourceSelectionEnabled()) {
                messages.addMessage(new MessageBuilder()
                    .error()
                    .source("source")
                    .defaultText("Required Source")
                    .code("source.required")
                    .build());
            }
        });
        super.validate(context);
    }
}
