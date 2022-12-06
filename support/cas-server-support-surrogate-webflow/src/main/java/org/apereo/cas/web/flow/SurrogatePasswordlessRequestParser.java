package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessRequest;
import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialParser;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link SurrogatePasswordlessRequestParser}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class SurrogatePasswordlessRequestParser implements PasswordlessRequestParser {

    /**
     * Request property that indicates the surrogate username, if any.
     */
    public static final String PROPORTY_SURROGATE_USERNAME = "surrogateUsername";

    private final SurrogateCredentialParser surrogateCredentialParser;

    @Override
    public PasswordlessRequest parse(final String username) {
        val credential = new BasicIdentifiableCredential(username);
        val result = surrogateCredentialParser.parse(credential);
        if (result.isPresent()) {
            val sr = result.get();
            return PasswordlessRequest.builder()
                .username(sr.getUsername())
                .properties(CollectionUtils.wrap(PROPORTY_SURROGATE_USERNAME, sr.getSurrogateUsername()))
                .build();
        }
        return PasswordlessRequest.builder().username(username).build();
    }
}
