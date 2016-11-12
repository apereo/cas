package org.apereo.cas.adaptors.duo.authn;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link BaseDuoAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseDuoAuthenticationService implements DuoAuthenticationService {

    private static final String RESULT_KEY_RESPONSE = "response";
    private static final String RESULT_KEY_STAT = "stat";
    private static final long serialVersionUID = -8044100706027708789L;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Duo Properties.
     */
    protected final MultifactorAuthenticationProperties.Duo duoProperties;

    private transient HttpClient httpClient;

    /**
     * Creates the duo authentication service.
     */
    public BaseDuoAuthenticationService(final MultifactorAuthenticationProperties.Duo duoProperties) {
        this.duoProperties = duoProperties;
    }

    @Override
    public boolean ping() {
        try {
            String url = getApiHost().concat("/rest/v1/ping");
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

    @Override
    public String getApiHost() {
        return duoProperties.getDuoApiHost();
    }

    public void setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final BaseDuoAuthenticationService rhs = (BaseDuoAuthenticationService) obj;
        return new EqualsBuilder()
                .append(this.duoProperties.getDuoApiHost(), rhs.duoProperties.getDuoApiHost())
                .append(this.duoProperties.getDuoApplicationKey(), rhs.duoProperties.getDuoApplicationKey())
                .append(this.duoProperties.getDuoIntegrationKey(), rhs.duoProperties.getDuoIntegrationKey())
                .append(this.duoProperties.getDuoSecretKey(), rhs.duoProperties.getDuoSecretKey())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.duoProperties.getDuoApiHost())
                .append(this.duoProperties.getDuoApplicationKey())
                .append(this.duoProperties.getDuoIntegrationKey())
                .append(this.duoProperties.getDuoSecretKey())
                .toHashCode();
    }
}
