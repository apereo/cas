package org.apereo.cas.adaptors.duo.authn;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.adaptors.duo.DuoIntegration;
import org.apereo.cas.adaptors.duo.DuoUserAccount;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

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

    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

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
            final String url = buildUrlHttpScheme(getApiHost().concat("/rest/v1/ping"));
            logger.debug("Contacting Duo @ {}", url);

            final HttpMessage msg = this.httpClient.sendMessageToEndPoint(new URL(url));
            if (msg != null) {
                final String response = URLDecoder.decode(msg.getMessage(), StandardCharsets.UTF_8.name());
                logger.debug("Received Duo ping response {}", response);
                final ObjectMapper mapper = new ObjectMapper();
                mapper.findAndRegisterModules();
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

    @Override
    public Optional<DuoIntegration> getDuoIntegrationPolicy() {
        try {
            final URL url = new URIBuilder(buildUrlHttpScheme(
                    getApiHost().concat("/admin/v1/integrations/")
                            .concat(duoProperties.getDuoIntegrationKey()))).build().toURL();

            logger.debug("Contacting Duo @ {}", url);
            final HttpMessage msg = this.httpClient.sendMessageToEndPoint(url);
            if (msg != null) {
                final String jsonResponse = URLDecoder.decode(msg.getMessage(), StandardCharsets.UTF_8.name());
                logger.debug("Received Duo admin response {}", jsonResponse);
                final ObjectMapper mapper = new ObjectMapper();
                mapper.findAndRegisterModules();
                final JsonNode result = mapper.readTree(jsonResponse);
                if (result.has(RESULT_KEY_RESPONSE) && result.has(RESULT_KEY_STAT)
                        && result.get(RESULT_KEY_STAT).asText().equalsIgnoreCase("OK")) {

                    final JsonNode response = result.get(RESULT_KEY_RESPONSE);

                    final DuoIntegration policy =
                            DuoIntegration.newInstance(getJsonNodeFieldValue(response, "name"))
                                    .setType(getJsonNodeFieldValue(response, "type"))
                                    .setGreeting(getJsonNodeFieldValue(response, "greeting"))
                                    .setEnrollmentPolicyStatus(DuoIntegration.DuoEnrollmentPolicyStatus.valueOf(
                                            getJsonNodeFieldValue(response, "enroll_policy",
                                                    DuoIntegration.DuoEnrollmentPolicyStatus.ENROLL.name()).toUpperCase()));


                    logger.debug("Found/Constructed Duo account integration {}", policy);
                    return Optional.of(policy);
                }

                logger.warn("Could not reach/ping Duo. Response returned is {}", result);
            }
        } catch (final Exception e) {
            logger.warn("Reaching Duo has failed with error: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<DuoUserAccount> getDuoUserAccount(final String uid) {
        try {
            final URL url = new URIBuilder(buildUrlHttpScheme(getApiHost().concat("/admin/v1/users/").concat(uid))).build().toURL();
            logger.debug("Contacting Duo @ {}", url);

            final HttpMessage msg = this.httpClient.sendMessageToEndPoint(url);
            if (msg != null) {
                final String jsonResponse = URLDecoder.decode(msg.getMessage(), StandardCharsets.UTF_8.name());
                logger.debug("Received Duo admin response {}", jsonResponse);
                final ObjectMapper mapper = new ObjectMapper();
                mapper.findAndRegisterModules();
                final JsonNode result = mapper.readTree(jsonResponse);
                if (result.has(RESULT_KEY_RESPONSE) && result.has(RESULT_KEY_STAT)
                        && result.get(RESULT_KEY_STAT).asText().equalsIgnoreCase("OK")) {


                    JsonNode response = result.get(RESULT_KEY_RESPONSE);
                    if (response.getNodeType() == JsonNodeType.ARRAY) {
                        response = response.elements().next();
                    }
                    final DuoUserAccount account =
                            DuoUserAccount.newInstance(getJsonNodeFieldValue(response, "user_id"))
                                    .setEmail(getJsonNodeFieldValue(response, "user_id"))
                                    .setRealName(getJsonNodeFieldValue(response, "realname"))
                                    .setUsername(getJsonNodeFieldValue(response, "username"))
                                    .setStatus(DuoUserAccount.DuoAccountStatus.valueOf(
                                            getJsonNodeFieldValue(response, "status",
                                                    DuoUserAccount.DuoAccountStatus.DISABLED.name()).toUpperCase()));
                    if (response.has("groups")) {
                        final Set<String> groups = Sets.newHashSet();
                        response.get("groups").elements().forEachRemaining(n -> groups.add(n.get("name").asText()));
                        account.setGroups(groups);
                    }

                    logger.debug("Found/Constructed Duo user account {}", account);
                    return Optional.of(account);
                }

                logger.warn("Could not reach/ping Duo. Response returned is {}", result);
            }
        } catch (final Exception e) {
            logger.warn("Reaching Duo has failed with error: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }

    private String buildUrlHttpScheme(final String url) {
        if (!url.startsWith("http")) {
            return "https://" + url;
        }
        return url;
    }


    private static String getJsonNodeFieldValue(final JsonNode node, final String fieldName) {
        return getJsonNodeFieldValue(node, fieldName, null);
    }

    private static String getJsonNodeFieldValue(final JsonNode node, final String fieldName, final String defaultValue) {
        if (node != null && node.has(fieldName)) {
            return node.get(fieldName).asText(defaultValue);
        }
        return null;
    }
}
