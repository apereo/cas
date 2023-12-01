package org.apereo.cas.audit.oracle;

import org.apereo.cas.audit.generic.JdbcAuditTrailEntity;
import org.apereo.cas.audit.spi.entity.AuditTrailEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * This is {@link OracleJdbcAuditTrailEntity}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Entity(name = JdbcAuditTrailEntity.AUDIT_TRAIL_TABLE_NAME)
@Table(name = JdbcAuditTrailEntity.AUDIT_TRAIL_TABLE_NAME)
@SuperBuilder
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@AttributeOverrides({
    @AttributeOverride(name = "resource", column = @Column(name = "AUD_RESOURCE", columnDefinition = "clob")),
    @AttributeOverride(name = "headers", column = @Column(name = "AUD_HEADERS", columnDefinition = "clob")),
    @AttributeOverride(name = "extraInfo", column = @Column(name = "AUD_EXTRA_INFO", columnDefinition = "clob"))
})
public class OracleJdbcAuditTrailEntity extends AuditTrailEntity {
}
