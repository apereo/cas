package org.apereo.cas.acct;

import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ServiceTicketGeneratorAuthority;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.Strings;

/**
 * This is {@link AccountProfileServiceTicketGeneratorAuthority}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class AccountProfileServiceTicketGeneratorAuthority implements ServiceTicketGeneratorAuthority {
    private final CasConfigurationProperties casProperties;

    @Override
    public boolean supports(final AuthenticationResult authenticationResult, final Service service) {
        val serviceUrl = Strings.CI.appendIfMissing(casProperties.getServer().getPrefix(), "/")
            .concat(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        return service != null && service.getId().equals(serviceUrl);
    }

    @Override
    public boolean shouldGenerate(final AuthenticationResult authenticationResult, final Service service) {
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
