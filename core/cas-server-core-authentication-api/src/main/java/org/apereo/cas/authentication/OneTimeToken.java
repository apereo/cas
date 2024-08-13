package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.springframework.data.annotation.Id;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.io.Serial;
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
@NoArgsConstructor
public class OneTimeToken implements Serializable, Comparable<OneTimeToken> {

    @Serial
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

    public OneTimeToken(final Integer token, final String userId) {
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

    /**
     * Assign id if undefined.
     *
     * @return the registered service
     */
    @CanIgnoreReturnValue
    public OneTimeToken assignIdIfNecessary() {
        if (getId() <= 0) {
            setId(System.currentTimeMillis());
        }
        return this;
    }
}
