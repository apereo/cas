package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.webflow.execution.RequestContext;

import javax.sql.DataSource;

/**
 * This is {@link JdbcAcceptableUsagePolicyRepository}.
 * Examines the principal attribute collection to determine if
 * the policy has been accepted, and if not, allows for a configurable
 * way so that user's choice can later be remembered and saved back into
 * the jdbc instance.
 *
 * @author Misagh Moayyed
 * @since 5.2
 */
public class JdbcAcceptableUsagePolicyRepository extends AbstractPrincipalAttributeAcceptableUsagePolicyRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcAcceptableUsagePolicyRepository.class);

    private static final long serialVersionUID = 1600024683199961892L;

    private final JdbcTemplate jdbcTemplate;
    private final String tableName;

    public JdbcAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport,
                                               final String aupAttributeName,
                                               final DataSource dataSource,
                                               final String tableName) {
        super(ticketRegistrySupport, aupAttributeName);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.tableName = tableName;
    }

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        try {
            final String sql = String.format("UPDATE %s SET %s=true WHERE username=?", this.tableName, this.aupAttributeName);
            return this.jdbcTemplate.update(sql, credential.getId()) > 0;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
