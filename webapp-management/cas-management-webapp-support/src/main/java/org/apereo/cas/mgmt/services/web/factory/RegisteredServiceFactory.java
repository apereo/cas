package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;

/**
 * Factory used to convert {@link RegisteredService} from/to {@link RegisteredServiceEditBean.ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface RegisteredServiceFactory {
    /**
     * Create a {@link RegisteredServiceEditBean.FormData} bean with configuration for the edit service form.
     *
     * @return the form data bean
     */
    RegisteredServiceEditBean.FormData createFormData();

    /**
     * Create a {@link RegisteredServiceEditBean.ServiceData} bean from the provided {@link RegisteredService}.
     *
     * @param svc service being converted
     * @return the data bean representing the provided service
     */
    RegisteredServiceEditBean.ServiceData createServiceData(RegisteredService svc);

    /**
     * Create a {@link RegisteredServiceViewBean} bean from the provided {@link RegisteredService}.
     *
     * @param svc service being converted
     * @return the data bean representing the provided service
     */
    RegisteredServiceViewBean createServiceViewBean(RegisteredService svc);

    /**
     * Create a {@link RegisteredService} object from the provided {@link RegisteredServiceEditBean.ServiceData} data bean.
     *
     * @param data the data bean being converted
     * @return the registered service created from the provided data bean
     */
    RegisteredService createRegisteredService(RegisteredServiceEditBean.ServiceData data);
}
