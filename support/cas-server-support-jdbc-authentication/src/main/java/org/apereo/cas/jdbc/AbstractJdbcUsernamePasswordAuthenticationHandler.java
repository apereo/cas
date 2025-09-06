package org.apereo.cas.jdbc;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.model.support.jdbc.authn.BaseJdbcAuthenticationProperties;
import org.apereo.cas.util.CollectionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for database authentication handlers.
 *
 * @author Scott Battaglia
 * @since 3.0.0.3
 */
@Getter
@Slf4j
public abstract class AbstractJdbcUsernamePasswordAuthenticationHandler<T extends BaseJdbcAuthenticationProperties> extends AbstractUsernamePasswordAuthenticationHandler {

    protected final JdbcTemplate jdbcTemplate;

    protected final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected final DataSource dataSource;

    protected final T properties;

    protected AbstractJdbcUsernamePasswordAuthenticationHandler(final T properties,

                                                                final PrincipalFactory principalFactory,
                                                                final DataSource dataSource) {
        super(properties.getName(), principalFactory, properties.getOrder());
        this.properties = properties;
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    protected Map<String, List<Object>> collectPrincipalAttributes(final Map<String, Object> dbFields) {
        val attributes = new HashMap<String, List<Object>>();
        val principalAttributeMap = CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(properties.getPrincipalAttributeList());
        principalAttributeMap.forEach((key, names) -> {
            val attribute = dbFields.get(key);
            if (attribute != null) {
                LOGGER.debug("Found attribute [{}] from the query results", key);
                val attributeNames = CollectionUtils.toCollection(names);
                attributeNames.forEach(attrName -> {
                    LOGGER.debug("Principal attribute [{}] is virtually remapped/renamed to [{}]", key, attrName);
                    attributes.put(attrName.toString(), CollectionUtils.wrap(attribute.toString()));
                });
            } else {
                LOGGER.warn("Requested attribute [{}] could not be found in the query results", key);
            }
        });
        return attributes;
    }
}
