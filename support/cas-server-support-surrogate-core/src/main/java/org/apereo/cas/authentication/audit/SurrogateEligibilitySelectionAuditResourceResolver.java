package org.apereo.cas.authentication.audit;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.springframework.webflow.execution.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link SurrogateEligibilitySelectionAuditResourceResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class SurrogateEligibilitySelectionAuditResourceResolver extends ReturnValueAsStringResourceResolver {

    @Override
    public String[] resolveFrom(final JoinPoint auditableTarget, final Object returnValue) {
        Objects.requireNonNull(returnValue, "Event must not be null");
        val resultEvent = (Event) returnValue;
        if (resultEvent.getAttributes().contains("result")) {
            val values = new HashMap<String, Object>(resultEvent.getAttributes().get("result", Map.class));
            values.put("status", resultEvent.getId());
            return new String[]{auditFormat.serialize(values)};
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
