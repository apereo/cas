package org.apereo.cas.acct;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * This is {@link AccountRegistrationProperty}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@SuperBuilder
@NoArgsConstructor
public class AccountRegistrationProperty implements Serializable {
    private static final long serialVersionUID = -7637969227639901358L;

    private int order;

    private String name;

    private String format;

    @Builder.Default
    private AccountRegistrationPropertyTypes type = AccountRegistrationPropertyTypes.TEXT;

    private boolean required;

    private String label;

    /**
     * Define different types.
     */
    public enum AccountRegistrationPropertyTypes {
        /**
         * Text type.
         */
        TEXT,
        /**
         * Number type.
         */
        NUMBER,
        /**
         * Date type.
         */
        DATE,
        /**
         * Pasword type.
         */
        PASSWORD,
        /**
         * Field contains multiple values that can be selected from a list.
         */
        LIST
    }
}
