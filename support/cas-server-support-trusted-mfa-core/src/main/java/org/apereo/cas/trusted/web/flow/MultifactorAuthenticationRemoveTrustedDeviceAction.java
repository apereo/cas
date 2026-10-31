package org.apereo.cas.trusted.web.flow;

import module java.base;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MultifactorAuthenticationRemoveTrustedDeviceAction}.
 * Only trusted devices recorded for the authenticated principal can be removed.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class MultifactorAuthenticationRemoveTrustedDeviceAction extends BaseCasWebflowAction {
    protected final MultifactorAuthenticationTrustStorage mfaTrustEngine;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val key = requestContext.getRequestParameters().getRequired("key");
        val authentication = WebUtils.getAuthentication(requestContext);
        val owned = authentication != null && mfaTrustEngine.get(authentication.getPrincipal().getId())
            .stream()
            .anyMatch(record -> key.equals(record.getRecordKey()));
        if (!owned) {
            LOGGER.warn("Trusted device [{}] is not registered for the authenticated principal and cannot be removed", key);
            return error();
        }
        mfaTrustEngine.remove(key);
        return success();
    }
}
