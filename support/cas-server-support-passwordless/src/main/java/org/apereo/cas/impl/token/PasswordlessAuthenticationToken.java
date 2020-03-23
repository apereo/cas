package org.apereo.cas.impl.token;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link PasswordlessAuthenticationToken}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@MappedSuperclass
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Builder
public class PasswordlessAuthenticationToken implements Serializable {
    private static final long serialVersionUID = 3810773120720229099L;

    @Id
    @Transient
    @JsonProperty
    private long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String token;

    @Column(name = "EXP_DATE", length = Integer.MAX_VALUE, nullable = false)
    private ZonedDateTime expirationDate;

    @JsonIgnore
    public boolean isExpired() {
        val now = ZonedDateTime.now(ZoneOffset.UTC);
        return now.isAfter(getExpirationDate()) || now.isEqual(getExpirationDate());
    }
}
