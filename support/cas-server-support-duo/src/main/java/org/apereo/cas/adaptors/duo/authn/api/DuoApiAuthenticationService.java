package org.apereo.cas.adaptors.duo.authn.api;

import com.duosecurity.client.Http;
import org.apereo.cas.adaptors.duo.authn.BaseDuoAuthenticationService;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;

/**
 * An abstraction that encapsulates interaction with Duo 2fa authentication service via its public API.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DuoApiAuthenticationService extends BaseDuoAuthenticationService<Boolean> {
    private static final int API_VERSION = 2;

    /**
     * Creates the duo authentication service.
     *
     * @param duoProperties
     */
    public DuoApiAuthenticationService(final MultifactorAuthenticationProperties.Duo duoProperties) {
        super(duoProperties);
    }

    @Override
    public Boolean authenticate(final Credential crds) {
        try {
            final DuoApiCredential credential = DuoApiCredential.class.cast(crds);
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
            logger.debug("Duo authentication response: {}", result);
            if ("allow".equalsIgnoreCase(result.getString("result"))) {
                return true;
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

}
