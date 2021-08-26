package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.duosecurity.duoweb.DuoWeb;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * An abstraction that encapsulates interaction with Duo 2fa authentication service via its public API.
 *
 * @author Michael Kennedy
 * @author Misagh Moayyed
 * @author Eric Pierce
 * @author Dmitriy Kopylenko
 * @since 4.2
 * @deprecated since 6.4.0
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Deprecated(since = "6.4.0")
public class BasicDuoSecurityAuthenticationService extends BaseDuoSecurityAuthenticationService {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private static final long serialVersionUID = -6690808348975271382L;

    public BasicDuoSecurityAuthenticationService(final DuoSecurityMultifactorAuthenticationProperties duoProperties,
                                                 final HttpClient httpClient,
                                                 final List<MultifactorAuthenticationPrincipalResolver> multifactorAuthenticationPrincipalResolver,
                                                 final Cache<String, DuoSecurityUserAccount> userAccountCache) {
        super(duoProperties, httpClient, multifactorAuthenticationPrincipalResolver, userAccountCache);
    }

    private static String buildUrlHttpScheme(final String url) {
        if (!RegexUtils.find("^http(s)*://", url)) {
            return "https://" + url;
        }
        return url;
    }

    @Override
    public DuoSecurityAuthenticationResult authenticateInternal(final Credential creds) throws Exception {
        return authenticateDuoCredential(creds);
    }

    @Override
    public boolean ping() {
        try {
            val duoApiHost = SpringExpressionLanguageValueResolver.getInstance().resolve(getProperties().getDuoApiHost());
            val url = buildUrlHttpScheme(duoApiHost.concat("/rest/v1/ping"));
            LOGGER.trace("Pinging Duo Security @ [{}]", url);

            val msg = httpClient.sendMessageToEndPoint(new URL(url));
            if (msg != null) {
                val response = URLDecoder.decode(msg.getMessage(), StandardCharsets.UTF_8.name());
                LOGGER.debug("Received Duo ping response [{}]", response);

                val result = MAPPER.readTree(response);
                if (result.has(RESULT_KEY_RESPONSE) && result.has(RESULT_KEY_STAT)
                    && result.get(RESULT_KEY_RESPONSE).asText().equalsIgnoreCase("pong")
                    && result.get(RESULT_KEY_STAT).asText().equalsIgnoreCase("OK")) {
                    return true;
                }
                LOGGER.warn("Could not reach/ping Duo. Response returned is [{}]", result);
            }
        } catch (final Exception e) {
            LOGGER.warn("Pinging Duo has failed with error: [{}]", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Optional<String> signRequestToken(final String uid) {
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        return Optional.of(DuoWeb.signRequest(
            resolver.resolve(properties.getDuoIntegrationKey()),
            resolver.resolve(properties.getDuoSecretKey()),
            resolver.resolve(properties.getDuoApplicationKey()), uid));
    }

    private DuoSecurityAuthenticationResult authenticateDuoCredential(final Credential creds) throws Exception {
        val signedRequestToken = DuoSecurityCredential.class.cast(creds).getSignedDuoResponse();
        if (StringUtils.isBlank(signedRequestToken)) {
            throw new IllegalArgumentException("No signed request token was passed to verify");
        }
        LOGGER.trace("Verifying duo response with signed request token '[{}]'", signedRequestToken);
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        val authUserId = DuoWeb.verifyResponse(
            resolver.resolve(properties.getDuoIntegrationKey()),
            resolver.resolve(properties.getDuoSecretKey()),
            resolver.resolve(properties.getDuoApplicationKey()), signedRequestToken);
        return DuoSecurityAuthenticationResult.builder().success(true).username(authUserId).build();
    }

}
