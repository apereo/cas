package org.apereo.cas.adaptors.duo.authn.api;

import com.duosecurity.client.Http;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

/**
 * An abstraction that encapsulates interaction with Duo 2fa authentication service via its public API.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DuoApiAuthenticationService {
    private static final int API_VERSION = 2;

    private static final Logger LOGGER = LoggerFactory.getLogger(DuoApiAuthenticationService.class);

    private final MultifactorAuthenticationProperties.Duo duoProperties;

    /**
     * Creates the duo authentication service.
     */
    public DuoApiAuthenticationService(final MultifactorAuthenticationProperties.Duo duoProperties) {
        this.duoProperties = duoProperties;
    }

    /**
     * Verify the authentication response from Duo.
     *
     * @param credential the authentication
     * @return true/false
     */
    public boolean authenticate(final DuoApiCredential credential) {
        try {
            final Principal p = credential.getAuthentication().getPrincipal();
            final Http request = new Http(HttpMethod.POST.name(),
                    duoProperties.getDuoApiHost(),
                    "/auth/v" + API_VERSION + "/auth");
            request.addParam("username", p.getId());
            request.addParam("factor", "auto");
            request.addParam("device", "auto");
            request.signRequest(
                    duoProperties.getDuoIntegrationKey(),
                    duoProperties.getDuoSecretKey(), API_VERSION);

            final JSONObject result = (JSONObject) request.executeRequest();
            LOGGER.debug("Duo authentication response: {}", result);
            if ("allow".equalsIgnoreCase(result.getString("result"))) {
                return true;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
