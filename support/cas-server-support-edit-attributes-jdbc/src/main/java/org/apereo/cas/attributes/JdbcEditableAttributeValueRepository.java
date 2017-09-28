package org.apereo.cas.attributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link JdbcEditableAttributeValueRepository}. Stores editable
 * attributes inside an the jdbc instance.
 *
 * @author Marcus Watkins
 * @since 5.2
 */
public class JdbcEditableAttributeValueRepository extends AbstractPrincipalEditableAttributeValueRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcEditableAttributeValueRepository.class);

    private static final long serialVersionUID = 8900336240346167L;

    private final JdbcTemplate jdbcTemplate;
    private final String tableName;

    public JdbcEditableAttributeValueRepository(final TicketRegistrySupport ticketRegistrySupport,
            final DataSource dataSource, final String tableName) {
        super(ticketRegistrySupport);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.tableName = tableName;
    }

    @Override
    public boolean storeAttributeValues(final RequestContext requestContext, final Credential credential,
            final Map<String, String> attributeValues) {

        final Pair<String, List<String>> sqlParams = generateUpdateSql(tableName, credential.getId(), attributeValues);
        try {
            return jdbcTemplate.update(sqlParams.getLeft(), sqlParams.getRight().toArray()) > 0;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return false;
    }

    public static Pair<String, List<String>> generateUpdateSql(final String tableName, final String username,
            final Map<String, String> attributeValues) {
        final ArrayList<String> params = new ArrayList<>();
        final ArrayList<String> cols = new ArrayList<>();

        attributeValues.forEach((k, v) -> {
            cols.add(k + "=?");
            params.add(v);
        });

        final String sql = String.format("UPDATE %s SET %s WHERE username=?", tableName, String.join(",", cols));
        params.add(username);

        return Pair.of(sql, params);
    }
}
