package org.apereo.cas.validation;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apereo.inspektr.audit.annotation.Audit;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link ChainingCasProtocolValidationSpecification}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class ChainingCasProtocolValidationSpecification implements CasProtocolValidationSpecification {
    private final List<CasProtocolValidationSpecification> specifications = new ArrayList<>();

    private final boolean canBeSatisfiedByAnySpecification;

    @Audit(
        action = AuditableActions.PROTOCOL_SPECIFICATION_VALIDATE,
        actionResolverName = AuditActionResolvers.VALIDATE_PROTOCOL_SPECIFICATION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.VALIDATE_PROTOCOL_SPECIFICATION_RESOURCE_RESOLVER)
    @Override
    public boolean isSatisfiedBy(final Assertion assertion, final HttpServletRequest request) {
        if (this.canBeSatisfiedByAnySpecification) {
            return this.specifications
                .stream()
                .anyMatch(spec -> spec.isSatisfiedBy(assertion, request));
        }
        return this.specifications.stream()
            .allMatch(spec -> spec.isSatisfiedBy(assertion, request));
    }

    @Override
    public void reset() {
        this.specifications.forEach(CasProtocolValidationSpecification::reset);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Forwarded to the chained specifications. This chain is typically a shared singleton, so it
     * must not hold per-request state; the {@code renew} value carried by a protocol request is
     * resolved from the request itself when the specification is evaluated.
     */
    @Override
    public void setRenew(final boolean value) {
        this.specifications.forEach(spec -> spec.setRenew(value));
    }

    /**
     * Add policy.
     *
     * @param policy the policy
     */
    public void addSpecification(final CasProtocolValidationSpecification policy) {
        this.specifications.add(policy);
    }

    /**
     * Add policies.
     *
     * @param policies the policies
     */
    public void addSpecifications(final CasProtocolValidationSpecification... policies) {
        this.specifications.addAll(Arrays.stream(policies).toList());
    }

    /**
     * Size.
     *
     * @return the int
     */
    public int size() {
        return specifications.size();
    }
}
