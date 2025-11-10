package org.apereo.cas.pm;

/**
 * This is {@link PasswordManagementExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@FunctionalInterface
public interface PasswordManagementExecutionPlan {

    /**
     * Register password management service.
     *
     * @return the password management service
     */
    PasswordManagementService registerPasswordManagementService();
}
