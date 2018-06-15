package org.apereo.cas.consent;

/**
 * This is {@link InMemoryConsentRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class InMemoryConsentRepositoryTests extends BaseConsentRepositoryTests {

    public ConsentRepository getRepository() {
        return new InMemoryConsentRepository();
    }
}
