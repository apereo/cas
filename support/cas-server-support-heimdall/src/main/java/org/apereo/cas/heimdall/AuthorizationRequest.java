package org.apereo.cas.heimdall;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.heimdall.authzen.AuthZenAction;
import org.apereo.cas.heimdall.authzen.AuthZenResource;
import org.apereo.cas.heimdall.authzen.AuthZenSubject;
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
public class AuthorizationRequest extends BaseHeimdallEntity {
    @Serial
    private static final long serialVersionUID = -3826637704182099574L;

    private String method;
    private String uri;
    private String namespace;
    
    private AuthZenSubject subject;
    private AuthZenResource resource;
    private AuthZenAction action;

    @Builder.Default
    private Map<String, ?> context = new HashMap<>();

    @JsonIgnore
    private Principal principal;
}

