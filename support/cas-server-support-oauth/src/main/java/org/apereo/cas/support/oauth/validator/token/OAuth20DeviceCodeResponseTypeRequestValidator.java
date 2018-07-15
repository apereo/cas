package org.apereo.cas.support.oauth.validator.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.pac4j.core.context.J2EContext;

/**
 * This is {@link OAuth20DeviceCodeResponseTypeRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class OAuth20DeviceCodeResponseTypeRequestValidator implements OAuth20TokenRequestValidator {
    private final ServicesManager servicesManager;
    private final TicketRegistry ticketRegistry;

    @Override
    public boolean validate(final J2EContext context) {
        return supports(context);
    }

    @Override
    public boolean supports(final J2EContext context) {
        val responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE);
        val clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID);
        return OAuth20Utils.isResponseType(responseType, OAuth20ResponseTypes.DEVICE_CODE)
            && StringUtils.isNotBlank(clientId);
    }
}
