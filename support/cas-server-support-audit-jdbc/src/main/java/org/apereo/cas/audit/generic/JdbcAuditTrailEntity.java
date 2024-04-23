package org.apereo.cas.audit.generic;

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
 * This is {@link JdbcAuditTrailEntity}.
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
@AttributeOverrides(@AttributeOverride(name = "resource", column = @Column(name = "AUD_RESOURCE", columnDefinition = "longvarchar")))
public class JdbcAuditTrailEntity extends AuditTrailEntity {
    /**
     * Audit table name.
     */
    public static final String AUDIT_TRAIL_TABLE_NAME = "COM_AUDIT_TRAIL";
}
