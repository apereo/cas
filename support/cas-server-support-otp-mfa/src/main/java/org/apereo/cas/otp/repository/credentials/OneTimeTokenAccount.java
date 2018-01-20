package org.apereo.cas.otp.repository.credentials;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.CompareToBuilder;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link OneTimeTokenAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Slf4j
@ToString
@Getter
@Setter
@EqualsAndHashCode
public class OneTimeTokenAccount implements Serializable, Comparable<OneTimeTokenAccount> {

    private static final long serialVersionUID = -8289105320642735252L;

    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id = -1;

    @Column(nullable = false)
    private String secretKey;

    @Column(nullable = false)
    private int validationCode;

    @ElementCollection
    @CollectionTable(name = "scratch_codes", joinColumns = @JoinColumn(name = "username"))
    @Column(nullable = false)
    private List<Integer> scratchCodes = new ArrayList<>();

    @Column(nullable = false)
    private String username;

    public OneTimeTokenAccount() {
        setId(java.lang.System.currentTimeMillis());
    }

    /**
     * Instantiates a new Google authenticator account.
     *
     * @param username       the user id
     * @param secretKey      the secret key
     * @param validationCode the validation code
     * @param scratchCodes   the scratch codes
     */
    @JsonCreator
    public OneTimeTokenAccount(@JsonProperty("username") final String username,
                               @JsonProperty("secretKey") final String secretKey, @JsonProperty("validationCode") final int validationCode,
                               @JsonProperty("scratchCodes") final List<Integer> scratchCodes) {
        this();
        this.secretKey = secretKey;
        this.validationCode = validationCode;
        this.scratchCodes = scratchCodes;
        this.username = username;
    }

    @Override
    public int compareTo(final OneTimeTokenAccount o) {
        return new CompareToBuilder().append(this.scratchCodes, o.getScratchCodes()).append(this.validationCode, o.getValidationCode())
            .append(this.secretKey, o.getSecretKey()).append(this.username, o.getUsername()).build();
    }

}
