package org.apereo.cas.audit;

import org.apereo.cas.audit.generic.JdbcAuditTrailEntity;
import org.apereo.cas.audit.mssql.MsSqlServerJdbcAuditTrailEntity;
import org.apereo.cas.audit.mysql.MySQLJdbcAuditTrailEntity;
import org.apereo.cas.audit.oracle.OracleJdbcAuditTrailEntity;
import org.apereo.cas.audit.postgres.PostgresJdbcAuditTrailEntity;
import org.apereo.cas.audit.spi.entity.AuditTrailEntity;
import org.apereo.cas.jpa.AbstractJpaEntityFactory;

/**
 * This is {@link JdbcAuditTrailEntityFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class JdbcAuditTrailEntityFactory extends AbstractJpaEntityFactory<AuditTrailEntity> {
    public JdbcAuditTrailEntityFactory(final String dialect) {
        super(dialect);
    }

    @Override
    public Class<AuditTrailEntity> getType() {
        return (Class<AuditTrailEntity>) getEntityClass();
    }

    private Class<? extends AuditTrailEntity> getEntityClass() {
        if (isOracle()) {
            return OracleJdbcAuditTrailEntity.class;
        }
        if (isMySql() || isMariaDb()) {
            return MySQLJdbcAuditTrailEntity.class;
        }
        if (isPostgres()) {
            return PostgresJdbcAuditTrailEntity.class;
        }
        if (isMsSqlServer()) {
            return MsSqlServerJdbcAuditTrailEntity.class;
        }
        return JdbcAuditTrailEntity.class;
    }
}
