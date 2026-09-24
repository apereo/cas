package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.util.RandomUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jspecify.annotations.NonNull;
import org.springframework.data.annotation.Id;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

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
@SuppressWarnings("NullAway.Init")
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
    public int compareTo(@NonNull final OneTimeToken token) {
        return Comparator
            .comparing(OneTimeToken::getToken)
            .thenComparing(OneTimeToken::getUserId)
            .thenComparing(OneTimeToken::getIssuedDateTime)
            .thenComparingLong(OneTimeToken::getId)
            .compare(this, token);
    }

    /**
     * Assigns an identifier if one has not been given already. The identifier is drawn at random
     * rather than taken from the clock: it is the primary key in some of the stores behind this
     * record, and a clock reading only has millisecond resolution, so two records created in the
     * same millisecond would share a key and one would silently overwrite the other.
     *
     * @return this record, with an identifier assigned
     */
    @CanIgnoreReturnValue
    public OneTimeToken assignIdIfNecessary() {
        if (getId() <= 0) {
            setId(RandomUtils.nextLong(1, Long.MAX_VALUE));
        }
        return this;
    }
}
