package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.RegisteredServiceAccessStrategy;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.services.web.beans.RegisteredServiceViewBean;

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

    /**
     * Map {@link RegisteredServiceAccessStrategy} onto the target {@link RegisteredServiceViewBean} data bean.
     *
     * @param accessStrategy the source access strategy
     * @param bean           the destination data bean
     */
    void mapAccessStrategy(RegisteredServiceAccessStrategy accessStrategy, RegisteredServiceViewBean bean);

    /**
     * Create a {@link RegisteredServiceAccessStrategy} represented by the specified {@link ServiceData} bean. Return
     * null if a supported {@link RegisteredServiceAccessStrategy} couldn't be created.
     *
     * @param bean a source data bean
     * @return the access strategy represented by the specified data bean
     */
    RegisteredServiceAccessStrategy toAccessStrategy(ServiceData bean);
}
