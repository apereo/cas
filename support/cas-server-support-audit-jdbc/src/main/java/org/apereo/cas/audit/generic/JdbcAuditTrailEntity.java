package org.apereo.cas.audit.generic;

import module java.base;
import org.apereo.cas.audit.spi.entity.AuditTrailEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SuperBuilder;
import lombok.experimental.Accessors;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * This is {@link JdbcAuditTrailEntity}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Entity(name = JdbcAuditTrailEntity.AUDIT_TRAIL_TABLE_NAME)
@Table(name = JdbcAuditTrailEntity.AUDIT_TRAIL_TABLE_NAME,
    indexes = @Index(name = JdbcAuditTrailEntity.AUDIT_TRAIL_DATE_INDEX_NAME, columnList = "AUD_DATE,AUD_ACTION"))
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

    /**
     * Index on the audit date and action, for queries over a period such as summaries.
     */
    public static final String AUDIT_TRAIL_DATE_INDEX_NAME = "COM_AUDIT_TRAIL_DATE_IDX";
}
