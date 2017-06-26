package org.apereo.cas.api;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.webflow.execution.Event;

/**
 * This is {@link AuthenticationRiskContingencyResponse}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AuthenticationRiskContingencyResponse {
    private final Event result;

    public AuthenticationRiskContingencyResponse(final Event result) {
        this.result = result;
    }

    public Event getResult() {
        return result;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("result", result.getId())
                .toString();
    }
}
