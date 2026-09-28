package org.apereo.cas.heimdall.authzen;

import module java.base;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Options of an AuthZEN access evaluations request.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthZenEvaluationsOptions implements Serializable {
    @Serial
    private static final long serialVersionUID = 7127336815238962347L;

    @JsonProperty("evaluations_semantic")
    private AuthZenEvaluationsSemantic evaluationsSemantic;
}
