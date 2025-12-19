package org.apereo.cas.web.flow.passwordless;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialParser;
import org.apereo.cas.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link SurrogatePasswordlessAuthenticationRequestParser}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class SurrogatePasswordlessAuthenticationRequestParser implements PasswordlessRequestParser {

    /**
     * Request property that indicates the surrogate username, if any.
     */
    public static final String PROPERTY_SURROGATE_USERNAME = "surrogateUsername";

    private final SurrogateCredentialParser surrogateCredentialParser;

    @Override
    public PasswordlessAuthenticationRequest parse(final String username) {
        val credential = new BasicIdentifiableCredential(username);
        val result = surrogateCredentialParser.parse(credential);
        if (result.isPresent()) {
            val sr = result.get();
            return PasswordlessAuthenticationRequest
                .builder()
                .providedUsername(username)
                .username(sr.getUsername())
                .properties(CollectionUtils.wrap(PROPERTY_SURROGATE_USERNAME, sr.getSurrogateUsername()))
                .build();
        }
        return PasswordlessAuthenticationRequest.builder().username(username).build();
    }
}
