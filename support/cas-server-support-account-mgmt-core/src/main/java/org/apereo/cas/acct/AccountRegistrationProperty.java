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

    @Builder.Default
    private String type = "text";

    @Builder.Default
    private String pattern = ".+";

    private boolean required;

    private String label;

    @Builder.Default
    private String cssClass = "account-registration-field";

    private String title;

    @Builder.Default
    private String validationMessage = "cas.screen.acct.error.invalid-value";
}
