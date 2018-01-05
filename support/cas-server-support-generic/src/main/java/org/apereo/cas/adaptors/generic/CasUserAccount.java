package org.apereo.cas.adaptors.generic;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link CasUserAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class CasUserAccount implements Serializable {
    private static final long serialVersionUID = 7579594722197541062L;

    /**
     * Indicates user account status.
     */
    public enum AccountStatus {
        /**
         * Ok account status.
         */
        OK,
        /**
         * Locked account status.
         */
        LOCKED,
        /**
         * Disabled account status.
         */
        DISABLED,
        /**
         * Expired account status.
         */
        EXPIRED,
        /**
         * Must change password account status.
         */
        MUST_CHANGE_PASSWORD
    }

    private String password;
    private Map<String, Object> attributes = new LinkedHashMap<>();
    private AccountStatus status = AccountStatus.OK;
    private LocalDate expirationDate;

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(final AccountStatus status) {
        this.status = status;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(final LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }
}
