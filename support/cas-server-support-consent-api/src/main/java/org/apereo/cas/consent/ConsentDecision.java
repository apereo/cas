package org.apereo.cas.consent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

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
    private static final long serialVersionUID = -3240292729509593433L;

    @Id
    @Transient
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

    @Lob
    @Column(name = "attributes", length = Integer.MAX_VALUE)
    private String attributes;

    public ConsentDecision() {
        this.id = System.currentTimeMillis();
    }
}
