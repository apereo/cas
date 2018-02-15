package org.apereo.cas.config;

import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.registry.JpaTicketRegistry;
import org.apereo.cas.ticket.registry.support.JpaTgtDeleteHandler;
import org.apereo.cas.ticket.registry.support.DefaultJpaTgtDeleteHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasOauthJdbcConfiguration}.
 *
 * @author sbearcsiro
 * @since 5.2.3
 */
@Configuration("casOauthJdbcConfiguration")
@ConditionalOnClass(JpaTicketRegistry.class)
public class CasOAuthJdbcConfiguration {

    @Bean
    public JpaTgtDeleteHandler oauthJpaTgtDeleteHandler(@Qualifier("ticketCatalog") final TicketCatalog ticketCatalog) {
        return new DefaultJpaTgtDeleteHandler(ticketCatalog, OAuthCode.PREFIX);
    }

}
