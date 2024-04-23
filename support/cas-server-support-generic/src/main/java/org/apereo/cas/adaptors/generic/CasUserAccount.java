package org.apereo.cas.adaptors.generic;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link CasUserAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@ToString(exclude = "password")
public class CasUserAccount implements Serializable {
    private static final int SIZE = 8;

    @Serial
    private static final long serialVersionUID = 7579594722197541062L;

    private String password;

    private String availability;
    
    private String location;

    private Map<String, List<Object>> attributes = new LinkedHashMap<>(SIZE);

    private List<String> warnings = new ArrayList<>(SIZE);

    private AccountStatus status = AccountStatus.OK;

    private LocalDate expirationDate;

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

}
