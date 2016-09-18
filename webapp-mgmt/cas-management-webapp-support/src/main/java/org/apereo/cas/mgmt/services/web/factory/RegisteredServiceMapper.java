package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;

/**
 * Interface for converting {@link RegisteredService} to/from {@link RegisteredServiceEditBean.ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface RegisteredServiceMapper {
    /**
     * Map {@link RegisteredService} onto the target {@link RegisteredServiceEditBean.ServiceData} data bean.
     *
     * @param svc  the source registered service
     * @param bean the destination data bean
     */
    void mapRegisteredService(RegisteredService svc, RegisteredServiceEditBean.ServiceData bean);

    /**
     * Map {@link RegisteredService} onto the target {@link RegisteredServiceViewBean} data bean.
     *
     * @param svc  the source registered service
     * @param bean the destination data bean
     */
    void mapRegisteredService(RegisteredService svc, RegisteredServiceViewBean bean);

    /**
     * Create a RegisteredService represented by the specified {@link RegisteredServiceEditBean.ServiceData} bean.
     *
     * @param data a source data bean
     * @return the registered service represented by the specified data bean
     */
    RegisteredService toRegisteredService(RegisteredServiceEditBean.ServiceData data);
}
