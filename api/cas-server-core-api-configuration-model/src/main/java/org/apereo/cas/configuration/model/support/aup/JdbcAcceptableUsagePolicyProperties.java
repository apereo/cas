package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link JdbcAcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-aup-jdbc")
@Getter
@Setter
@Accessors(chain = true)
public class JdbcAcceptableUsagePolicyProperties extends AbstractJpaProperties {

    private static final long serialVersionUID = -1325011278378393385L;

    /**
     * The table name in the database that holds the AUP attribute to update for the user.
     */
    private String tableName;

    /**
     * The column to store the AUP attribute. May differ from the profile attribute defined
     * by {@link AcceptableUsagePolicyProperties#getAupAttributeName()}.
     * SQL query can be further customized by setting {@link #sqlUpdate}.
     */
    private String aupColumn;

    /**
     * The column to identify the principal.
     * SQL query can be further customized by setting {@link #sqlUpdate}.
     */
    private String principalIdColumn = "username";

    /**
     * The profile attribute to extract the value for the {@link #principalIdColumn} used in the WHERE clause
     * of {@link #sqlUpdate}. If empty, the principal ID will be used.
     */
    private String principalIdAttribute;

    /**
     * The query template to update the AUP attribute.
     * %s placeholders represent {@link #tableName}, {@link #aupColumn}, {@link #principalIdColumn} settings.
     */
    private String sqlUpdate = "UPDATE %s SET %s=true WHERE %s=?";
}
