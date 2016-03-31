package org.jasig.cas.adaptors.duo;

import com.duosecurity.duoweb.DuoWeb;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.util.http.HttpClient;
import org.jasig.cas.util.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.net.URLDecoder;

/**
 * An abstraction that encapsulates interaction with Duo 2fa authentication service via its public API.
 * @author Michael Kennedy
 * @author Misagh Moayyed
 * @author Eric Pierce
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
@RefreshScope
@Component("duoAuthenticationService")
public class DuoAuthenticationService {
    private transient Logger logger = LoggerFactory.getLogger(this.getClass());
    
    
    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;

    
    @Value("${cas.mfa.duo.integration.key:}")
    private String duoIntegrationKey;

    
    @Value("${cas.mfa.duo.secret.key:}")
    private String duoSecretKey;

    
    @Value("${cas.mfa.duo.application.key:}")
    private String duoApplicationKey;

    
    @Value("${cas.mfa.duo.api.host:}")
    private String duoApiHost;

    /**
     * Creates the duo authentication service.
     */
    public DuoAuthenticationService() {}

    @PostConstruct
    private void initialize() {
        Assert.hasLength(this.duoApiHost, "Duo API host cannot be blank");
        Assert.hasLength(this.duoIntegrationKey, "Duo integration key cannot be blank");
        Assert.hasLength(this.duoSecretKey, "Duo secret key cannot be blank");
        Assert.hasLength(this.duoApplicationKey, "Duo application key cannot be blank");
    }

    public String getDuoApiHost() {
        return this.duoApiHost;
    }

    public String getDuoIntegrationKey() {
        return duoIntegrationKey;
    }

    public String getDuoSecretKey() {
        return duoSecretKey;
    }

    public String getDuoApplicationKey() {
        return duoApplicationKey;
    }

    /**
     * Sign the authentication request.
     * @param username username requesting authentication
     * @return signed response
     */
    public String generateSignedRequestToken(final String username) {
        return DuoWeb.signRequest(this.duoIntegrationKey, this.duoSecretKey, this.duoApplicationKey, username);
    }

    /**
     * Verify the authentication response from Duo.
     * @param signedRequestToken signed request token
     * @return authenticated user
     * @throws Exception if response verification fails
     */
    public String authenticate(final String signedRequestToken) throws Exception {
        if (StringUtils.isBlank(signedRequestToken)) {
            throw new IllegalArgumentException("No signed request token was passed to verify");
        }

        logger.debug("Calling DuoWeb.verifyResponse with signed request token '{}'", signedRequestToken);
        return DuoWeb.verifyResponse(this.duoIntegrationKey, this.duoSecretKey, this.duoApplicationKey, signedRequestToken);
    }

    /**
     * Can ping boolean.
     *
     * @return the boolean
     */
    public boolean canPing() {
        try {
            String url = this.duoApiHost.concat("/rest/v1/ping");
            if (!url.startsWith("http")) {
                url = "https://" + url;
            }
            final HttpMessage msg = this.httpClient.sendMessageToEndPoint(new URL(url));
            if (msg != null) {
                final String response = URLDecoder.decode(msg.getMessage(), "UTF-8");
                logger.debug("Received Duo ping response {}", response);
                final ObjectMapper mapper = new ObjectMapper();
                final JsonNode result = mapper.readTree(response);
                if (result.has("response") && result.has("stat")
                        && result.get("response").asText().equalsIgnoreCase("pong")
                        && result.get("stat").asText().equalsIgnoreCase("OK")) {
                    return true;
                }
                logger.warn("Could not reach/ping Duo. Response returned is {}", result);
            }
        } catch (final Exception e) {
            logger.warn("Pinging Duo has failed with error: {}", e.getMessage(), e);
        }
        return false;
    }
}
