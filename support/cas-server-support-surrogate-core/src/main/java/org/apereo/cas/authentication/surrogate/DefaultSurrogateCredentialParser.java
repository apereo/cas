package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.MutableCredential;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Optional;

/**
 * This is {@link DefaultSurrogateCredentialParser}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultSurrogateCredentialParser implements SurrogateCredentialParser {
    private final SurrogateAuthenticationProperties properties;

    @Override
    public Optional<SurrogateAuthenticationRequest> parse(final MutableCredential credential) {
        if (credential != null) {
            val separator = properties.getCore().getSeparator();
            if (credential.getId().contains(separator)) {
                val givenUserName = credential.getId();
                val surrogateUsername = givenUserName.substring(0, givenUserName.indexOf(separator));
                val primaryUserName = givenUserName.substring(givenUserName.indexOf(separator) + separator.length());
                LOGGER.debug("Converting to surrogate credential for username [{}], surrogate username [{}]", primaryUserName, surrogateUsername);
                return Optional.of(SurrogateAuthenticationRequest.builder()
                    .surrogateUsername(surrogateUsername)
                    .username(primaryUserName)
                    .credential(credential)
                    .selectable(credential.getId().startsWith(separator))
                    .build());
            }
        }
        LOGGER.debug("Credential is undefined or does not indicate a surrogate authentication request");
        return Optional.empty();
    }
}
