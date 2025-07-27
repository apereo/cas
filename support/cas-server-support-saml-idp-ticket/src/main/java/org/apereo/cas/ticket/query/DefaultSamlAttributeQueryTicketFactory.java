package org.apereo.cas.ticket.query;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.opensaml.saml.common.SAMLObject;
import java.util.Objects;

/**
 * Factory to create OAuth access tokens.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
public class DefaultSamlAttributeQueryTicketFactory implements SamlAttributeQueryTicketFactory {

    @Getter
    protected final ExpirationPolicyBuilder expirationPolicyBuilder;

    protected final ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    protected final OpenSamlConfigBean configBean;

    @Getter
    protected final UniqueTicketIdGenerator ticketIdGenerator = UniqueTicketIdGenerator.prefixedTicketIdGenerator();

    @Override
    public SamlAttributeQueryTicket create(final String id,
                                           final SAMLObject samlObject,
                                           final String relyingParty,
                                           final TicketGrantingTicket ticketGrantingTicket) {
        return FunctionUtils.doUnchecked(() -> {
            try (val transformSamlObject = SamlUtils.transformSamlObject(this.configBean, samlObject)) {
                val codeId = createTicketIdFor(id, relyingParty);
                val service = webApplicationServiceFactory.createService(relyingParty);
                service.getAttributes().put(TicketGrantingTicket.class.getSimpleName(), CollectionUtils.wrapList(ticketGrantingTicket.getId()));
                service.getAttributes().put(RegisteredService.class.getSimpleName(), CollectionUtils.wrapList(relyingParty));
                service.getAttributes().put("owner", CollectionUtils.wrapList(getTicketType().getName()));
                return new SamlAttributeQueryTicketImpl(codeId, service,
                    expirationPolicyBuilder.buildTicketExpirationPolicy(),
                    relyingParty,
                    transformSamlObject.toString(),
                    Objects.requireNonNull(ticketGrantingTicket).getAuthentication());
            }
        });
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return SamlAttributeQueryTicket.class;
    }
}
