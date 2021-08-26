package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * This is {@link AuthenticationPolicyExecutionResult}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@SuperBuilder
@ToString
public class AuthenticationPolicyExecutionResult implements Serializable {
    private static final long serialVersionUID = -6607624825058147653L;

    private final boolean success;

    /**
     * Failure authentication policy.
     *
     * @return the authentication policy execution result
     */
    public static AuthenticationPolicyExecutionResult failure() {
        return AuthenticationPolicyExecutionResult.builder().success(false).build();
    }

    /**
     * Success authentication policy via a condition.
     *
     * @param condition the condition
     * @return the authentication policy execution result
     */
    public static AuthenticationPolicyExecutionResult success(final boolean condition) {
        return AuthenticationPolicyExecutionResult.builder().success(condition).build();
    }

    /**
     * Success authentication policy.
     *
     * @return the authentication policy execution result
     */
    public static AuthenticationPolicyExecutionResult success() {
        return success(true);
    }
}
