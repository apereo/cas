package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpClient;

import com.duosecurity.Client;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
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
    private static final long serialVersionUID = -1690808348975271382L;

    private final Client duoClient;

    public UniversalPromptDuoSecurityAuthenticationService(
        final DuoSecurityMultifactorAuthenticationProperties duoProperties,
        final HttpClient httpClient,
        final CasConfigurationProperties casProperties) {
        this(duoProperties, httpClient, getDuoClient(duoProperties, casProperties));
    }

    UniversalPromptDuoSecurityAuthenticationService(
        final DuoSecurityMultifactorAuthenticationProperties duoProperties,
        final HttpClient httpClient,
        final Client duoClient) {
        super(duoProperties, httpClient);
        this.duoClient = duoClient;
    }

    @Override
    public DuoSecurityAuthenticationResult authenticateInternal(final Credential c) throws Exception {
        val credential = (DuoSecurityUniversalPromptCredential) c;
        LOGGER.trace("Exchanging Duo Security authorization code [{}]", credential.getId());
        val result = duoClient.exchangeAuthorizationCodeFor2FAResult(credential.getId(),
            credential.getAuthentication().getPrincipal().getId());
        LOGGER.debug("Validated Duo Security code [{}] with result [{}]", credential.getId(), result);

        val username = StringUtils.defaultIfBlank(result.getPreferred_username(), result.getSub());
        val attributes = new LinkedHashMap<String, List<Object>>();
        attributes.put("duoExp", CollectionUtils.wrap(result.getExp()));
        attributes.put("duoIss", CollectionUtils.wrap(result.getIss()));
        attributes.put("duoIat", CollectionUtils.wrap(result.getIat()));
        attributes.put("duoAuthTime", CollectionUtils.wrap(result.getAuth_time()));
        attributes.put("duoSub", CollectionUtils.wrap(result.getSub()));
        attributes.put("duoPreferredUsername", CollectionUtils.wrap(result.getPreferred_username()));
        attributes.put("duoAud", CollectionUtils.wrap(result.getAud()));

        val authContext = result.getAuth_context();
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

        val authResult = result.getAuth_result();
        if (authResult != null) {
            attributes.put("duoAuthResult", CollectionUtils.wrap(authResult.getResult()));
            attributes.put("duoAuthResultStatus", CollectionUtils.wrap(authResult.getStatus()));
            attributes.put("duoAuthResultStatusMessage", CollectionUtils.wrap(authResult.getStatus_msg()));
        }

        return DuoSecurityAuthenticationResult.builder()
            .success(true)
            .username(username)
            .attributes(attributes)
            .build();
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

    @Override
    public Optional<Object> getDuoClient() {
        return Optional.of(this.duoClient);
    }

    @SneakyThrows
    private static Client getDuoClient(final DuoSecurityMultifactorAuthenticationProperties duoProperties,
        final CasConfigurationProperties casProperties) {
        return new Client(duoProperties.getDuoIntegrationKey(),
            duoProperties.getDuoSecretKey(),
            duoProperties.getDuoApiHost(),
            casProperties.getServer().getLoginUrl());
    }
}
