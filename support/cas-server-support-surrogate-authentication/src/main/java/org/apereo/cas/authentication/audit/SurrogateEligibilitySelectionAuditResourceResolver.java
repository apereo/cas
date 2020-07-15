package org.apereo.cas.authentication.audit;

import lombok.val;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apereo.inspektr.audit.spi.support.ReturnValueAsStringResourceResolver;
import org.aspectj.lang.JoinPoint;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;

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
        val resultEvent = Event.class.cast(returnValue);
        val builder = new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE);
        val values = (Map<String, Object>) resultEvent.getAttributes().get(new EventFactorySupport().getResultAttributeName(), Map.class);
        values.forEach(builder::append);
        builder.append("status", resultEvent.getId());
        return new String[]{builder.toString()};
    }
}
