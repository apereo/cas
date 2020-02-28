package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.springframework.data.annotation.Id;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * This is {@link OneTimeToken}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@MappedSuperclass
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
@Setter
@EqualsAndHashCode
public class OneTimeToken implements Serializable, Comparable<OneTimeToken> {

    private static final long serialVersionUID = -1329938047176583075L;

    @Id
    @JsonProperty("id")
    @Transient
    private long id;

    @Column(nullable = false)
    private Integer token;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDateTime issuedDateTime = LocalDateTime.now(ZoneId.systemDefault());

    public OneTimeToken() {
        setId(System.currentTimeMillis());
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
            .append(id, o.id)
            .build();
    }
}
