package org.apereo.cas.pm.rest;

import module java.base;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordHistoryService;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.impl.BasePasswordManagementService;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * This is {@link RestPasswordManagementService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RestPasswordManagementService extends BasePasswordManagementService {

    private final RestClient restClient;

    public RestPasswordManagementService(final CipherExecutor<Serializable, String> cipherExecutor,
                                         final CasConfigurationProperties casProperties,
                                         final RestClient restClient,
                                         final PasswordHistoryService passwordHistoryService) {
        super(casProperties, cipherExecutor, passwordHistoryService);
        this.restClient = restClient;
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
        val result = restClient.post().uri(rest.getEndpointUrlChange())
            .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().toEntity(Boolean.class);
        return result.getStatusCode().value() == HttpStatus.OK.value() && result.hasBody()
            && Objects.requireNonNull(result.getBody());
    }

    @Override
    public @Nullable String findUsername(final PasswordManagementQuery query) {
        val rest = casProperties.getAuthn().getPm().getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlUser())) {
            return null;
        }
        val url = UriComponentsBuilder.fromUriString(rest.getEndpointUrlUser())
            .queryParam("email", query.getUsername()).build().toUriString();
        val result = restClient.get().uri(URI.create(url)).retrieve().toEntity(String.class);

        if (result.getStatusCode().value() == HttpStatus.OK.value() && result.hasBody()) {
            return result.getBody();
        }
        return null;
    }

    @Override
    public Set<String> findEmails(final PasswordManagementQuery query) {
        val rest = casProperties.getAuthn().getPm().getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlEmail())) {
            return Set.of();
        }

        val url = UriComponentsBuilder.fromUriString(rest.getEndpointUrlEmail())
            .queryParam("username", query.getUsername()).build().toUriString();
        val result = restClient.get().uri(URI.create(url)).retrieve().toEntity(String.class);

        if (result.getStatusCode().value() == HttpStatus.OK.value() && result.hasBody()
            && StringUtils.isNotBlank(result.getBody())) {
            return Set.of(result.getBody());
        }
        return Set.of();
    }

    @Override
    public @Nullable String findPhone(final PasswordManagementQuery query) {
        val rest = casProperties.getAuthn().getPm().getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlPhone())) {
            return null;
        }
        val url = UriComponentsBuilder.fromUriString(rest.getEndpointUrlPhone())
            .queryParam("username", query.getUsername()).build().toUriString();
        val result = restClient.get().uri(URI.create(url)).retrieve().toEntity(String.class);

        if (result.getStatusCode().value() == HttpStatus.OK.value() && result.hasBody()) {
            return result.getBody();
        }
        return null;
    }

    @Override
    public Map<String, String> getSecurityQuestions(final PasswordManagementQuery query) {
        val rest = casProperties.getAuthn().getPm().getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlSecurityQuestions())) {
            return Map.of();
        }

        val url = UriComponentsBuilder.fromUriString(rest.getEndpointUrlSecurityQuestions())
            .queryParam("username", query.getUsername()).build().toUriString();
        val result = restClient.get().uri(URI.create(url)).retrieve().toEntity(Map.class);

        if (result.getStatusCode().value() == HttpStatus.OK.value() && result.hasBody()) {
            return Objects.requireNonNull(result.getBody());
        }
        return Map.of();
    }

    @Override
    public void updateSecurityQuestions(final PasswordManagementQuery query) {
        val rest = casProperties.getAuthn().getPm().getRest();
        if (StringUtils.isNotBlank(rest.getEndpointUrlSecurityQuestions())) {
            val url = UriComponentsBuilder.fromUriString(rest.getEndpointUrlSecurityQuestions())
                .queryParam("username", query.getUsername()).build().toUriString();
            restClient.post().uri(url)
                .headers(headers -> headers.addAll(new HttpHeaders(query.getSecurityQuestions())))
                .retrieve().toEntity(Boolean.class);
        }
    }

    @Override
    public boolean unlockAccount(final Credential credential) {
        val rest = casProperties.getAuthn().getPm().getRest();
        var result = true;
        if (StringUtils.isNotBlank(rest.getEndpointUrlAccountUnlock())) {
            val url = UriComponentsBuilder.fromUriString(rest.getEndpointUrlAccountUnlock())
                .queryParam("username", credential.getId()).build().toUriString();
            result = restClient.post().uri(URI.create(url)).retrieve().toEntity(Boolean.class).getStatusCode().is2xxSuccessful();
        }
        return result;
    }
}
