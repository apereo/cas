package org.jasig.cas.services.web.factory;

import org.jasig.cas.authentication.principal.PrincipalAttributesRepository;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;

/**
 * Interface for converting {@link PrincipalAttributesRepository} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface PrincipalAttributesRepositoryMapper {
    /**
     * Map {@link PrincipalAttributesRepository} onto the target {@link ServiceData} data bean.
     *
     * @param repository the source principal attribute repository
     * @param bean       the destination data bean
     */
    void mapPrincipalRepository(PrincipalAttributesRepository repository, ServiceData bean);
}
