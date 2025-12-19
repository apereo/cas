package org.apereo.cas.services.consent;

import module java.base;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.services.RegisteredServiceConsentPolicy;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

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
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ChainingRegisteredServiceConsentPolicy implements RegisteredServiceConsentPolicy {
    @Serial
    private static final long serialVersionUID = -2949244688986345692L;

    private final List<RegisteredServiceConsentPolicy> policies = new ArrayList<>();

    /**
     * Add policies.
     *
     * @param policy the policy
     */
    public void addPolicies(final Collection<RegisteredServiceConsentPolicy> policy) {
        if (policies.addAll(policy.stream().filter(BeanSupplier::isNotProxy).toList())) {
            AnnotationAwareOrderComparator.sortIfNecessary(this.policies);
        }
    }

    /**
     * Add policy.
     *
     * @param policy the policy
     */
    public void addPolicy(final RegisteredServiceConsentPolicy policy) {
        if (BeanSupplier.isNotProxy(policy)) {
            policies.add(policy);
            AnnotationAwareOrderComparator.sortIfNecessary(this.policies);
        }
    }

    @Override
    @JsonIgnore
    public TriStateBoolean getStatus() {
        if (this.policies.stream().filter(BeanSupplier::isNotProxy).anyMatch(policy -> policy.getStatus().isTrue())) {
            return TriStateBoolean.TRUE;
        }
        if (this.policies.stream().filter(BeanSupplier::isNotProxy).allMatch(policy -> policy.getStatus().isFalse())) {
            return TriStateBoolean.FALSE;
        }
        return TriStateBoolean.UNDEFINED;
    }

    @Override
    @JsonIgnore
    public Set<String> getExcludedServices() {
        return this.policies
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(RegisteredServiceConsentPolicy::getExcludedServices)
            .flatMap(Set::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @JsonIgnore
    @Override
    public Set<String> getExcludedAttributes() {
        return this.policies
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(RegisteredServiceConsentPolicy::getExcludedAttributes)
            .filter(Objects::nonNull)
            .flatMap(Set::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    }

    @Override
    @JsonIgnore
    public Set<String> getIncludeOnlyAttributes() {
        return this.policies
            .stream()
            .filter(BeanSupplier::isNotProxy)
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
