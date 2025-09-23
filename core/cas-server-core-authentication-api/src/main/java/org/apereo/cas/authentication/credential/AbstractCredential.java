package org.apereo.cas.authentication.credential;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.CredentialMetadata;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.validation.ValidationContext;
import java.io.Serial;
import java.util.Objects;

/**
 * Base class for CAS credentials that are safe for long-term storage.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@ToString
@Setter
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class AbstractCredential implements Credential {
    @Serial
    private static final long serialVersionUID = 8196868021183513898L;

    private CredentialMetadata credentialMetadata;

    /**
     * Gets credential metadata. Will initialize if metadata is null.
     *
     * @return current credential metadata
     */
    @Override
    public CredentialMetadata getCredentialMetadata() {
        if (credentialMetadata == null) {
            this.credentialMetadata = new BasicCredentialMetadata(this);
        }
        return this.credentialMetadata;
    }

    @JsonIgnore
    public boolean isValid() {
        return StringUtils.isNotBlank(getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass().getName(), getId());
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Credential credential) {
            return Objects.equals(getId(), credential.getId());
        }
        return false;
    }

    /**
     * Validate.
     *
     * @param context the context
     */
    public void validate(final ValidationContext context) {
        if (!isValid()) {
            val messages = context.getMessageContext();
            messages.addMessage(new MessageBuilder()
                .error()
                .source("token")
                .defaultText("Unable to accept credential with an empty or unspecified token")
                .build());
        }
    }
}
