package org.apereo.cas.heimdall.authzen;

import module java.base;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One entry of an AuthZEN access evaluations request. Each field that is present overrides the
 * corresponding default of the enclosing request.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthZenEvaluation implements Serializable {
    @Serial
    private static final long serialVersionUID = 3185220373437260481L;

    private AuthZenSubject subject;

    private AuthZenResource resource;

    private AuthZenAction action;

    private Map<String, Object> context;
}
