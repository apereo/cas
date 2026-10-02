package org.apereo.cas.adaptors.duo.authn;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationCredential;
import org.apereo.cas.authentication.credential.AbstractCredential;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link DuoSecurityUniversalPromptCredential}.
 * The first-factor authentication is only needed while the Duo Security response is verified,
 * to find the principal; it is not serialized, so it is not kept a second time inside the ticket-granting ticket.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ToString
@Setter
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = "token", callSuper = true)
public class DuoSecurityUniversalPromptCredential extends AbstractCredential implements MultifactorAuthenticationCredential {
    @Serial
    private static final long serialVersionUID = -7571699733132111037L;

    private final String token;

    @JsonIgnore
    @ToString.Exclude
    private final transient @Nullable Authentication authentication;

    private String providerId;

    @JsonCreator
    public DuoSecurityUniversalPromptCredential(@JsonProperty("token") final String token) {
        this(token, null);
    }

    @Override
    public String getId() {
        return this.token;
    }
}
