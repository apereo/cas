package org.apereo.cas.pm.rest;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.impl.BasePasswordManagementService;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
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
                                         final CasConfigurationProperties casProperties,
                                         final RestTemplate restTemplate,
                                         final PasswordHistoryService passwordHistoryService) {
        super(casProperties, cipherExecutor, passwordHistoryService);
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean changeInternal(final PasswordChangeRequest bean) {
        val rest = casProperties.getAuthn().getPm().getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlChange())) {
            return false;
        }

        val body = new HashMap<>();
        body.put(rest.getFieldNameUser(), bean.getUsername());
        body.put(rest.getFieldNamePassword(), bean.toPassword());
        if (bean.getCurrentPassword() != null) {
            body.put(rest.getFieldNamePasswordOld(), bean.toCurrentPassword());
        }
        val headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        val entity = new HttpEntity<>(body, headers);
        val result = restTemplate.exchange(rest.getEndpointUrlChange(), HttpMethod.POST, entity, Boolean.class);
        return result.getStatusCode().value() == HttpStatus.OK.value() && result.hasBody()
            && Objects.requireNonNull(result.getBody());
    }

    @Override
    public String findUsername(final PasswordManagementQuery query) {
        val rest = casProperties.getAuthn().getPm().getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlUser())) {
            return null;
        }
        val url = UriComponentsBuilder.fromUriString(rest.getEndpointUrlUser())
            .queryParam("email", query.getUsername()).build().toUriString();
        val request = new RequestEntity<>(HttpMethod.GET, URI.create(url));
        val result = restTemplate.exchange(request, String.class);

        if (result.getStatusCode().value() == HttpStatus.OK.value() && result.hasBody()) {
            return result.getBody();
        }
        return null;
    }

    @Override
    public String findEmail(final PasswordManagementQuery query) {
        val rest = casProperties.getAuthn().getPm().getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlEmail())) {
            return null;
        }

        val url = UriComponentsBuilder.fromUriString(rest.getEndpointUrlEmail())
            .queryParam("username", query.getUsername()).build().toUriString();
        val request = new RequestEntity<>(HttpMethod.GET, URI.create(url));
        val result = restTemplate.exchange(request, String.class);

        if (result.getStatusCode().value() == HttpStatus.OK.value() && result.hasBody()) {
            return result.getBody();
        }
        return null;
    }

    @Override
    public String findPhone(final PasswordManagementQuery query) {
        val rest = casProperties.getAuthn().getPm().getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlPhone())) {
            return null;
        }
        val url = UriComponentsBuilder.fromUriString(rest.getEndpointUrlPhone())
            .queryParam("username", query.getUsername()).build().toUriString();
        val request = new RequestEntity<>(HttpMethod.GET, URI.create(url));
        val result = restTemplate.exchange(request, String.class);

        if (result.getStatusCode().value() == HttpStatus.OK.value() && result.hasBody()) {
            return result.getBody();
        }
        return null;
    }

    @Override
    public Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) {
        val rest = casProperties.getAuthn().getPm().getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlSecurityQuestions())) {
            return null;
        }

        val url = UriComponentsBuilder.fromUriString(rest.getEndpointUrlSecurityQuestions())
            .queryParam("username", query.getUsername()).build().toUriString();
        val request = new RequestEntity<>(HttpMethod.GET, URI.create(url));
        val result = restTemplate.exchange(request, Map.class);

        if (result.getStatusCode().value() == HttpStatus.OK.value() && result.hasBody()) {
            return result.getBody();
        }
        return null;
    }

    @Override
    public void updateSecurityQuestions(final PasswordManagementQuery query) {
        val rest = casProperties.getAuthn().getPm().getRest();
        if (StringUtils.isNotBlank(rest.getEndpointUrlSecurityQuestions())) {
            val url = UriComponentsBuilder.fromUriString(rest.getEndpointUrlSecurityQuestions())
                .queryParam("username", query.getUsername()).build().toUriString();
            val entity = new HttpEntity<>(query.getSecurityQuestions());
            restTemplate.exchange(url, HttpMethod.POST, entity, Boolean.class);
        }
    }

    @Override
    public boolean unlockAccount(final Credential credential) {
        val rest = casProperties.getAuthn().getPm().getRest();
        var result = true;
        if (StringUtils.isNotBlank(rest.getEndpointUrlAccountUnlock())) {
            val url = UriComponentsBuilder.fromUriString(rest.getEndpointUrlAccountUnlock())
                .queryParam("username", credential.getId()).build().toUriString();
            val request = new RequestEntity<>(HttpMethod.POST, URI.create(url));
            result = restTemplate.exchange(request, Boolean.class).getStatusCode().is2xxSuccessful();
        }
        return result;
    }
}
