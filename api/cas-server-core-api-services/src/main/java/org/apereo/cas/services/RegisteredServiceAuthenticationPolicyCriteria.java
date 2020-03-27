package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

/**
 * This is {@link RegisteredServiceAuthenticationPolicyCriteria}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceAuthenticationPolicyCriteria extends Serializable {
    enum AuthenticationPolicyTypes {
        DEFAULT,
        ANY_AUTHENTICATION_HANDLER
    }

    AuthenticationPolicyTypes getType();

    boolean isTryAll();
}
