package org.apereo.cas.azure.ad.authentication;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.azuread.AzureActiveDirectoryAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import javax.security.auth.login.FailedLoginException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link AzureActiveDirectoryAuthenticationHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class AzureActiveDirectoryAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
            .singleValueAsArray(true).build().toObjectMapper();

    private final AzureActiveDirectoryAuthenticationProperties properties;

    public AzureActiveDirectoryAuthenticationHandler(final ServicesManager servicesManager,
                                                     final PrincipalFactory principalFactory,
                                                     final AzureActiveDirectoryAuthenticationProperties properties) {
        super(properties.getName(), servicesManager, principalFactory, properties.getOrder());
        this.properties = properties;
    }

    private String getUserInfoFromGraph(final IAuthenticationResult authenticationResult, final String username) throws Exception {
        String select = "";
        if (StringUtils.isNotBlank(properties.getAttributes())) {
            select = "?$select=" + properties.getAttributes();
        }

        val url = new URI(StringUtils.appendIfMissing(properties.getResource(), "/") + "v1.0/users/" + username + select).toURL();
        val conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + authenticationResult.accessToken());
        conn.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        LOGGER.debug("Fetching user info from [{}] using access token [{}]", url.toExternalForm(), authenticationResult.accessToken());
        val httpResponseCode = conn.getResponseCode();
        if (HttpStatus.valueOf(httpResponseCode).is2xxSuccessful()) {
            return IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8);
        }
        val msg = String.format("Failed: status %s with message: %s", httpResponseCode, conn.getResponseMessage());
        throw new FailedLoginException(msg);
    }

    protected IAuthenticationResult getAccessTokenFromUserCredentials(final String username, final String password) throws Exception {
        val clientId = SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getClientId());
        val scopes = org.springframework.util.StringUtils.commaDelimitedListToSet(properties.getScope());
        if (StringUtils.isNotBlank(properties.getClientSecret())) {
            val clientSecret = SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getClientSecret());
            val clientCredential = ClientCredentialFactory.createFromSecret(clientSecret);
            val context = ConfidentialClientApplication.builder(clientId, clientCredential)
                    .authority(properties.getLoginUrl())
                    .validateAuthority(true)
                    .build();
            val resource = StringUtils.appendIfMissing(properties.getResource(), "/").concat(".default");
            val parameters = ClientCredentialParameters.builder(Set.of(resource))
                    .tenant(properties.getTenant())
                    .build();
            LOGGER.debug("Acquiring token for [{}] with tenant [{}] for resource [{}]", username, properties.getTenant(), resource);
            val future = context.acquireToken(parameters);
            return future.get();
        }
        val context = PublicClientApplication.builder(clientId)
                .authority(properties.getLoginUrl())
                .validateAuthority(true)
                .build();
        val parameters = UserNamePasswordParameters.builder(scopes, username, password.toCharArray())
                .tenant(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getTenant()))
                .build();
        LOGGER.debug("Acquiring token for [{}] with tenant [{}] scopes [{}]", username, properties.getTenant(), scopes);
        val future = context.acquireToken(parameters);
        return future.get();
    }

    @Override
    protected AuthenticationHandlerExecutionResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential,
            final String originalPassword) throws Throwable {

        try {
            val username = credential.getUsername();
            LOGGER.trace("Fetching token for [{}]", username);
            val result = getAccessTokenFromUserCredentials(username, credential.toPassword());
            LOGGER.debug("Retrieved token [{}] for [{}]", result.accessToken(), username);
            val userInfo = getUserInfoFromGraph(result, username);
            LOGGER.trace("Retrieved user info [{}]", userInfo);
            val userInfoMap = (Map<String, ?>) MAPPER.readValue(JsonValue.readHjson(userInfo).toString(), Map.class);
            val attributeMap = new HashMap<String, List<Object>>(userInfoMap.size());
            userInfoMap.forEach((key, value) -> {
                val values = CollectionUtils.toCollection(value, ArrayList.class);
                if (!values.isEmpty()) {
                    attributeMap.put(key, values);
                }
            });
            val principal = principalFactory.createPrincipal(username, attributeMap);
            LOGGER.debug("Created principal for id [{}] and [{}] attributes", username, attributeMap);
            return createHandlerResult(credential, principal, new ArrayList<>());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            throw new FailedLoginException("Invalid credentials: " + e.getMessage());
        }
    }
}
