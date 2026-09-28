package org.apereo.cas.heimdall.authzen;

import module java.base;
import org.apereo.cas.heimdall.AuthorizationRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;

/**
 * AuthZEN access evaluations request. The top-level subject, resource, action and context are defaults
 * for every entry of {@code evaluations}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthZenEvaluationsRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -2361488349560227214L;

    private AuthZenSubject subject;

    private AuthZenResource resource;

    private AuthZenAction action;

    private Map<String, Object> context;

    @Builder.Default
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<AuthZenEvaluation> evaluations = new ArrayList<>();

    private AuthZenEvaluationsOptions options;

    /**
     * The request built from the top-level fields alone, used when there are no evaluations.
     *
     * @return the authorization request
     */
    @JsonIgnore
    public AuthorizationRequest toAuthorizationRequest() {
        return toAuthorizationRequest(new AuthZenEvaluation());
    }

    private AuthorizationRequest toAuthorizationRequest(final AuthZenEvaluation evaluation) {
        val selectedContext = ObjectUtils.getIfNull(evaluation.getContext(), this.context);
        val effectiveContext = selectedContext == null
            ? new HashMap<String, Object>()
            : new HashMap<String, Object>(selectedContext);
        return AuthorizationRequest.builder()
            .subject(ObjectUtils.getIfNull(evaluation.getSubject(), subject))
            .resource(ObjectUtils.getIfNull(evaluation.getResource(), resource))
            .action(ObjectUtils.getIfNull(evaluation.getAction(), action))
            .context(effectiveContext)
            .build();
    }

    /**
     * One authorization request per evaluation, in order, with the top-level defaults applied.
     *
     * @return the authorization requests
     */
    @JsonIgnore
    public List<AuthorizationRequest> toAuthorizationRequests() {
        return evaluations.stream().map(this::toAuthorizationRequest).toList();
    }

    /**
     * The evaluations semantic, {@code execute_all} unless requested otherwise.
     *
     * @return the semantic
     */
    @JsonIgnore
    public AuthZenEvaluationsSemantic getEvaluationsSemantic() {
        return options == null || options.getEvaluationsSemantic() == null
            ? AuthZenEvaluationsSemantic.EXECUTE_ALL
            : options.getEvaluationsSemantic();
    }
}
