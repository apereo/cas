package org.apereo.cas.oidc.vc.issuer.metadata;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * This is {@link CredentialConfigurationDisplay}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Jacksonized
public class CredentialConfigurationDisplay implements Serializable {
    @Serial
    private static final long serialVersionUID = 216197021376111794L;

    private String name;
    @Builder.Default
    private String locale = "en-US";
    private String description;
    private CredentialConfigurationDisplayLogo logo;
    private String backgroundColor;
    private String textColor;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @Jacksonized
    public static class CredentialConfigurationDisplayLogo implements Serializable {
        @Serial
        private static final long serialVersionUID = 216197021376111795L;

        @Builder.Default
        private String uri = "https://apereo.github.io/cas/images/cas_logo.png";

        @JsonProperty("alt_text")
        private String altText;
    }
}
