package org.apereo.cas.audit;

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

/**
 * This is {@link JdbcAuditTrailEntity}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Entity(name = "COM_AUDIT_TRAIL")
@SuperBuilder
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@AttributeOverrides(@AttributeOverride(name = "resource", column = @Column(name = "AUD_RESOURCE", columnDefinition = "text")))
public class JdbcAuditTrailEntity extends AuditTrailEntity {
}
