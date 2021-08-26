package org.apereo.cas.aup;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.configuration.model.support.aup.JdbcAcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
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
@Slf4j
public class JdbcAcceptableUsagePolicyRepository extends BaseAcceptableUsagePolicyRepository {
    private static final long serialVersionUID = 1600024683199961892L;

    private final transient JdbcTemplate jdbcTemplate;

    private final TransactionTemplate transactionTemplate;

    public JdbcAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport,
                                               final AcceptableUsagePolicyProperties aupProperties,
                                               final DataSource dataSource,
                                               final TransactionTemplate transactionTemplate) {
        super(ticketRegistrySupport, aupProperties);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public AcceptableUsagePolicyStatus verify(final RequestContext requestContext) {
        var status = super.verify(requestContext);
        if (!status.isAccepted()) {
            val jdbc = aupProperties.getJdbc();
            val aupColumnName = getAcceptableUsagePolicyColumnName(jdbc);
            val sql = String.format(jdbc.getSqlSelect(), aupColumnName, jdbc.getTableName(), jdbc.getPrincipalIdColumn());
            val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
            val principalId = determinePrincipalId(principal);
            LOGGER.debug("Executing search query [{}] for principal [{}]", sql, principalId);
            return this.transactionTemplate.execute(action -> {
                val acceptedFlag = this.jdbcTemplate.queryForObject(sql, String.class, principalId);
                return new AcceptableUsagePolicyStatus(BooleanUtils.toBoolean(acceptedFlag), status.getPrincipal());
            });
        }
        return status;
    }

    @Override
    public boolean submit(final RequestContext requestContext) {
        try {
            val jdbc = aupProperties.getJdbc();
            val aupColumnName = getAcceptableUsagePolicyColumnName(jdbc);
            val sql = String.format(jdbc.getSqlUpdate(), jdbc.getTableName(), aupColumnName, jdbc.getPrincipalIdColumn());
            val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
            val principalId = determinePrincipalId(principal);
            LOGGER.debug("Executing update query [{}] for principal [{}]", sql, principalId);
            return transactionTemplate.execute(action -> jdbcTemplate.update(sql, principalId) > 0);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    /**
     * Gets acceptable usage policy column name.
     *
     * @param jdbc the jdbc
     * @return the acceptable usage policy column name
     */
    protected String getAcceptableUsagePolicyColumnName(final JdbcAcceptableUsagePolicyProperties jdbc) {
        return StringUtils.defaultIfBlank(jdbc.getAupColumn(), aupProperties.getCore().getAupAttributeName()).trim();
    }

    /**
     * Extracts principal ID from a principal attribute or the provided credentials.
     *
     * @param principal the principal
     * @return the principal ID to update the AUP setting in the database for
     */
    protected String determinePrincipalId(final Principal principal) {
        if (StringUtils.isBlank(aupProperties.getJdbc().getPrincipalIdAttribute())) {
            return principal.getId();
        }
        val pIdAttribName = aupProperties.getJdbc().getPrincipalIdAttribute();
        if (!principal.getAttributes().containsKey(pIdAttribName)) {
            throw new IllegalStateException("Principal attribute [" + pIdAttribName + "] cannot be found");
        }
        val pIdAttributeValue = principal.getAttributes().get(pIdAttribName);
        val pIdAttributeValues = CollectionUtils.toCollection(pIdAttributeValue);
        var principalId = StringUtils.EMPTY;
        if (!pIdAttributeValues.isEmpty()) {
            principalId = pIdAttributeValues.iterator().next().toString().trim();
        }
        if (pIdAttributeValues.size() > 1) {
            LOGGER.warn("Principal attribute [{}] was found, but its value [{}] is multi-valued. "
                + "Proceeding with the first element [{}]", pIdAttribName, pIdAttributeValue, principalId);
        }
        if (principalId.isEmpty()) {
            throw new IllegalStateException("Principal attribute [" + pIdAttribName + "] was found, but it is either empty"
                + " or multi-valued with an empty element");
        }
        return principalId;
    }
}
