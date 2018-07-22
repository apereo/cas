package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.interrupt.InterruptResponse;

import lombok.experimental.UtilityClass;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InterruptUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@UtilityClass
public class InterruptUtils {
    /**
     * Gets interrupt from.
     *
     * @param ctx the ctx
     * @return the interrupt from
     */
    public static InterruptResponse getInterruptFrom(final RequestContext ctx) {
        return ctx.getFlowScope().get("interrupt", InterruptResponse.class);
    }

    /**
     * Put interrupt in.
     *
     * @param requestContext the request context
     * @param response       the response
     */
    public static void putInterruptIn(final RequestContext requestContext, final InterruptResponse response) {
        requestContext.getFlowScope().put("interrupt", response);
    }
}
