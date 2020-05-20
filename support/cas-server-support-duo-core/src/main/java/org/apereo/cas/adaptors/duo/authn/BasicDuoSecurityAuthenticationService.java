package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.util.http.HttpClient;

import com.duosecurity.duoweb.DuoWeb;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

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

    private static final long serialVersionUID = -6690808348975271382L;

    /**
     * Creates the duo authentication service.
     *
     * @param duoProperties Duo authentication properties
     * @param httpClient    http client used to run the requests
     */
    public BasicDuoSecurityAuthenticationService(final DuoSecurityMultifactorProperties duoProperties, final HttpClient httpClient) {
        super(duoProperties, httpClient);
    }

    @Override
    public String signRequestToken(final String uid) {
        return DuoWeb.signRequest(duoProperties.getDuoIntegrationKey(),
            duoProperties.getDuoSecretKey(),
            duoProperties.getDuoApplicationKey(), uid);
    }

    @Override
    public Pair<Boolean, String> authenticate(final Credential creds) throws Exception {
        if (creds instanceof DuoSecurityDirectCredential) {
            return authenticateDuoCredentialDirect(creds);
        }
        return authenticateDuoCredential(creds);
    }

    private Pair<Boolean, String> authenticateDuoCredentialDirect(final Credential crds) {
        try {
            val credential = DuoSecurityDirectCredential.class.cast(crds);
            val p = credential.getAuthentication().getPrincipal();
            val request = buildHttpPostAuthRequest();
            signHttpAuthRequest(request, p.getId());
            val result = (JSONObject) request.executeRequest();
            LOGGER.debug("Duo authentication response: [{}]", result);
            if ("allow".equalsIgnoreCase(result.getString("result"))) {
                return Pair.of(Boolean.TRUE, crds.getId());
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return Pair.of(Boolean.FALSE, crds.getId());
    }

    private Pair<Boolean, String> authenticateDuoCredential(final Credential creds) throws Exception {
        val signedRequestToken = DuoSecurityCredential.class.cast(creds).getSignedDuoResponse();
        if (StringUtils.isBlank(signedRequestToken)) {
            throw new IllegalArgumentException("No signed request token was passed to verify");
        }

        LOGGER.debug("Calling DuoWeb.verifyResponse with signed request token '[{}]'", signedRequestToken);
        val result = DuoWeb.verifyResponse(duoProperties.getDuoIntegrationKey(),
            duoProperties.getDuoSecretKey(),
            duoProperties.getDuoApplicationKey(), signedRequestToken);
        return Pair.of(Boolean.TRUE, result);
    }

}
