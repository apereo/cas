package org.apereo.cas.authentication.principal.provision;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.core5.http.HttpResponse;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.UserProfile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Locale;

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
    public void execute(final Principal principal, final UserProfile profile,
                        final BaseClient client, final Credential credential) {
        HttpResponse response = null;
        try {
            val body = new HashMap<String, Object>();
            body.put("principalId", principal.getId());
            body.put("principalAttributes", principal.getAttributes());
            body.put("profileId", profile.getId());
            body.put("profileTypedId", profile.getTypedId());
            body.put("profileAttributes", profile.getAttributes());
            body.put("clientName", client.getName());
            body.putAll(restProperties.getHeaders());

            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.valueOf(restProperties.getMethod().toUpperCase(Locale.ENGLISH).trim()))
                .url(restProperties.getUrl())
                .headers(restProperties.getHeaders())
                .build()
                .body(body);

            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getCode());
                LOGGER.debug("Provisioned principal [{}] with status result [{}]", principal.getId(), status);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
    }
}
