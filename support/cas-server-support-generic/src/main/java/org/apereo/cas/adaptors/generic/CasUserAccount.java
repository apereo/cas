package org.apereo.cas.adaptors.generic;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.extern.slf4j.Slf4j;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link CasUserAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@Slf4j
@Getter
@Setter
public class CasUserAccount implements Serializable {

    private static final long serialVersionUID = 7579594722197541062L;

    /**
     * Indicates user account status.
     */
    public enum AccountStatus {

        /**
         * Ok account status.
         */
        OK, /**
         * Locked account status.
         */
        LOCKED, /**
         * Disabled account status.
         */
        DISABLED, /**
         * Expired account status.
         */
        EXPIRED, /**
         * Must change password account status.
         */
        MUST_CHANGE_PASSWORD
    }

    private String password;

    private Map<String, Object> attributes = new LinkedHashMap<>();

    private AccountStatus status = AccountStatus.OK;

    private LocalDate expirationDate;

}
