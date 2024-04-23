package org.apereo.cas.validation;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apereo.inspektr.audit.annotation.Audit;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is {@link ChainingCasProtocolValidationSpecification}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class ChainingCasProtocolValidationSpecification implements CasProtocolValidationSpecification {
    private final List<CasProtocolValidationSpecification> specifications = new ArrayList<>(0);

    private final boolean canBeSatisfiedByAnySpecification;

    private boolean renew;

    @Audit(
        action = AuditableActions.PROTOCOL_SPECIFICATION_VALIDATE,
        actionResolverName = AuditActionResolvers.VALIDATE_PROTOCOL_SPECIFICATION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.VALIDATE_PROTOCOL_SPECIFICATION_RESOURCE_RESOLVER)
    @Override
    public boolean isSatisfiedBy(final Assertion assertion, final HttpServletRequest request) {
        if (this.canBeSatisfiedByAnySpecification) {
            return this.specifications
                .stream()
                .peek(spec -> spec.setRenew(this.renew))
                .anyMatch(spec -> spec.isSatisfiedBy(assertion, request));
        }
        return this.specifications.stream()
            .peek(spec -> spec.setRenew(this.renew))
            .allMatch(spec -> spec.isSatisfiedBy(assertion, request));
    }

    @Override
    public void reset() {
        this.specifications.forEach(CasProtocolValidationSpecification::reset);
        setRenew(false);
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
