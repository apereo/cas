package org.apereo.cas.gauth.web.flow.account;

import module java.base;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GoogleMultifactorAuthenticationAccountProfileScratchCodesAction}.
 * Replaces the scratch codes of a Google Authenticator device that belongs to the authenticated
 * principal. The new codes are put into the flash scope, so the account profile shows them once.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class GoogleMultifactorAuthenticationAccountProfileScratchCodesAction extends BaseCasWebflowAction {
    /**
     * Flash scope attribute for the new scratch codes.
     */
    public static final String FLASH_SCOPE_ATTR_SCRATCH_CODES = "gauthScratchCodes";

    /**
     * Flash scope attribute for the name of the device whose scratch codes were replaced.
     */
    public static final String FLASH_SCOPE_ATTR_DEVICE_NAME = "gauthScratchCodesDevice";

    private final OneTimeTokenCredentialRepository repository;

    private final CasGoogleAuthenticator googleAuthenticator;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val deviceId = NumberUtils.toLong(requestContext.getRequestParameters().getRequired("id"), -1);
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal().getId();
        val account = repository.get(principal, deviceId);
        if (account == null) {
            LOGGER.warn("Google Authenticator device [{}] is not registered for [{}]", deviceId, principal);
            WebUtils.addErrorMessageToContext(requestContext, "screen.account.mfadevices.scratchcodes.failed");
            return error();
        }
        val scratchCodes = new ArrayList<Number>(googleAuthenticator.createCredentials().getScratchCodes());
        account.setScratchCodes(scratchCodes);
        repository.update(account);
        requestContext.getFlashScope().put(FLASH_SCOPE_ATTR_SCRATCH_CODES, scratchCodes.stream().map(String::valueOf).toList());
        requestContext.getFlashScope().put(FLASH_SCOPE_ATTR_DEVICE_NAME, account.getName());
        WebUtils.addInfoMessageToContext(requestContext, "screen.account.mfadevices.scratchcodes.regenerated");
        return success();
    }
}
