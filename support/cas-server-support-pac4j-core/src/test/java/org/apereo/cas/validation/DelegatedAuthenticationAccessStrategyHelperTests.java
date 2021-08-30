package org.apereo.cas.validation;


import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DelegatedAuthenticationAccessStrategyHelper}.
 *
 * @author Stefan Reiterer
 * @since 6.5
 */
@Tag("Delegation")
public class DelegatedAuthenticationAccessStrategyHelperTests {

    @Test
    public void isDelegatedClientAuthorizedForServiceIsNull() {
        val serviceManager = mock(ServicesManager.class);
        val delegatedAuthenticationPolicyEnforcer = mock(AuditableExecution.class);

        DelegatedAuthenticationAccessStrategyHelper delegatedAuthenticationAccessStrategyHelper =
                new DelegatedAuthenticationAccessStrategyHelper(serviceManager, delegatedAuthenticationPolicyEnforcer);

        val httpServletRequest = mock(HttpServletRequest.class);

        /*
        Without service delegated authentication policy cannot be evaluated and so Delegated Auth. should not be
        authorized for the given client-name
         */
        assertFalse(
                delegatedAuthenticationAccessStrategyHelper.isDelegatedClientAuthorizedFor(
                        "myClientName", null, httpServletRequest));
    }

    @Test
    public void isDelegatedClientAuthorizedForServiceIdIsEmpty() {
        val serviceManager = mock(ServicesManager.class);
        val delegatedAuthenticationPolicyEnforcer = mock(AuditableExecution.class);

        DelegatedAuthenticationAccessStrategyHelper delegatedAuthenticationAccessStrategyHelper =
                new DelegatedAuthenticationAccessStrategyHelper(serviceManager, delegatedAuthenticationPolicyEnforcer);

        val httpServletRequest = mock(HttpServletRequest.class);
        val service = mock(Service.class);
        when(service.getId()).thenReturn(StringUtils.EMPTY);

        /*
        Without service delegated authentication policy cannot be evaluated and so Delegated Auth. should not be
        authorized for the given client-name
         */
        assertFalse(
                delegatedAuthenticationAccessStrategyHelper.isDelegatedClientAuthorizedFor(
                        "myClientName", service, httpServletRequest));
    }

    @Test
    public void isDelegatedClientAuthorizedForKnownService() {
        val serviceManager = mock(ServicesManager.class);
        val delegatedAuthenticationPolicyEnforcer = mock(AuditableExecution.class);

        DelegatedAuthenticationAccessStrategyHelper delegatedAuthenticationAccessStrategyHelper =
                new DelegatedAuthenticationAccessStrategyHelper(serviceManager, delegatedAuthenticationPolicyEnforcer);

        val httpServletRequest = mock(HttpServletRequest.class);
        val service = mock(Service.class);
        when(service.getId()).thenReturn("serviceId");

        var registeredService = mock(RegisteredService.class);
        var accessStrategy = mock(RegisteredServiceAccessStrategy.class);
        when(serviceManager.findServiceBy(Mockito.eq(service))).thenReturn(registeredService);
        when(registeredService.getAccessStrategy()).thenReturn(accessStrategy);
        when(accessStrategy.isServiceAccessAllowed()).thenReturn(true);

        var auditableExecutionResult = mock(AuditableExecutionResult.class);
        when(delegatedAuthenticationPolicyEnforcer.execute(any())).thenReturn(auditableExecutionResult);
        when(auditableExecutionResult.isExecutionFailure()).thenReturn(false);

        assertTrue(
                delegatedAuthenticationAccessStrategyHelper.isDelegatedClientAuthorizedFor(
                        "myClientName", service, httpServletRequest));

        verify(delegatedAuthenticationPolicyEnforcer, times(1)).execute(any());
        verify(auditableExecutionResult, times(1)).isExecutionFailure();
        verify(serviceManager, times(1)).findServiceBy(Mockito.eq(service));
        verify(serviceManager, times(1)).findServiceBy(Mockito.eq(service));
        verify(accessStrategy, times(1)).isServiceAccessAllowed();
    }
}
