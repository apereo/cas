package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.RegisteredServiceAccessStrategy;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;

/**
 * Interface for converting {@link RegisteredServiceAccessStrategy} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface AccessStrategyMapper {
    /**
     * Map {@link RegisteredServiceAccessStrategy} onto the target {@link ServiceData} data bean.
     *
     * @param accessStrategy the source access strategy
     * @param bean           the destination data bean
     */
    void mapAccessStrategy(RegisteredServiceAccessStrategy accessStrategy, ServiceData bean);
}
