package org.apereo.cas.adaptors.gauth.repository.credentials;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.otp.repository.credentials.BaseOneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link RestGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RestGoogleAuthenticatorTokenCredentialRepository extends BaseOneTimeTokenCredentialRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestGoogleAuthenticatorTokenCredentialRepository.class);
    private final IGoogleAuthenticator googleAuthenticator;

    private final RestTemplate restTemplate;
    private final MultifactorAuthenticationProperties.GAuth gauth;

    public RestGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
                                                            final RestTemplate restTemplate,
                                                            final MultifactorAuthenticationProperties.GAuth gauth) {
        this.googleAuthenticator = googleAuthenticator;
        this.restTemplate = restTemplate;
        this.gauth = gauth;
    }

    @Override
    public String getSecret(final String username) {
        final MultifactorAuthenticationProperties.GAuth.Rest rest = gauth.getRest();
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.put("username", Arrays.asList(username));

        final HttpEntity<String> entity = new HttpEntity<>(headers);
        final ResponseEntity<String> result = restTemplate.exchange(rest.getEndpointUrl(), HttpMethod.GET, entity, String.class);
        if (result.getStatusCodeValue() == HttpStatus.OK.value()) {
            return result.getBody();
        }
        return null;
    }

    @Override
    public void save(final String userName, final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        final MultifactorAuthenticationProperties.GAuth.Rest rest = gauth.getRest();
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.put("username", Arrays.asList(userName));
        headers.put("validationCode", Arrays.asList(String.valueOf(validationCode)));
        headers.put("secretKey", Arrays.asList(secretKey));
        headers.put("scratchCodes", scratchCodes.stream().map(String::valueOf).collect(Collectors.toList()));

        final HttpEntity<String> entity = new HttpEntity<>(headers);
        final ResponseEntity<Boolean> result = restTemplate.exchange(rest.getEndpointUrl(), HttpMethod.POST, entity, Boolean.class);
        if (result.getStatusCodeValue() == HttpStatus.OK.value()) {
            LOGGER.debug("Posted google authenticator account successfully");
        }
        LOGGER.warn("Failed to save google authenticator account successfully");
    }

    @Override
    public OneTimeTokenAccount create(final String username) {
        final GoogleAuthenticatorKey key = this.googleAuthenticator.createCredentials();
        return new GoogleAuthenticatorAccount(username, key.getKey(), key.getVerificationCode(), key.getScratchCodes());
    }
}
