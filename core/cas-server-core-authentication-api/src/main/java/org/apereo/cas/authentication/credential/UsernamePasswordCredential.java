package org.apereo.cas.authentication.credential;

import org.apereo.cas.authentication.MutableCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.function.FunctionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.util.ReflectionUtils;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.validation.DefaultValidationContext;

import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

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
@EqualsAndHashCode(exclude = "password", callSuper = true)
public class UsernamePasswordCredential extends AbstractCredential implements MutableCredential {
    /**
     * Authentication attribute name for password.
     **/
    public static final String AUTHENTICATION_ATTRIBUTE_PASSWORD = "credential";

    @Serial
    private static final long serialVersionUID = -700605081472810939L;

    private @Size(min = 1, message = "username.required") String username;

    @JsonIgnore
    private @Size(min = 1, message = "password.required") char[] password;

    private String source;

    private Map<String, Object> customFields = new LinkedHashMap<>();

    public UsernamePasswordCredential(final String username, final String password) {
        this.username = username;
        assignPassword(StringUtils.defaultString(password));
    }

    public UsernamePasswordCredential(final String username, final char[] password,
                                      final String source, final Map<String, Object> customFields) {
        this.username = username;
        this.password = password.clone();
        this.source = source;
        this.customFields = new HashMap<>(customFields);
    }

    @Override
    public String getId() {
        return this.username;
    }

    @Override
    public void setId(final String id) {
        this.username = id;
    }

    @Override
    public void validate(final ValidationContext context) {
        val messageContext = context.getMessageContext();
        if (!"submit".equalsIgnoreCase(context.getUserEvent()) || messageContext.hasErrorMessages()) {
            return;
        }

        val field = ReflectionUtils.findField(DefaultValidationContext.class, "requestContext");
        Objects.requireNonNull(field).trySetAccessible();
        val requestContext = (RequestContext) ReflectionUtils.getField(field, context);
        val props = requestContext.getActiveFlow().getApplicationContext().getBean(CasConfigurationProperties.class);
        
        if (StringUtils.isBlank(source) && props.getAuthn().getPolicy().isSourceSelectionEnabled()) {
            messageContext.addMessage(new MessageBuilder()
                .error()
                .source("source")
                .defaultText("Required Source")
                .code("source.required")
                .build());
        }
        super.validate(context);
    }

    /**
     * Convert to string-friendly password.
     *
     * @return the string
     */
    public String toPassword() {
        return FunctionUtils.doIfNull(this.password, () -> null, () -> new String(this.password)).get();
    }

    /**
     * Sets password and converts it to char array.
     *
     * @param password the password
     */
    public void assignPassword(final String password) {
        FunctionUtils.doIfNotNull(password, p -> {
            this.password = new char[p.length()];
            System.arraycopy(password.toCharArray(), 0, this.password, 0, password.length());
        }, p -> this.password = ArrayUtils.EMPTY_CHAR_ARRAY);
    }


}
