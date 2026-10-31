package org.apereo.cas.webauthn.web.flow.account;

import module java.base;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import com.yubico.core.RegistrationStorage;
import com.yubico.webauthn.data.ByteArray;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WebAuthnMultifactorAccountProfileRenameDeviceAction}.
 * Renames a passkey or security key that is registered to the authenticated principal.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class WebAuthnMultifactorAccountProfileRenameDeviceAction extends BaseCasWebflowAction {
    private static final int MAX_NAME_LENGTH = 100;

    private final RegistrationStorage webAuthnCredentialRepository;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val parameters = requestContext.getRequestParameters();
        val name = StringUtils.trimToEmpty(parameters.get("name"));
        val username = WebUtils.getAuthentication(requestContext).getPrincipal().getId();
        val registration = parseCredentialId(parameters.getRequired("id"))
            .flatMap(credentialId -> webAuthnCredentialRepository.getRegistrationByUsernameAndCredentialId(username, credentialId));
        if (name.isEmpty() || name.length() > MAX_NAME_LENGTH || registration.isEmpty()) {
            LOGGER.warn("Unable to rename WebAuthn device [{}] for [{}]", parameters.get("id"), username);
            WebUtils.addErrorMessageToContext(requestContext, "screen.account.mfadevices.rename.failed");
            return error();
        }
        val current = registration.get();
        webAuthnCredentialRepository.removeRegistrationByUsername(username, current);
        webAuthnCredentialRepository.addRegistrationByUsername(username, current.withCredentialNickname(name));
        WebUtils.addInfoMessageToContext(requestContext, "screen.account.mfadevices.rename.success");
        return success();
    }

    private static Optional<ByteArray> parseCredentialId(final String id) {
        try {
            return Optional.of(ByteArray.fromBase64Url(id));
        } catch (final Exception e) {
            LOGGER.debug("Invalid credential id [{}]", id);
            return Optional.empty();
        }
    }
}
