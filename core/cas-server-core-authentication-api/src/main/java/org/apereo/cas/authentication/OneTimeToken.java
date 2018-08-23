package org.apereo.cas.authentication;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.CompareToBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * This is {@link OneTimeToken}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@ToString
@Getter
@Setter
@EqualsAndHashCode
public class OneTimeToken implements Serializable, Comparable<OneTimeToken> {

    private static final long serialVersionUID = -1329938047176583075L;

    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private Integer token;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDateTime issuedDateTime = LocalDateTime.now();

    public OneTimeToken() {
        setId(java.lang.System.currentTimeMillis());
    }

    public OneTimeToken(final Integer token, final String userId) {
        this();
        this.token = token;
        this.userId = userId;
    }

    @Override
    public int compareTo(final OneTimeToken o) {
        return new CompareToBuilder()
            .append(token, o.getToken())
            .append(userId, o.getUserId())
            .append(issuedDateTime, o.getIssuedDateTime())
            .append(id, o.id).build();
    }
}
