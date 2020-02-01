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
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link PasswordlessAuthenticationToken}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Entity
@Table(name = "PasswordlessAuthenticationToken")
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
    @org.springframework.data.annotation.Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
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
