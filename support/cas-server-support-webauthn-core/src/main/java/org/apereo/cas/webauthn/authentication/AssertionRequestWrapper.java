package org.apereo.cas.webauthn.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * This is {@link AssertionRequestWrapper}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Getter
public class AssertionRequestWrapper {
    private final ByteArray requestId;

    @JsonIgnore
    private final transient AssertionRequest request;

    public PublicKeyCredentialRequestOptions getPublicKeyCredentialRequestOptions() {
        return request.getPublicKeyCredentialRequestOptions();
    }

    public Optional<String> getUsername() {
        return request.getUsername();
    }
}
