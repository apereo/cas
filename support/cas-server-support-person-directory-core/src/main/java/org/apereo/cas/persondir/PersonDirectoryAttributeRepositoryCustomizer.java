package org.apereo.cas.persondir;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.springframework.core.Ordered;

/**
 * This is {@link PersonDirectoryAttributeRepositoryCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface PersonDirectoryAttributeRepositoryCustomizer extends Ordered {

    /**
     * Supports.
     *
     * @param repository the repository
     * @return true/false
     */
    boolean supports(PersonAttributeDao repository);

    /**
     * Customize.
     *
     * @param repository the repository
     */
    void customize(PersonAttributeDao repository);
}
