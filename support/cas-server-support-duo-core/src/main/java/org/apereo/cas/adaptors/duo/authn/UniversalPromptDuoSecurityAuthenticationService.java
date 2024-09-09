package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpClient;
import com.duosecurity.Client;
import com.duosecurity.model.Token;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * An abstraction that encapsulates interaction with
 * Duo 2fa authentication service via its universal prompt API.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class UniversalPromptDuoSecurityAuthenticationService extends BaseDuoSecurityAuthenticationService {
    @Serial
    private static final long serialVersionUID = -1690808348975271382L;

    private final Client duoClient;

    public UniversalPromptDuoSecurityAuthenticationService(
        final DuoSecurityMultifactorAuthenticationProperties duoProperties,
        final HttpClient httpClient,
        final Client duoClient,
        final List<MultifactorAuthenticationPrincipalResolver> multifactorAuthenticationPrincipalResolver,
        final Cache<String, DuoSecurityUserAccount> userAccountCache) {
        super(duoProperties, httpClient, multifactorAuthenticationPrincipalResolver, userAccountCache);
        this.duoClient = duoClient;
    }

    @Override
    public Optional<Object> getDuoClient() {
        return Optional.of(this.duoClient);
    }

    @Override
    public DuoSecurityAuthenticationResult authenticateInternal(final Credential credential) throws Exception {
        val duoCredential = (DuoSecurityUniversalPromptCredential) credential;
        LOGGER.trace("Exchanging Duo Security authorization code [{}]", credential.getId());

        val principalId = getDuoPrincipalId(duoCredential);
        val result = duoClient.exchangeAuthorizationCodeFor2FAResult(credential.getId(), principalId);
        LOGGER.debug("Validated Duo Security code [{}] with result [{}]", credential.getId(), result);

        val username = StringUtils.defaultIfBlank(result.getPreferred_username(), result.getSub());
        val attributes = new LinkedHashMap<String, List<Object>>();
        if (getProperties().isCollectDuoAttributes()) {
            attributes.putAll(collectDuoAuthenticationAttributes(result));
            attributes.putAll(collectDuoAuthenticationContextAttributes(result));
            attributes.putAll(collectDuoAuthenticationResultAttributes(result));
        }
        return DuoSecurityAuthenticationResult.builder()
            .success(true)
            .username(username)
            .attributes(attributes)
            .build();
    }

    protected Map<String, List<Object>> collectDuoAuthenticationAttributes(final Token result) {
        val attributes = new LinkedHashMap<String, List<Object>>();
        attributes.put("duoExp", CollectionUtils.wrap(result.getExp()));
        attributes.put("duoIss", CollectionUtils.wrap(result.getIss()));
        attributes.put("duoIat", CollectionUtils.wrap(result.getIat()));
        attributes.put("duoAuthTime", CollectionUtils.wrap(result.getAuth_time()));
        attributes.put("duoSub", CollectionUtils.wrap(result.getSub()));
        attributes.put("duoPreferredUsername", CollectionUtils.wrap(result.getPreferred_username()));
        attributes.put("duoAud", CollectionUtils.wrap(result.getAud()));
        return attributes;
    }

    protected Map<String, List<Object>> collectDuoAuthenticationResultAttributes(final Token result) {
        val attributes = new LinkedHashMap<String, List<Object>>();
        val authResult = result.getAuth_result();
        if (authResult != null) {
            attributes.put("duoAuthResult", CollectionUtils.wrap(authResult.getResult()));
            attributes.put("duoAuthResultStatus", CollectionUtils.wrap(authResult.getStatus()));
            attributes.put("duoAuthResultStatusMessage", CollectionUtils.wrap(authResult.getStatus_msg()));
        }
        return attributes;
    }

    protected Map<String, List<Object>> collectDuoAuthenticationContextAttributes(final Token result) {
        val authContext = result.getAuth_context();
        val attributes = new LinkedHashMap<String, List<Object>>();
        if (authContext != null) {
            attributes.put("duoAuthCtxEventType", CollectionUtils.wrap(authContext.getEvent_type()));
            attributes.put("duoAuthCtxFactor", CollectionUtils.wrap(authContext.getFactor()));
            attributes.put("duoAuthCtxReason", CollectionUtils.wrap(authContext.getReason()));
            attributes.put("duoAuthCtxResult", CollectionUtils.wrap(authContext.getResult()));
            attributes.put("duoAuthCtxTimestamp", CollectionUtils.wrap(authContext.getTimestamp()));
            attributes.put("duoAuthCtxTxId", CollectionUtils.wrap(authContext.getTxid()));
            attributes.put("duoAuthCtxUserKey", CollectionUtils.wrap(authContext.getUser().getKey()));

            val accessDevice = authContext.getAccess_device();
            if (accessDevice != null) {
                if (StringUtils.isNotBlank(accessDevice.getHostname())) {
                    attributes.put("duoAuthCtxAccessDeviceHostname", CollectionUtils.wrap(accessDevice.getHostname()));
                }
                attributes.put("duoAuthCtxAccessDeviceIp", CollectionUtils.wrap(accessDevice.getIp()));
                val accessLocation = accessDevice.getLocation();
                if (accessLocation != null) {
                    attributes.put("duoAuthCtxAccessDeviceLocationCity", CollectionUtils.wrap(accessLocation.getCity()));
                    attributes.put("duoAuthCtxAccessDeviceLocationCountry", CollectionUtils.wrap(accessLocation.getCountry()));
                    attributes.put("duoAuthCtxAccessDeviceLocationState", CollectionUtils.wrap(accessLocation.getState()));
                }
            }

            val application = authContext.getApplication();
            if (application != null) {
                attributes.put("duoAuthCtxApplicationName", CollectionUtils.wrap(application.getName()));
            }

            val authDevice = authContext.getAuth_device();
            if (authDevice != null) {
                attributes.put("duoAuthCtxAuthDeviceHostname", CollectionUtils.wrap(authDevice.getName()));
                attributes.put("duoAuthCtxAuthDeviceIp", CollectionUtils.wrap(authDevice.getIp()));

                val authLocation = authDevice.getLocation();
                if (authLocation != null) {
                    attributes.put("duoAuthCtxAuthDeviceLocationCity", CollectionUtils.wrap(authLocation.getCity()));
                    attributes.put("duoAuthCtxAuthDeviceLocationCountry", CollectionUtils.wrap(authLocation.getCountry()));
                    attributes.put("duoAuthCtxAuthDeviceLocationState", CollectionUtils.wrap(authLocation.getState()));
                }
            }
        }
        return attributes;
    }

    protected String getDuoPrincipalId(final DuoSecurityUniversalPromptCredential duoCredential) {
        val principal = resolvePrincipal(duoCredential.getAuthentication().getPrincipal());
        val principalAttribute = getProperties().getPrincipalAttribute();
        if (principal.getAttributes().containsKey(principalAttribute)) {
            return principal.getAttributes().get(principalAttribute).getFirst().toString();
        }
        return principal.getId();
    }

    @Override
    public boolean ping() {
        try {
            val response = duoClient.healthCheck();
            LOGGER.debug("Received Duo Security health check response [{}]", response);
            return true;
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
        }
        return false;
    }
}
