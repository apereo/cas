package org.apereo.cas.acct.webflow;

import org.apereo.cas.acct.AccountRegistrationProperty;
import org.apereo.cas.acct.AccountRegistrationService;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * This is {@link LoadAccountRegistrationPropertiesAction}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class LoadAccountRegistrationPropertiesAction extends BaseCasWebflowAction {
    private final AccountRegistrationService accountRegistrationService;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val properties = accountRegistrationService.getAccountRegistrationPropertyLoader().load();
        requestContext.getFlowScope().put("registrationProperties", properties
            .values()
            .stream()
            .sorted(Comparator.comparing(AccountRegistrationProperty::getOrder))
            .collect(Collectors.toList()));
        return null;
    }
}
