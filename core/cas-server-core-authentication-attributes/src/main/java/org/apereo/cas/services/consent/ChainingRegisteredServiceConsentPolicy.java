package org.apereo.cas.services.consent;

import org.apereo.cas.services.RegisteredServiceConsentPolicy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link ChainingRegisteredServiceConsentPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChainingRegisteredServiceConsentPolicy implements RegisteredServiceConsentPolicy {
    private static final long serialVersionUID = -2949244688986345692L;

    private final List<RegisteredServiceConsentPolicy> policies = new ArrayList<>(0);

    /**
     * Add policies.
     *
     * @param policy the policy
     */
    public void addPolicies(final Collection<RegisteredServiceConsentPolicy> policy) {
        this.policies.addAll(policy);
        AnnotationAwareOrderComparator.sortIfNecessary(this.policies);
    }

    /**
     * Add policy.
     *
     * @param policy the policy
     */
    public void addPolicy(final RegisteredServiceConsentPolicy policy) {
        this.policies.add(policy);
        AnnotationAwareOrderComparator.sortIfNecessary(this.policies);
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return this.policies.stream().anyMatch(RegisteredServiceConsentPolicy::isEnabled);
    }

    @JsonIgnore
    @Override
    public Set<String> getExcludedAttributes() {
        return this.policies
            .stream()
            .map(RegisteredServiceConsentPolicy::getExcludedAttributes)
            .flatMap(Set::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    }

    @Override
    @JsonIgnore
    public Set<String> getIncludeOnlyAttributes() {
        return this.policies
            .stream()
            .map(RegisteredServiceConsentPolicy::getIncludeOnlyAttributes)
            .flatMap(Set::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    @JsonIgnore
    public int size() {
        return policies.size();
    }
}
