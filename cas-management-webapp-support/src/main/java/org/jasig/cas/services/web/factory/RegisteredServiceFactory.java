package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.FormData;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.services.web.beans.RegisteredServiceViewBean;

/**
 * Factory used to convert {@link RegisteredService} from/to {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface RegisteredServiceFactory {
    /**
     * Create a {@link FormData} bean with configuration for the edit service form.
     *
     * @return the form data bean
     */
    FormData createFormData();

    /**
     * Create a {@link ServiceData} bean from the provided {@link RegisteredService}.
     *
     * @param svc service being converted
     * @return the data bean representing the provided service
     */
    ServiceData createServiceData(RegisteredService svc);

    /**
     * Create a {@link RegisteredServiceViewBean} bean from the provided {@link RegisteredService}.
     *
     * @param svc service being converted
     * @return the data bean representing the provided service
     */
    RegisteredServiceViewBean createServiceViewBean(RegisteredService svc);

    /**
     * Create a {@link RegisteredService} object from the provided {@link ServiceData} data bean.
     *
     * @param data the data bean being converted
     * @return the registered service created from the provided data bean
     */
    RegisteredService createRegisteredService(ServiceData data);
}
