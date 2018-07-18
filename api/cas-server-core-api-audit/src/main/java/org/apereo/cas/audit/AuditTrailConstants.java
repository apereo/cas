package org.apereo.cas.audit;

/**
 * This is {@link AuditTrailConstants}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public interface AuditTrailConstants {

    /**
     * Postfix for audit events that succeed.
     */
    String AUDIT_ACTION_POSTFIX_SUCCESS= "_SUCCESS";

    /**
     * Postfix for audit events that create.
     */
    String AUDIT_ACTION_POSTFIX_CREATED = "_CREATED";

    /**
     * Postfix for audit events that fail.
     */
    String AUDIT_ACTION_POSTFIX_FAILED = "_FAILED";

    /**
     * Postfix for audit events that trigger.
     */
    String AUDIT_ACTION_POSTFIX_TRIGGERED = "_TRIGGERED";
}
