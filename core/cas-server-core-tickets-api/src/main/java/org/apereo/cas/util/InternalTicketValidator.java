package org.apereo.cas.util;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.client.validation.TicketValidator;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a ticket validator that uses CAS back channels to validate ST.
 *
 * @author Kirill Gagarski
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class InternalTicketValidator implements TicketValidator {
    private final CentralAuthenticationService centralAuthenticationService;

    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @Override
    @SuppressWarnings("unchecked")
    public Assertion validate(final String ticketId, final String service) {
        val assertion = centralAuthenticationService.validateServiceTicket(ticketId,
            webApplicationServiceFactory.createService(service));
        val authentication = assertion.getPrimaryAuthentication();
        val principal = authentication.getPrincipal();
        val attrPrincipal = new AttributePrincipalImpl(principal.getId(), (Map) principal.getAttributes());
        val authenticationAttributes = new HashMap<>(authentication.getAttributes());
        authenticationAttributes.put(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN,
            CollectionUtils.wrap(assertion.isFromNewLogin()));
        authenticationAttributes.put(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME,
            CollectionUtils.wrap(CoreAuthenticationUtils.isRememberMeAuthentication(authentication, assertion)));
        return new AssertionImpl(attrPrincipal, (Map) authenticationAttributes);
    }
}
