package org.apereo.cas.web.flow;

import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.configuration.model.support.mfa.twilio.CasTwilioMultifactorAuthenticationProperties;
import org.apereo.cas.mfa.twilio.CasTwilioMultifactorAuthenticationProvider;
import org.apereo.cas.mfa.twilio.CasTwilioMultifactorAuthenticationService;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Objects;

/**
 * This is {@link CasTwilioMultifactorSendTokenAction}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasTwilioMultifactorSendTokenAction extends AbstractMultifactorAuthenticationAction<CasTwilioMultifactorAuthenticationProvider> {
    protected final CasTwilioMultifactorAuthenticationService casTwilioMultifactorAuthenticationService;

    protected final CasTwilioMultifactorAuthenticationProperties properties;

    protected final BucketConsumer bucketConsumer;

    protected final TenantExtractor tenantExtractor;

    @Override
    protected Event doPreExecute(final RequestContext requestContext) throws Exception {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val authentication = WebUtils.getAuthentication(requestContext);
        val result = bucketConsumer.consume(getThrottledRequestKeyFor(authentication, requestContext));
        result.getHeaders().forEach(response::addHeader);
        return result.isConsumed() ? super.doPreExecute(requestContext) : error();
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val service = WebUtils.getService(requestContext);
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = resolvePrincipal(authentication.getPrincipal(), requestContext);
        Objects.requireNonNull(principal, "Principal cannot be null");
        val success = casTwilioMultifactorAuthenticationService.generateToken(principal, service);
        if (success) {
            WebUtils.addInfoMessageToContext(requestContext, "cas.mfa.twilio.label.tokensent");
            return success();
        }
        return error();
    }
}
