package org.apereo.cas.webauthn.web.flow;

import module java.base;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import com.yubico.core.RegistrationStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Decides whether to offer a passkey upgrade (WebAuthn conditional create) right after a login.
 * The upgrade is offered when the user submitted a username and password in this login flow, the resulting
 * authentication was established with a password credential, and the account may register another WebAuthn
 * credential. The submitted credential is read from the login flow's own scope, which multifactor subflows do not
 * change, so a password typed earlier in the single sign-on session does not count. The typed username, which the
 * password manager knows, and the display name are put into the flow scope for the upgrade page.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class WebAuthnCheckPasskeyUpgradeAction extends BaseCasWebflowAction {
    /**
     * Flow scope attribute that carries the username typed at login.
     */
    public static final String FLOW_SCOPE_PASSKEY_UPGRADE_USERNAME = "passkeyUpgradeUsername";

    /**
     * Flow scope attribute that carries the display name for the passkey.
     */
    public static final String FLOW_SCOPE_PASSKEY_UPGRADE_DISPLAY_NAME = "passkeyUpgradeDisplayName";

    private final RegistrationStorage webAuthnCredentialRepository;

    private final CasConfigurationProperties casProperties;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        if (!(requestContext.getFlowScope().get(CasWebflowConstants.VAR_ID_CREDENTIAL) instanceof final UsernamePasswordCredential credential)
            || StringUtils.isBlank(credential.getUsername())
            || authentication == null
            || authentication.getCredentials().stream().noneMatch(UsernamePasswordCredential.class::isInstance)) {
            return no();
        }
        val principal = authentication.getPrincipal();
        val core = casProperties.getAuthn().getMfa().getWebAuthn().getCore();
        if (!core.isMultipleDeviceRegistrationEnabled()
            && !webAuthnCredentialRepository.getRegistrationsByUsername(principal.getId()).isEmpty()) {
            LOGGER.debug("Passkey upgrade is skipped for [{}], who already has a registered device", principal.getId());
            return no();
        }
        val displayName = CollectionUtils.firstElement(principal.getAttributes().get(core.getDisplayNameAttribute()))
            .map(Object::toString)
            .orElseGet(principal::getId);
        val flowScope = requestContext.getFlowScope();
        flowScope.put(FLOW_SCOPE_PASSKEY_UPGRADE_USERNAME, credential.getUsername());
        flowScope.put(FLOW_SCOPE_PASSKEY_UPGRADE_DISPLAY_NAME, displayName);
        return yes();
    }
}
