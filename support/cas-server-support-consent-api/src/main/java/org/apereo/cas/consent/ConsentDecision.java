package org.apereo.cas.consent;

import module java.base;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * This is {@link ConsentDecision}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString
@Getter
@Setter
@Entity
@Table(name = "ConsentDecision")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@EqualsAndHashCode(of = {"id", "principal", "service", "tenant"})
public class ConsentDecision implements Serializable {
    @Serial
    private static final long serialVersionUID = -3240292729509593433L;

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String principal;

    @Column(nullable = false)
    private String service;

    /**
     * Date and time at which this decision was recorded, in UTC.
     * The field carries no zone of its own and is compared against the current time when the reminder
     * is evaluated, so it must be produced in UTC for every CAS node to agree on a decision's age
     * regardless of the time zone the node runs in.
     */
    @Column(nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now(ZoneOffset.UTC);

    @Column(nullable = false)
    private ConsentReminderOptions options = ConsentReminderOptions.ATTRIBUTE_NAME;

    @Column(nullable = false)
    private Long reminder = 14L;

    @Column(nullable = false)
    private ChronoUnit reminderTimeUnit = ChronoUnit.DAYS;

    @Column
    @JsonProperty("tenant")
    private String tenant;
    
    @Lob
    @Column(name = "attributes", length = Integer.MAX_VALUE)
    private String attributes;

    /**
     * Whether the decision belongs to the given tenant. A decision made in a tenant belongs only to that
     * tenant, and a decision made without a tenant belongs only outside of any tenant.
     *
     * @param tenantId the tenant id, or blank outside of any tenant
     * @return true if the decision belongs to the tenant
     */
    public boolean isAssignedToTenant(@Nullable final String tenantId) {
        return Strings.CI.equals(StringUtils.defaultString(tenant), StringUtils.defaultString(tenantId));
    }
}
