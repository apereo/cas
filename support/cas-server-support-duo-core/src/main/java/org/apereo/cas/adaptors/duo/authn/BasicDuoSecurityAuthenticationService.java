package org.apereo.cas.adaptors.duo.authn;

import com.duosecurity.client.Http;
import com.duosecurity.duoweb.DuoWeb;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.util.http.HttpClient;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstraction that encapsulates interaction with Duo 2fa authentication service via its public API.
 *
 * @author Michael Kennedy
 * @author Misagh Moayyed
 * @author Eric Pierce
 * @author Dmitriy Kopylenko
 * @since 4.2
 */
public class BasicDuoSecurityAuthenticationService extends BaseDuoSecurityAuthenticationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicDuoSecurityAuthenticationService.class);
    private static final long serialVersionUID = -6690808348975271382L;

    /**
     * Creates the duo authentication service.
     *
     * @param duoProperties Duo authentication properties
     * @param httpClient http client used to run the requests
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
        if (creds instanceof DuoDirectCredential) {
            return authenticateDuoCredentialDirect(creds);
        }
        return authenticateDuoCredential(creds);
    }

    private Pair<Boolean, String> authenticateDuoCredentialDirect(final Credential crds) {
        try {
            final DuoDirectCredential credential = DuoDirectCredential.class.cast(crds);
            final Principal p = credential.getAuthentication().getPrincipal();
            final Http request = buildHttpPostAuthRequest();
            signHttpAuthRequest(request, p.getId());
            final JSONObject result = (JSONObject) request.executeRequest();
            LOGGER.debug("Duo authentication response: [{}]", result);
            if ("allow".equalsIgnoreCase(result.getString("result"))) {
                return Pair.of(Boolean.TRUE, crds.getId());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return Pair.of(Boolean.FALSE, crds.getId());
    }

    private Pair<Boolean, String> authenticateDuoCredential(final Credential creds) throws Exception {
        final String signedRequestToken = DuoCredential.class.cast(creds).getSignedDuoResponse();
        if (StringUtils.isBlank(signedRequestToken)) {
            throw new IllegalArgumentException("No signed request token was passed to verify");
        }

        LOGGER.debug("Calling DuoWeb.verifyResponse with signed request token '[{}]'", signedRequestToken);
        final String result = DuoWeb.verifyResponse(duoProperties.getDuoIntegrationKey(),
                duoProperties.getDuoSecretKey(),
                duoProperties.getDuoApplicationKey(), signedRequestToken);
        return Pair.of(Boolean.TRUE, result);
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
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .toHashCode();
    }
}
