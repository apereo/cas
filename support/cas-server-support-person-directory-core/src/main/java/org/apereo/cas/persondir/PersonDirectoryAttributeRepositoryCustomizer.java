package org.apereo.cas.persondir;

import org.apereo.services.persondir.IPersonAttributeDao;
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
     * @return the boolean
     */
    boolean supports(IPersonAttributeDao repository);

    /**
     * Customize.
     *
     * @param repository the repository
     */
    void customize(IPersonAttributeDao repository);
}
