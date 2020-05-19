package org.apereo.cas.authentication.principal.provision;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.util.HttpUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpResponse;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.profile.CommonProfile;
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
    public void execute(final Principal principal, final CommonProfile profile, final BaseClient client) {
        HttpResponse response = null;
        try {
            val headers = new HashMap<String, Object>();
            headers.put("principalId", principal.getId());
            headers.put("principalAttributes", principal.getAttributes());
            headers.put("profileId", profile.getId());
            headers.put("profileTypedId", profile.getTypedId());
            headers.put("profileAttributes", profile.getAttributes());
            headers.put("authenticationAttributes", profile.getAuthenticationAttributes());
            headers.put("clientName", client.getName());

            response = HttpUtils.execute(restProperties.getUrl(), HttpMethod.GET.name(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword(), headers);

            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (status.is2xxSuccessful()) {
                    LOGGER.debug("Provisioned principal [{}] successfully", principal);
                }
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        } finally {
            HttpUtils.close(response);
        }
    }
}
