package org.apereo.cas.pm.rest;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link RestPasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RestPasswordManagementService extends BasePasswordManagementService {

    private final RestTemplate restTemplate;

    public RestPasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                         final String issuer,
                                         final RestTemplate restTemplate,
                                         final PasswordManagementProperties passwordManagementProperties,
                                         final PasswordHistoryService passwordHistoryService) {
        super(passwordManagementProperties, cipherExecutor, issuer, passwordHistoryService);
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean changeInternal(final Credential c, final PasswordChangeRequest bean) {
        val rest = properties.getRest();
        
        if (StringUtils.isBlank(rest.getEndpointUrlChange())) {
            return false;
        }

        val upc = (UsernamePasswordCredential) c;

        val headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        headers.put("username", CollectionUtils.wrap(upc.getUsername()));
        headers.put("password", CollectionUtils.wrap(bean.getPassword()));
        headers.put("oldPassword", CollectionUtils.wrap(upc.getPassword()));

        val entity = new HttpEntity<>(headers);
        val result = restTemplate.exchange(rest.getEndpointUrlChange(), HttpMethod.POST, entity, Boolean.class);
        if (result.getStatusCodeValue() == HttpStatus.OK.value() && result.hasBody()) {
            return Objects.requireNonNull(result.getBody()).booleanValue();
        }
        return false;
    }

    @Override
    public String findUsername(final PasswordManagementQuery query) {
        val rest = properties.getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlUser())) {
            return null;
        }

        val headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        headers.put("email", CollectionUtils.wrap(query.getUsername()));
        val entity = new HttpEntity<>(headers);
        val result = restTemplate.exchange(rest.getEndpointUrlUser(), HttpMethod.GET, entity, String.class);

        if (result.getStatusCodeValue() == HttpStatus.OK.value() && result.hasBody()) {
            return result.getBody();
        }
        return null;
    }

    @Override
    public String findEmail(final PasswordManagementQuery query) {
        val rest = properties.getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlEmail())) {
            return null;
        }

        val headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        headers.put("username", CollectionUtils.wrap(query.getUsername()));
        val entity = new HttpEntity<>(headers);
        val result = restTemplate.exchange(rest.getEndpointUrlEmail(), HttpMethod.GET, entity, String.class);

        if (result.getStatusCodeValue() == HttpStatus.OK.value() && result.hasBody()) {
            return result.getBody();
        }
        return null;
    }

    @Override
    public String findPhone(final PasswordManagementQuery query) {
        val rest = properties.getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlPhone())) {
            return null;
        }

        val headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        headers.put("username", CollectionUtils.wrap(query.getUsername()));
        val entity = new HttpEntity<>(headers);
        val result = restTemplate.exchange(rest.getEndpointUrlPhone(), HttpMethod.GET, entity, String.class);

        if (result.getStatusCodeValue() == HttpStatus.OK.value() && result.hasBody()) {
            return result.getBody();
        }
        return null;
    }

    @Override
    public Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) {
        val rest = properties.getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlSecurityQuestions())) {
            return null;
        }
        val headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        headers.put("username", CollectionUtils.wrap(query.getUsername()));
        val entity = new HttpEntity<>(headers);
        val result = restTemplate.exchange(rest.getEndpointUrlSecurityQuestions(),
            HttpMethod.GET, entity, Map.class);

        if (result.getStatusCodeValue() == HttpStatus.OK.value() && result.hasBody()) {
            return result.getBody();
        }
        return null;
    }
}
