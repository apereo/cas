package org.apereo.cas.aup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * This is {@link AcceptableUsagePolicyTerms}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@EqualsAndHashCode
@ToString
@Builder
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class AcceptableUsagePolicyTerms implements Serializable {
    /**
     * Parent code to link to message bundles.
     */
    public static final String CODE = "screen.aup.policyterms";

    private static final long serialVersionUID = -5583211907625747831L;

    private final String code;

    private final String defaultText;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public AcceptableUsagePolicyTerms(@JsonProperty("code") final String code,
                                      @JsonProperty("defaultText") final String defaultText) {
        this.code = code;
        this.defaultText = defaultText;
    }

    /**
     * Indicate if policy terms are defined.
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isDefined() {
        return StringUtils.isNotBlank(code) || StringUtils.isNotBlank(defaultText);
    }
}
