package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.http.HttpClient;

import com.duosecurity.duoweb.DuoWeb;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class BasicDuoSecurityAuthenticationService extends BaseDuoSecurityAuthenticationService {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final long serialVersionUID = -6690808348975271382L;

    public BasicDuoSecurityAuthenticationService(final DuoSecurityMultifactorProperties duoProperties,
        final HttpClient httpClient, final List<MultifactorAuthenticationPrincipalResolver> multifactorAuthenticationPrincipalResolver) {
        super(duoProperties, httpClient, multifactorAuthenticationPrincipalResolver);
    }

    @Override
    public DuoSecurityAuthenticationResult authenticateInternal(final Credential creds) throws Exception {
        return authenticateDuoCredential(creds);
    }

    @Override
    public boolean ping() {
        try {
            val url = buildUrlHttpScheme(getApiHost().concat("/rest/v1/ping"));
            LOGGER.trace("Contacting Duo @ [{}]", url);

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
        return Optional.of(DuoWeb.signRequest(duoProperties.getDuoIntegrationKey(),
            duoProperties.getDuoSecretKey(),
            duoProperties.getDuoApplicationKey(), uid));
    }

    private DuoSecurityAuthenticationResult authenticateDuoCredential(final Credential creds) throws Exception {
        val signedRequestToken = DuoSecurityCredential.class.cast(creds).getSignedDuoResponse();
        if (StringUtils.isBlank(signedRequestToken)) {
            throw new IllegalArgumentException("No signed request token was passed to verify");
        }
        LOGGER.trace("Verifying duo response with signed request token '[{}]'", signedRequestToken);
        val authUserId = DuoWeb.verifyResponse(duoProperties.getDuoIntegrationKey(),
            duoProperties.getDuoSecretKey(),
            duoProperties.getDuoApplicationKey(), signedRequestToken);
        return DuoSecurityAuthenticationResult.builder().success(true).username(authUserId).build();
    }

    private static String buildUrlHttpScheme(final String url) {
        if (!RegexUtils.find("^http(s)*://", url)) {
            return "https://" + url;
        }
        return url;
    }

}
