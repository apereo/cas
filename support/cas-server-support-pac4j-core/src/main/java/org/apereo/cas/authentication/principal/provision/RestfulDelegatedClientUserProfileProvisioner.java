package org.apereo.cas.authentication.principal.provision;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpResponse;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.UserProfile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.HashMap;

/**
 * This is {@link RestfulDelegatedClientUserProfileProvisioner}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class RestfulDelegatedClientUserProfileProvisioner extends BaseDelegatedClientUserProfileProvisioner {
    private final RestEndpointProperties restProperties;

    @Override
    public void execute(final Principal principal, final UserProfile profile, final BaseClient client) {
        HttpResponse response = null;
        try {
            val headers = new HashMap<String, Object>();
            headers.put("principalId", principal.getId());
            headers.put("principalAttributes", principal.getAttributes());
            headers.put("profileId", profile.getId());
            headers.put("profileTypedId", profile.getTypedId());
            headers.put("profileAttributes", profile.getAttributes());
            headers.put("clientName", client.getName());
            headers.putAll(restProperties.getHeaders());
            
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.valueOf(restProperties.getMethod().toUpperCase().trim()))
                .url(restProperties.getUrl())
                .headers(headers)
                .build();
            
            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    LOGGER.debug("Provisioned principal [{}] successfully", principal);
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
    }
}
