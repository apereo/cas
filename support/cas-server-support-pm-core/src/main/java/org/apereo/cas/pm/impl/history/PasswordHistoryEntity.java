package org.apereo.cas.pm.impl.history;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link PasswordHistoryEntity}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonInclude(JsonInclude.Include.NON_NULL)
@MappedSuperclass
public class PasswordHistoryEntity implements Serializable {
    private static final long serialVersionUID = -206561112913280345L;

    @Id
    @Transient
    private long id = -1;

    @Column(nullable = false)
    private String username;

    @Column(name = "password", length = 2048, nullable = false)
    private String password;

    @Column(nullable = false)
    private ZonedDateTime recordDate;

    public PasswordHistoryEntity() {
        this.id = System.currentTimeMillis();
        this.recordDate = ZonedDateTime.now(ZoneOffset.UTC);
    }
}
