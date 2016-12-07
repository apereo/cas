package org.apereo.cas.adaptors.duo.authn;

import com.duosecurity.client.Http;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.adaptors.duo.DuoIntegration;
import org.apereo.cas.adaptors.duo.DuoUserAccount;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProperties;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

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
    private static final int AUTH_API_VERSION = 2;
    private static final int ADMIN_API_VERSION = 1;
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
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
     *
     * @param duoProperties the duo properties
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

                final JsonNode result = MAPPER.readTree(response);
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
            final Http integrationRequest = buildHttpGetIntegrationsRequest(duoProperties.getDuoIntegrationKey());
            signHttpIntegrationsRequest(integrationRequest);

            logger.debug("Contacting Duo to inquire about the integration policy {}", duoProperties.getDuoIntegrationKey());
            final String integrationResponse = integrationRequest.executeHttpRequest().body().string();

            final String jsonResponse = URLDecoder.decode(integrationResponse, StandardCharsets.UTF_8.name());
            logger.debug("Received Duo admin response {}", jsonResponse);

            final JsonNode result = MAPPER.readTree(jsonResponse);
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
        } catch (final Exception e) {
            logger.warn("Reaching Duo has failed with error: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<DuoUserAccount> getDuoUserAccount(final String username) {
        try {

            final Http userRequest = buildHttpGetUsersRequest(username);
            signHttpUsersRequest(userRequest);
            logger.debug("Contacting Duo to inquire about username {}", username);
            final String userResponse = userRequest.executeHttpRequest().body().string();
            final String jsonResponse = URLDecoder.decode(userResponse, StandardCharsets.UTF_8.name());
            logger.debug("Received Duo admin response {}", jsonResponse);

            final JsonNode result = MAPPER.readTree(jsonResponse);
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

    protected Http buildHttpPostAuthRequest() {
        return new Http(HttpMethod.POST.name(),
                duoProperties.getDuoApiHost(),
                String.format("/auth/v%s/auth", AUTH_API_VERSION);
    }

    protected Http buildHttpGetUsersRequest(final String username) {
        final Http usersRequest = new Http(HttpMethod.GET.name(),
                duoProperties.getDuoApiHost(),
                String.format("/admin/v%s/users", ADMIN_API_VERSION));
        usersRequest.addParam("username", username);
        return usersRequest;
    }

    protected Http buildHttpGetIntegrationsRequest(final String integrationKey) {
        return new Http(HttpMethod.GET.name(),
                duoProperties.getDuoApiHost(),
                String.format("/admin/v%s/integrations/%s", ADMIN_API_VERSION, integrationKey));
    }

    /**
     * Sign http request.
     *
     * @param request the request
     * @param id      the id
     */
    protected void signHttpAuthRequest(final Http request, final String id) {
        try {
            request.addParam("username", id);
            request.addParam("factor", "auto");
            request.addParam("device", "auto");
            request.signRequest(
                    duoProperties.getDuoIntegrationKey(),
                    duoProperties.getDuoSecretKey(), AUTH_API_VERSION);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    protected void signHttpIntegrationsRequest(final Http request) {
        try {
            request.signRequest(
                    duoProperties.getDuoIntegrationKey(),
                    duoProperties.getDuoSecretKey(), ADMIN_API_VERSION);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    protected void signHttpUsersRequest(final Http request) {
        try {
            request.signRequest(
                    duoProperties.getDuoIntegrationKey(),
                    duoProperties.getDuoSecretKey(), ADMIN_API_VERSION);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
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
