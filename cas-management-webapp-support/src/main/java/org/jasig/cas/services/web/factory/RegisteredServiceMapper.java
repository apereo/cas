package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.services.web.beans.RegisteredServiceViewBean;

/**
 * Interface for converting {@link RegisteredService} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface RegisteredServiceMapper {
    /**
     * Map {@link RegisteredService} onto the target {@link ServiceData} data bean.
     *
     * @param svc  the source registered service
     * @param bean the destination data bean
     */
    void mapRegisteredService(RegisteredService svc, ServiceData bean);

    /**
     * Map {@link RegisteredService} onto the target {@link RegisteredServiceViewBean} data bean.
     *
     * @param svc  the source registered service
     * @param bean the destination data bean
     */
    void mapRegisteredService(RegisteredService svc, RegisteredServiceViewBean bean);

    /**
     * Create a RegisteredService represented by the specified {@link ServiceData} bean.
     *
     * @param data a source data bean
     * @return the registered service represented by the specified data bean
     */
    RegisteredService toRegisteredService(ServiceData data);
}
