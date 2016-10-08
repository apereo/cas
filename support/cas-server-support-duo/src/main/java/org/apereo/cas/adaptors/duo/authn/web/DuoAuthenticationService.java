package org.apereo.cas.adaptors.duo.authn.web;

import com.duosecurity.duoweb.DuoWeb;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * An abstraction that encapsulates interaction with Duo 2fa authentication service via its public API.
 *
 * @author Michael Kennedy
 * @author Misagh Moayyed
 * @author Eric Pierce
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public class DuoAuthenticationService {
    private static final String RESULT_KEY_RESPONSE = "response";
    private static final String RESULT_KEY_STAT = "stat";

    private transient Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private HttpClient httpClient;

    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Creates the duo authentication service.
     */
    public DuoAuthenticationService() {
    }

    @PostConstruct
    private void initialize() {
        Assert.hasLength(casProperties.getAuthn().getMfa().getDuo().getDuoApiHost(), "Duo API host cannot be blank");
        Assert.hasLength(casProperties.getAuthn().getMfa().getDuo().getDuoIntegrationKey(), "Duo integration key cannot be blank");
        Assert.hasLength(casProperties.getAuthn().getMfa().getDuo().getDuoSecretKey(), "Duo secret key cannot be blank");
        Assert.hasLength(casProperties.getAuthn().getMfa().getDuo().getDuoApplicationKey(), "Duo application key cannot be blank");
    }


    /**
     * Sign the authentication request.
     *
     * @param username username requesting authentication
     * @return signed response
     */
    public String generateSignedRequestToken(final String username) {
        return DuoWeb.signRequest(casProperties.getAuthn().getMfa().getDuo().getDuoIntegrationKey(),
                casProperties.getAuthn().getMfa().getDuo().getDuoSecretKey(),
                casProperties.getAuthn().getMfa().getDuo().getDuoApplicationKey(), username);
    }

    /**
     * Verify the authentication response from Duo.
     *
     * @param signedRequestToken signed request token
     * @return authenticated user
     * @throws Exception if response verification fails
     */
    public String authenticate(final String signedRequestToken) throws Exception {
        if (StringUtils.isBlank(signedRequestToken)) {
            throw new IllegalArgumentException("No signed request token was passed to verify");
        }

        logger.debug("Calling DuoWeb.verifyResponse with signed request token '{}'", signedRequestToken);
        return DuoWeb.verifyResponse(casProperties.getAuthn().getMfa().getDuo().getDuoIntegrationKey(),
                casProperties.getAuthn().getMfa().getDuo().getDuoSecretKey(),
                casProperties.getAuthn().getMfa().getDuo().getDuoApplicationKey(), signedRequestToken);
    }

    /**
     * Can ping boolean.
     *
     * @return true/false
     */
    public boolean canPing() {
        try {
            String url = casProperties.getAuthn().getMfa().getDuo().getDuoApiHost().concat("/rest/v1/ping");
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            final HttpMessage msg = this.httpClient.sendMessageToEndPoint(new URL(url));
            if (msg != null) {
                final String response = URLDecoder.decode(msg.getMessage(), StandardCharsets.UTF_8.name());
                logger.debug("Received Duo ping response {}", response);
                final ObjectMapper mapper = new ObjectMapper();
                final JsonNode result = mapper.readTree(response);
                if (result.has(RESULT_KEY_RESPONSE) && result.has(RESULT_KEY_STAT)
                        && result.get(RESULT_KEY_RESPONSE).asText().equalsIgnoreCase("pong")
                        && result.get(RESULT_KEY_STAT).asText().equalsIgnoreCase("OK")) {
                    return true;
                }
                logger.warn("Could not reach/ping Duo. Response returned is {}", result);
            }
        } catch (final Exception e) {
            logger.warn("Pinging Duo has failed with error: {}", e.getMessage(), e);
        }
        return false;
    }

    public void setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }
    
    public String getDuoApiHost() {
        return casProperties.getAuthn().getMfa().getDuo().getDuoApiHost();
    }
}
