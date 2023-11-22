package org.apereo.cas.oidc.assurance;

import org.apereo.cas.oidc.assurance.entity.Verification;
import java.util.List;

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
     * Empty assurance verification source.
     *
     * @return the assurance verification source
     */
    static AssuranceVerificationSource empty() {
        return List::of;
    }
}
