package org.apereo.cas.consent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * This is {@link ConsentDecision}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@MappedSuperclass
@ToString
@Getter
@Setter
public class ConsentDecision implements Serializable {
    @Serial
    private static final long serialVersionUID = -3240292729509593433L;

    @Id
    @Transient
    @JsonSerialize(using = ToStringSerializer.class)
    private long id;

    @Column(nullable = false)
    private String principal;

    @Column(nullable = false)
    private String service;

    @Column(nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now(ZoneId.systemDefault());

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
    public ConsentDecision() {
        this.id = System.currentTimeMillis();
    }
}
