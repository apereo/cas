package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
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
public class OneTimeTokenAccount implements Serializable, Comparable<OneTimeTokenAccount>, Cloneable {
    /**
     * Table name used to hold otp scratch codes.
     */
    public static final String TABLE_NAME_SCRATCH_CODES = "scratch_codes";

    private static final long serialVersionUID = -8289105320642735252L;

    @Id
    @Transient
    @JsonProperty("id")
    private long id = -1;

    @Column(nullable = false, length = 2048)
    private String secretKey;

    @Column(nullable = false)
    private int validationCode;

    @ElementCollection
    @CollectionTable(name = TABLE_NAME_SCRATCH_CODES, joinColumns = @JoinColumn(name = "username"))
    @Column(nullable = false)
    private List<Integer> scratchCodes = new ArrayList<>(0);

    @Column(nullable = false, unique = true)
    private String username;

    @Column
    private ZonedDateTime registrationDate = ZonedDateTime.now(ZoneOffset.UTC);

    public OneTimeTokenAccount() {
        setId(System.currentTimeMillis());
    }

    /**
     * Instantiates a new Google authenticator account.
     *
     * @param username       the user id
     * @param secretKey      the secret key
     * @param validationCode the validation code
     * @param scratchCodes   the scratch codes
     */
    public OneTimeTokenAccount(final String username, final String secretKey, final int validationCode, final List<Integer> scratchCodes) {
        this();
        this.secretKey = secretKey;
        this.validationCode = validationCode;
        this.scratchCodes = scratchCodes;
        this.username = username;
    }

    @JsonCreator
    public OneTimeTokenAccount(@JsonProperty("username") final String username,
                               @JsonProperty("secretKey") final String secretKey,
                               @JsonProperty("validationCode") final int validationCode,
                               @JsonProperty("scratchCodes") final List<Integer> scratchCodes,
                               @JsonProperty("registrationDate") final ZonedDateTime registrationDate) {
        this(username, secretKey, validationCode, scratchCodes);
        this.registrationDate = registrationDate;
    }

    @Override
    public int compareTo(final OneTimeTokenAccount o) {
        return new CompareToBuilder()
            .append(this.scratchCodes.toArray(), o.getScratchCodes().toArray())
            .append(this.validationCode, o.getValidationCode())
            .append(this.secretKey, o.getSecretKey())
            .append(this.username, o.getUsername()).build();
    }

    @Override
    @SneakyThrows
    public OneTimeTokenAccount clone() {
        return (OneTimeTokenAccount) super.clone();
    }
}
