package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.List;

/**
 * This is {@link AuthenticationHandlerExecutionResult} that describes the result of an authentication attempt.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface AuthenticationHandlerExecutionResult extends Serializable {

    /**
     * Gets handler name.
     *
     * @return the handler name
     */
    String getHandlerName();

    /**
     * Gets credential meta data.
     *
     * @return the credential meta data
     */
    CredentialMetaData getCredentialMetaData();

    /**
     * Gets principal.
     *
     * @return the principal
     */
    Principal getPrincipal();

    /**
     * Gets warnings.
     *
     * @return the warnings
     */
    List<MessageDescriptor> getWarnings();

    /**
     * Add warning to authentication handler execution result.
     *
     * @param message the message
     * @return the authentication handler execution result
     */
    AuthenticationHandlerExecutionResult addWarning(MessageDescriptor message);

    /**
     * Clear warnings from authentication handler execution result.
     *
     * @return the authentication handler execution result
     */
    AuthenticationHandlerExecutionResult clearWarnings();
}
