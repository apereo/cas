package org.apereo.cas.authentication.credential;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.CredentialMetaData;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.ToString;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.validation.ValidationContext;

/**
 * Base class for CAS credentials that are safe for long-term storage.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@ToString
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public abstract class AbstractCredential implements Credential, CredentialMetaData {

    /**
     * Serialization version marker.
     */
    private static final long serialVersionUID = 8196868021183513898L;

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Credential)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        val builder = new EqualsBuilder();
        builder.append(getId(), ((Credential) other).getId());
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        val builder = new HashCodeBuilder(11, 41);
        builder.append(getClass().getName());
        builder.append(getId());
        return builder.toHashCode();
    }

    @JsonIgnore
    @Override
    public Class<? extends Credential> getCredentialClass() {
        return this.getClass();
    }

    @JsonIgnore
    public boolean isValid() {
        return StringUtils.isNotBlank(getId());
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
