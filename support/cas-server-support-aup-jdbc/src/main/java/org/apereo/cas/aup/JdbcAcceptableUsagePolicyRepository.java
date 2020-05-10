package org.apereo.cas.aup;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.configuration.model.support.aup.AcceptableUsagePolicyProperties;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
@Slf4j
public class JdbcAcceptableUsagePolicyRepository extends BaseAcceptableUsagePolicyRepository {
    private static final long serialVersionUID = 1600024683199961892L;

    private final transient JdbcTemplate jdbcTemplate;

    public JdbcAcceptableUsagePolicyRepository(final TicketRegistrySupport ticketRegistrySupport,
                                               final AcceptableUsagePolicyProperties aupProperties,
                                               final DataSource dataSource) {
        super(ticketRegistrySupport, aupProperties);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public boolean submit(final RequestContext requestContext, final Credential credential) {
        try {
            val jdbc = aupProperties.getJdbc();
            var aupColumnName = aupProperties.getAupAttributeName();
            if (StringUtils.isNotBlank(jdbc.getAupColumn())) {
                aupColumnName = jdbc.getAupColumn();
            }
            val sql = String.format(jdbc.getSqlUpdate(), jdbc.getTableName(), aupColumnName, jdbc.getPrincipalIdColumn());
            val principalId = determinePrincipalId(requestContext, credential);
            LOGGER.debug("Executing update query [{}] for principal [{}]", sql, principalId);
            return this.jdbcTemplate.update(sql, principalId) > 0;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Extracts principal ID from a principal attribute or the provided credentials.
     *
     * @param requestContext the context
     * @param credential     the credential
     * @return the principal ID to update the AUP setting in the database for
     */
    protected String determinePrincipalId(final RequestContext requestContext, final Credential credential) {
        if (StringUtils.isBlank(aupProperties.getJdbc().getPrincipalIdAttribute())) {
            return credential.getId();
        }
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
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
