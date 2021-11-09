package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.CompareToBuilder;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link OneTimeTokenAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@MappedSuperclass
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString(exclude = {"secretKey", "scratchCodes"})
@Getter
@Setter
@EqualsAndHashCode
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class OneTimeTokenAccount implements Serializable, Comparable<OneTimeTokenAccount>, Cloneable {
    /**
     * Table name used to hold otp scratch codes.
     */
    public static final String TABLE_NAME_SCRATCH_CODES = "scratch_codes";

    private static final long serialVersionUID = -8289105320642735252L;

    @Id
    @Transient
    @JsonProperty("id")
    @Builder.Default
    private long id = System.currentTimeMillis();

    @Column(nullable = false, length = 2048)
    @JsonProperty("secretKey")
    private String secretKey;

    @Column(nullable = false)
    @JsonProperty("validationCode")
    private int validationCode;

    @ElementCollection
    @CollectionTable(name = TABLE_NAME_SCRATCH_CODES, joinColumns = @JoinColumn(name = "id"))
    @Column(nullable = false)
    @Builder.Default
    private List<Integer> scratchCodes = new ArrayList<>(0);

    @Column(nullable = false)
    @JsonProperty("username")
    private String username;

    @Column(nullable = false)
    @JsonProperty("name")
    private String name;

    @Column
    @JsonProperty("registrationDate")
    @Builder.Default
    private ZonedDateTime registrationDate = ZonedDateTime.now(ZoneOffset.UTC);

    @Override
    public int compareTo(final OneTimeTokenAccount o) {
        return new CompareToBuilder()
            .append(this.scratchCodes.toArray(), o.getScratchCodes().toArray())
            .append(this.validationCode, o.getValidationCode())
            .append(this.secretKey, o.getSecretKey())
            .append(this.username, o.getUsername())
            .append(this.name, o.getName())
            .build();
    }

    @Override
    @SneakyThrows
    public OneTimeTokenAccount clone() {
        return (OneTimeTokenAccount) super.clone();
    }
}
