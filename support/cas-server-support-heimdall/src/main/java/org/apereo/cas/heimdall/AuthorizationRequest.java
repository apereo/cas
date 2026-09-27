package org.apereo.cas.heimdall;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.heimdall.authzen.AuthZenAction;
import org.apereo.cas.heimdall.authzen.AuthZenResource;
import org.apereo.cas.heimdall.authzen.AuthZenSubject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SuperBuilder;
import lombok.ToString;
import lombok.With;
import org.springframework.validation.annotation.Validated;

/**
 * This is {@link AuthorizationRequest}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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

    /**
     * Whether this is an AuthZEN request, which carries a subject, a resource and an action.
     *
     * @return true if subject, resource and action are all present
     */
    @JsonIgnore
    public boolean isAuthZen() {
        return subject != null && resource != null && action != null;
    }
}

