package org.apereo.cas.pm.rest;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.apereo.cas.pm.BasePasswordManagementService;
import org.apereo.cas.pm.PasswordChangeBean;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.Map;

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
                                         final PasswordManagementProperties passwordManagementProperties) {
        super(cipherExecutor, issuer, passwordManagementProperties);
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean changeInternal(final Credential c, final PasswordChangeBean bean) {
        final PasswordManagementProperties.Rest rest = properties.getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlChange())) {
            return false;
        }

        final UsernamePasswordCredential upc = (UsernamePasswordCredential) c;

        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        headers.put("username", CollectionUtils.wrap(upc.getUsername()));
        headers.put("password", CollectionUtils.wrap(bean.getPassword()));
        headers.put("oldPassword", CollectionUtils.wrap(upc.getPassword()));

        final HttpEntity<String> entity = new HttpEntity<>(headers);
        final ResponseEntity<Boolean> result = restTemplate.exchange(rest.getEndpointUrlChange(), HttpMethod.POST, entity, Boolean.class);
        if (result.getStatusCodeValue() == HttpStatus.OK.value()) {
            return result.getBody();
        }
        return false;
    }

    @Override
    public String findEmail(final String username) {
        final PasswordManagementProperties.Rest rest = properties.getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlEmail())) {
            return null;
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        headers.put("username", CollectionUtils.wrap(username));
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        final ResponseEntity<String> result = restTemplate.exchange(rest.getEndpointUrlEmail(), HttpMethod.GET, entity, String.class);

        if (result.getStatusCodeValue() == HttpStatus.OK.value() && result.hasBody()) {
            return result.getBody();
        }
        return null;
    }

    @Override
    public Map<String, String> getSecurityQuestions(final String username) {
        final PasswordManagementProperties.Rest rest = properties.getRest();
        if (StringUtils.isBlank(rest.getEndpointUrlSecurityQuestions())) {
            return null;
        }
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        headers.put("username", CollectionUtils.wrap(username));
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        final ResponseEntity<Map> result = restTemplate.exchange(rest.getEndpointUrlSecurityQuestions(),
                HttpMethod.GET, entity, Map.class);

        if (result.getStatusCodeValue() == HttpStatus.OK.value() && result.hasBody()) {
            return result.getBody();
        }
        return null;
    }
}
