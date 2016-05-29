package org.apereo.cas.audit.spi.config;

import org.apereo.cas.audit.spi.MessageBundleAwareResourceResolver;
import org.apereo.cas.audit.spi.TicketAsFirstParameterResourceResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasCoreAuditConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreAuditConfiguration")
public class CasCoreAuditConfiguration {

    /**
     * Ticket as first parameter resource resolver.
     *
     * @return the ticket as first parameter resource resolver
     */
    @Bean(name="ticketResourceResolver")
    public TicketAsFirstParameterResourceResolver ticketResourceResolver() {
        return new TicketAsFirstParameterResourceResolver();
    }

    /**
     * Message bundle aware resource resolver.
     *
     * @return the message bundle aware resource resolver
     */
    @Bean(name="messageBundleAwareResourceResolver")
    public MessageBundleAwareResourceResolver messageBundleAwareResourceResolver() {
        return new MessageBundleAwareResourceResolver();
    }
}
