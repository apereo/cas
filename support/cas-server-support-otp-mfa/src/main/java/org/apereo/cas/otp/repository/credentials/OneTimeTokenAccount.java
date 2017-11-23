package org.apereo.cas.otp.repository.credentials;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
public class OneTimeTokenAccount implements Serializable, Comparable<OneTimeTokenAccount> {

    private static final long serialVersionUID = -8289105320642735252L;

    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id = -1;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private String secretKey;

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
    private int validationCode;

    @ElementCollection
    @CollectionTable(name = "scratch_codes", joinColumns = @JoinColumn(name = "username"))
    @Column(updatable = true, insertable = true, nullable = false)
    private List<Integer> scratchCodes = new ArrayList<>();

    @Column(length = 255, updatable = true, insertable = true, nullable = false)
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
                               @JsonProperty("secretKey") final String secretKey,
                               @JsonProperty("validationCode") final int validationCode,
                               @JsonProperty("scratchCodes") final List<Integer> scratchCodes) {
        this();
        this.secretKey = secretKey;
        this.validationCode = validationCode;
        this.scratchCodes = scratchCodes;
        this.username = username;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public int getValidationCode() {
        return this.validationCode;
    }

    public List<Integer> getScratchCodes() {
        return this.scratchCodes;
    }

    public String getUsername() {
        return username;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public void setValidationCode(final int validationCode) {
        this.validationCode = validationCode;
    }

    public void setScratchCodes(final List<Integer> scratchCodes) {
        this.scratchCodes = scratchCodes;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    @Override
    public int compareTo(final OneTimeTokenAccount o) {
        return new CompareToBuilder()
                .append(this.scratchCodes, o.getScratchCodes())
                .append(this.validationCode, o.getValidationCode())
                .append(this.secretKey, o.getSecretKey())
                .append(this.username, o.getUsername())
                .build();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final OneTimeTokenAccount rhs = (OneTimeTokenAccount) obj;
        return new EqualsBuilder()
                .append(this.secretKey, rhs.secretKey)
                .append(this.validationCode, rhs.validationCode)
                .append(this.scratchCodes, rhs.scratchCodes)
                .append(this.username, rhs.username)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(secretKey)
                .append(validationCode)
                .append(scratchCodes)
                .append(username)
                .toHashCode();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("username", username)
                .toString();
    }
}
