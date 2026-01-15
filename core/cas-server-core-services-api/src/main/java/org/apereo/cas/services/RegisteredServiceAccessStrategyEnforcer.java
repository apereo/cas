package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.audit.AuditableExecution;

/**
 * This is {@link RegisteredServiceAccessStrategyEnforcer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface RegisteredServiceAccessStrategyEnforcer extends AuditableExecution {
}
