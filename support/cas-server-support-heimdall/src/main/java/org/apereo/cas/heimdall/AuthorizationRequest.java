package org.apereo.cas.heimdall;

import org.apereo.cas.authentication.principal.Principal;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link AuthorizationRequest}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Data
@NoArgsConstructor
@Validated
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@With
public class AuthorizationRequest extends BaseHeimdallRequest {
    @Serial
    private static final long serialVersionUID = -3826637704182099574L;

    private @NotBlank String method;
    private @NotBlank String uri;
    private @NotBlank String namespace;

    @Builder.Default
    private Map<String, ?> context = new HashMap<>();

    @JsonIgnore
    private Principal principal;
}

