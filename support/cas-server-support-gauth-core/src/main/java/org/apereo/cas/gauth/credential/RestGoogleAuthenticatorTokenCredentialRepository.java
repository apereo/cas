package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.model.support.mfa.GoogleAuthenticatorMultifactorProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link RestGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
public class RestGoogleAuthenticatorTokenCredentialRepository extends BaseGoogleAuthenticatorTokenCredentialRepository {

    private final GoogleAuthenticatorMultifactorProperties gauth;

    public RestGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
                                                            final GoogleAuthenticatorMultifactorProperties gauth,
                                                            final CipherExecutor<String, String> tokenCredentialCipher) {
        super(tokenCredentialCipher, googleAuthenticator);
        this.gauth = gauth;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        val rest = gauth.getRest();
        val headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));

        val entity = new HttpEntity<Object>(headers);
        val restTemplate = new RestTemplate();
        val result = restTemplate.exchange(rest.getEndpointUrl(), HttpMethod.GET, entity, List.class);
        if (result.getStatusCodeValue() == HttpStatus.OK.value()) {
            val results = (List<GoogleAuthenticatorAccount>) result.getBody();
            if (results != null) {
                return results.stream().map(this::decode).collect(Collectors.toList());
            }
        }
        return new ArrayList<>(0);
    }

    @Override
    public OneTimeTokenAccount get(final String username) {
        val rest = gauth.getRest();
        val headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        headers.put("username", CollectionUtils.wrap(username));

        val entity = new HttpEntity<Object>(headers);
        val restTemplate = new RestTemplate();
        val result = restTemplate.exchange(rest.getEndpointUrl(), HttpMethod.GET, entity, OneTimeTokenAccount.class);
        if (result.getStatusCodeValue() == HttpStatus.OK.value()) {
            return decode(Objects.requireNonNull(result.getBody()));
        }
        return null;
    }

    @Override
    public void save(final String userName, final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        val account = new GoogleAuthenticatorAccount(userName, secretKey, validationCode, scratchCodes);
        update(account);
    }

    @Override
    public void deleteAll() {
        val rest = gauth.getRest();
        val restTemplate = new RestTemplate();
        restTemplate.delete(rest.getEndpointUrl());
    }

    @Override
    public void delete(final String username) {
        val rest = gauth.getRest();
        val headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        headers.put("username", CollectionUtils.wrap(username));
        val entity = new HttpEntity<Object>(headers);
        val restTemplate = new RestTemplate();
        restTemplate.exchange(rest.getEndpointUrl(), HttpMethod.DELETE, entity, Long.class);
    }

    @Override
    public long count() {
        val rest = gauth.getRest();
        val restTemplate = new RestTemplate();
        val countUrl = StringUtils.appendIfMissing(rest.getEndpointUrl(), "/").concat("count");
        val value = restTemplate.getForObject(countUrl, Long.class);
        return Objects.requireNonNull(value).longValue();
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount accountToUpdate) {
        val rest = gauth.getRest();
        val account = encode(accountToUpdate);

        val headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        headers.put("username", CollectionUtils.wrap(account.getUsername()));
        headers.put("validationCode", CollectionUtils.wrap(String.valueOf(account.getValidationCode())));
        headers.put("secretKey", CollectionUtils.wrap(account.getSecretKey()));
        headers.put("scratchCodes", account.getScratchCodes().stream().map(String::valueOf).collect(Collectors.toList()));

        val entity = new HttpEntity<Object>(headers);
        val restTemplate = new RestTemplate();
        val result = restTemplate.exchange(rest.getEndpointUrl(), HttpMethod.POST, entity, Object.class);
        if (result.getStatusCodeValue() == HttpStatus.OK.value()) {
            LOGGER.debug("Posted google authenticator account successfully");
            return account;
        }
        LOGGER.warn("Failed to save google authenticator account successfully");
        return null;
    }
}
