package org.apereo.cas.adaptors.duo.authn.web;

import com.duosecurity.duoweb.DuoWeb;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(DuoAuthenticationService.class);

    private HttpClient httpClient;

    private final MultifactorAuthenticationProperties.Duo duoProperties;

    /**
     * Creates the duo authentication service.
     */
    public DuoAuthenticationService(final MultifactorAuthenticationProperties.Duo duoProperties) {
        this.duoProperties = duoProperties;
    }

    /**
     * Sign the authentication request.
     *
     * @param username username requesting authentication
     * @return signed response
     */
    public String generateSignedRequestToken(final String username) {
        return DuoWeb.signRequest(duoProperties.getDuoIntegrationKey(),
                duoProperties.getDuoSecretKey(),
                duoProperties.getDuoApplicationKey(), username);
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

        LOGGER.debug("Calling DuoWeb.verifyResponse with signed request token '{}'", signedRequestToken);
        return DuoWeb.verifyResponse(duoProperties.getDuoIntegrationKey(),
                duoProperties.getDuoSecretKey(),
                duoProperties.getDuoApplicationKey(), signedRequestToken);
    }

    /**
     * Can ping boolean.
     *
     * @return true/false
     */
    public boolean canPing() {
        try {
            String url = duoProperties.getDuoApiHost().concat("/rest/v1/ping");
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            final HttpMessage msg = this.httpClient.sendMessageToEndPoint(new URL(url));
            if (msg != null) {
                final String response = URLDecoder.decode(msg.getMessage(), StandardCharsets.UTF_8.name());
                LOGGER.debug("Received Duo ping response {}", response);
                final ObjectMapper mapper = new ObjectMapper();
                final JsonNode result = mapper.readTree(response);
                if (result.has(RESULT_KEY_RESPONSE) && result.has(RESULT_KEY_STAT)
                        && result.get(RESULT_KEY_RESPONSE).asText().equalsIgnoreCase("pong")
                        && result.get(RESULT_KEY_STAT).asText().equalsIgnoreCase("OK")) {
                    return true;
                }
                LOGGER.warn("Could not reach/ping Duo. Response returned is {}", result);
            }
        } catch (final Exception e) {
            LOGGER.warn("Pinging Duo has failed with error: {}", e.getMessage(), e);
        }
        return false;
    }

    public void setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

}
