package org.apereo.cas.webauthn.authentication;

import org.apereo.cas.webauthn.credential.WebAuthnCredentialRegistration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;

/**
 * This is {@link SuccessfulAuthenticationResult}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@AllArgsConstructor
public class SuccessfulAuthenticationResult {
    private AssertionRequestWrapper request;
    private AssertionResponse response;
    private Collection<WebAuthnCredentialRegistration> registrations;
    private List<String> warnings;
}
