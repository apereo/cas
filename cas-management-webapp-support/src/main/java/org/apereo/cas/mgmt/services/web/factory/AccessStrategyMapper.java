package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;

/**
 * Interface for converting {@link RegisteredServiceAccessStrategy} to/from {@link RegisteredServiceEditBean.ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface AccessStrategyMapper {
    /**
     * Map {@link RegisteredServiceAccessStrategy} onto the target {@link RegisteredServiceEditBean.ServiceData} data bean.
     *
     * @param accessStrategy the source access strategy
     * @param bean           the destination data bean
     */
    void mapAccessStrategy(RegisteredServiceAccessStrategy accessStrategy, RegisteredServiceEditBean.ServiceData bean);

    /**
     * Map {@link RegisteredServiceAccessStrategy} onto the target {@link RegisteredServiceViewBean} data bean.
     *
     * @param accessStrategy the source access strategy
     * @param bean           the destination data bean
     */
    void mapAccessStrategy(RegisteredServiceAccessStrategy accessStrategy, RegisteredServiceViewBean bean);

    /**
     * Create a {@link RegisteredServiceAccessStrategy} represented by the specified
     * {@link RegisteredServiceEditBean.ServiceData} bean. Return
     * null if a supported {@link RegisteredServiceAccessStrategy} couldn't be created.
     *
     * @param bean a source data bean
     * @return the access strategy represented by the specified data bean
     */
    RegisteredServiceAccessStrategy toAccessStrategy(RegisteredServiceEditBean.ServiceData bean);
}
