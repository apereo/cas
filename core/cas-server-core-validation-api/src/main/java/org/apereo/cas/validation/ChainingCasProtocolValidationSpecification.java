package org.apereo.cas.validation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link ChainingCasProtocolValidationSpecification}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class ChainingCasProtocolValidationSpecification implements CasProtocolValidationSpecification {
    private final List<CasProtocolValidationSpecification> specifications = new ArrayList<>();

    private final boolean canBeSatisfiedByAnySpecification;

    @Override
    public boolean isSatisfiedBy(final Assertion assertion, final HttpServletRequest request) {
        if (this.canBeSatisfiedByAnySpecification) {
            return this.specifications.stream().anyMatch(s -> s.isSatisfiedBy(assertion, request));
        }
        return this.specifications.stream().allMatch(s -> s.isSatisfiedBy(assertion, request));
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
        this.specifications.addAll(Arrays.stream(policies).collect(Collectors.toList()));
    }

    /**
     * Size.
     *
     * @return the int
     */
    public int size() {
        return specifications.size();
    }

    @Override
    public void reset() {
        this.specifications.forEach(CasProtocolValidationSpecification::reset);
    }
}
