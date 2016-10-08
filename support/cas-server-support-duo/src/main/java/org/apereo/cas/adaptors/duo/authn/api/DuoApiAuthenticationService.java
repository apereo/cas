package org.apereo.cas.adaptors.duo.authn.api;

import com.duosecurity.client.Http;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

/**
 * An abstraction that encapsulates interaction with Duo 2fa authentication service via its public API.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DuoApiAuthenticationService {
    private static final int API_VERSION = 2;
    
    private transient Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CasConfigurationProperties casProperties;
    

    /**
     * Creates the duo authentication service.
     */
    public DuoApiAuthenticationService() {
    }

    @PostConstruct
    private void initialize() {
        Assert.hasLength(casProperties.getAuthn().getMfa().getDuo().getDuoApiHost(), "Duo API host cannot be blank");
        Assert.hasLength(casProperties.getAuthn().getMfa().getDuo().getDuoIntegrationKey(), "Duo integration key cannot be blank");
        Assert.hasLength(casProperties.getAuthn().getMfa().getDuo().getDuoSecretKey(), "Duo secret key cannot be blank");
        Assert.hasLength(casProperties.getAuthn().getMfa().getDuo().getDuoApplicationKey(), "Duo application key cannot be blank");
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
                    casProperties.getAuthn().getMfa().getDuo().getDuoApiHost(),
                    "/auth/v" + API_VERSION + "/auth");
            request.addParam("username", p.getId());
            request.addParam("factor", "auto");
            request.addParam("device", "auto");

            request.signRequest(
                    casProperties.getAuthn().getMfa().getDuo().getDuoIntegrationKey(),
                    casProperties.getAuthn().getMfa().getDuo().getDuoSecretKey(), API_VERSION);

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
