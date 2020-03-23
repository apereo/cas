package org.apereo.cas.azure.ad.authentication;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.security.auth.login.FailedLoginException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is {@link AzureActiveDirectoryAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class AzureActiveDirectoryAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private final String loginUrl;

    private final String resource;

    private final String clientId;

    public AzureActiveDirectoryAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                                     final PrincipalFactory principalFactory, final Integer order,
                                                     final String clientId, final String loginUrl,
                                                     final String resource) {
        super(name, servicesManager, principalFactory, order);
        this.clientId = clientId;
        this.loginUrl = loginUrl;
        this.resource = resource;
    }

    private String getUserInfoFromGraph(final String accessToken) throws Exception {
        val url = new URL(StringUtils.appendIfMissing(this.resource, "/") + "v1.0/me");
        val conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", MediaType.APPLICATION_JSON_VALUE);

        LOGGER.debug("Fetching user info from [{}] using access token [{}]", url.toExternalForm(), accessToken);
        val httpResponseCode = conn.getResponseCode();
        if (HttpStatus.valueOf(httpResponseCode).is2xxSuccessful()) {
            return IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8);
        }
        val msg = String.format("Failed: status %s with message: %s", httpResponseCode, conn.getResponseMessage());
        throw new FailedLoginException(msg);
    }

    private AuthenticationResult getAccessTokenFromUserCredentials(final String username, final String password) throws Exception {
        var service = (ExecutorService) null;
        try {
            service = Executors.newFixedThreadPool(1);
            val context = new AuthenticationContext(this.loginUrl, false, service);
            LOGGER.debug("Acquiring token for resource [{}] and client id [{}} for user [{}]", this.resource, this.clientId, username);
            val future = context.acquireToken(this.resource, this.clientId, username, password, null);
            return future.get();
        } finally {
            if (service != null) {
                service.shutdown();
            }
        }
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
                                                                                        final String originalPassword)
        throws GeneralSecurityException {

        try {
            val username = credential.getUsername();
            LOGGER.trace("Fetching token for [{}]", username);
            val result = getAccessTokenFromUserCredentials(username, credential.getPassword());
            LOGGER.debug("Retrieved token [{}] for [{}]", result.getAccessToken(), username);
            val userInfo = getUserInfoFromGraph(result.getAccessToken());
            LOGGER.trace("Retrieved user info [{}]", userInfo);
            val userInfoMap = (Map<String, ?>) MAPPER.readValue(JsonValue.readHjson(userInfo).toString(), Map.class);

            val attributeMap = Maps.<String, List<Object>>newHashMapWithExpectedSize(userInfoMap.size());
            userInfoMap.forEach((key, value) -> {
                val values = CollectionUtils.toCollection(value, ArrayList.class);
                if (!values.isEmpty()) {
                    attributeMap.put(key, values);
                }
            });

            val principal = this.principalFactory.createPrincipal(username, attributeMap);
            LOGGER.debug("Created principal for id [{}] and [{}] attributes", username, attributeMap);
            return createHandlerResult(credential, principal, new ArrayList<>(0));
        } catch (final Exception e) {
            throw new FailedLoginException("Invalid credentials: " + e.getMessage());
        }
    }
}
