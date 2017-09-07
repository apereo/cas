package org.apereo.cas.adaptors.gauth.repository.credentials;

import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import org.apereo.cas.configuration.model.support.mfa.GAuthMultifactorProperties;
import org.apereo.cas.otp.repository.credentials.BaseOneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenAccount;
import org.apereo.cas.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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
    private final GAuthMultifactorProperties gauth;

    public RestGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
                                                            final RestTemplate restTemplate,
                                                            final GAuthMultifactorProperties gauth) {
        this.googleAuthenticator = googleAuthenticator;
        this.restTemplate = restTemplate;
        this.gauth = gauth;
    }

    @Override
    public OneTimeTokenAccount get(final String username) {
        final GAuthMultifactorProperties.Rest rest = gauth.getRest();
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        headers.put("username", CollectionUtils.wrap(username));

        final HttpEntity<String> entity = new HttpEntity<>(headers);
        final ResponseEntity<OneTimeTokenAccount> result = restTemplate.exchange(rest.getEndpointUrl(), HttpMethod.GET, entity, OneTimeTokenAccount.class);
        if (result.getStatusCodeValue() == HttpStatus.OK.value()) {
            return result.getBody();
        }
        return null;
    }

    @Override
    public void save(final String userName, final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        final GAuthMultifactorProperties.Rest rest = gauth.getRest();
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        headers.put("username", CollectionUtils.wrap(userName));
        headers.put("validationCode", CollectionUtils.wrap(String.valueOf(validationCode)));
        headers.put("secretKey", CollectionUtils.wrap(secretKey));
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
