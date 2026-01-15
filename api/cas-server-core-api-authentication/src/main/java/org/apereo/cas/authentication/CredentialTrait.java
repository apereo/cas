package org.apereo.cas.authentication;

import module java.base;

/**
 * This is {@link CredentialTrait}. This is typically designed
 * as a marker interface. Traits tag and decorate credentials
 * with capability, and specific characteristics. For example, a typical
 * trait might be that a credential object can be impersonated.
 * Each {@link Credential} may carry a collection of traits, which allows
 * implementations to tag and decorate credentials with specific behavior
 * and attach those to the credential as trait without having to define
 * multiple credentials via inheritance.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface CredentialTrait extends Serializable {
}
