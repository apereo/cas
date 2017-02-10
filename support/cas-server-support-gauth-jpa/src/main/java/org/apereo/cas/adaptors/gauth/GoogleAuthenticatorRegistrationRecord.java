package org.apereo.cas.adaptors.gauth;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link GoogleAuthenticatorRegistrationRecord}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Entity
@Table(name = "GoogleAuthenticatorRegistrationRecord")
public class GoogleAuthenticatorRegistrationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id = Integer.MAX_VALUE;

    @Column(updatable = true, insertable = true, nullable = false)
    private String username;

    @Column(updatable = true, insertable = true, nullable = false)
    private String secretKey;

    @Column(updatable = true, insertable = true, nullable = false)
    private Integer validationCode;

    @ElementCollection
    @CollectionTable(name = "scratch_codes", joinColumns = @JoinColumn(name = "username"))
    @Column(updatable = true, insertable = true, nullable = false)
    private List<Integer> scratchCodes = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public Integer getValidationCode() {
        return validationCode;
    }

    public void setValidationCode(final Integer validationCode) {
        this.validationCode = validationCode;
    }

    public List<Integer> getScratchCodes() {
        return scratchCodes;
    }

    public void setScratchCodes(final List<Integer> scratchCodes) {
        this.scratchCodes = scratchCodes;
    }
}

