package org.apereo.cas.oidc.assurance;

import module java.base;
import org.apereo.cas.oidc.assurance.entity.Verification;

/**
 * This is {@link AssuranceVerificationSource}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface AssuranceVerificationSource {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "assuranceVerificationSource";

    /**
     * Load list of verifications.
     *
     * @return the list
     */
    List<Verification> load();

    /**
     * Find verification by trust framework.
     *
     * @param trustFramework the trust framework
     * @return the verification
     */
    default Optional<Verification> findByTrustFramework(final String trustFramework) {
        return Optional.empty();
    }

    /**
     * Empty assurance verification source.
     *
     * @return the assurance verification source
     */
    static AssuranceVerificationSource empty() {
        return List::of;
    }
}
