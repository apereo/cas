package org.apereo.cas.oidc.vc.issuer.metadata;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * This is {@link OidcCredentialConfigurationTypeMetadata}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Setter
@NoArgsConstructor
public class OidcCredentialConfigurationTypeMetadata implements Serializable {
    @Serial
    private static final long serialVersionUID = -7403145313503329287L;

    private String vct;
    private String name;
    private String description;

    private List<ClaimMetadata> claims;

    private List<CredentialConfigurationDisplay> display;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @SuperBuilder
    public static class ClaimMetadata implements Serializable {
        @Serial
        private static final long serialVersionUID = 226197021376111795L;

        private List<String> path;
        private boolean mandatory;
        private String sd;
        private List<ClaimDisplay> display;
    }
    
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @SuperBuilder
    public static class ClaimDisplay implements Serializable {
        @Serial
        private static final long serialVersionUID = 216197021376111795L;

        private String label;

        @Builder.Default
        private String lang = "en-US";
    }
}
