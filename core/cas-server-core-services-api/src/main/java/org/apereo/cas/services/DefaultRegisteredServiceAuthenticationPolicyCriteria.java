package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link DefaultRegisteredServiceAuthenticationPolicyCriteria}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
@EqualsAndHashCode
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultRegisteredServiceAuthenticationPolicyCriteria implements RegisteredServiceAuthenticationPolicyCriteria {
    private static final long serialVersionUID = -2905826778096374574L;

    private boolean tryAll;

    private AuthenticationPolicyTypes type;

    private String script;
}
