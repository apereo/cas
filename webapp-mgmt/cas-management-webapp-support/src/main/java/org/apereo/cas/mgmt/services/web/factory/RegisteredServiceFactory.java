package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.mgmt.services.web.beans.FormData;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;

/**
 * Factory used to convert {@link RegisteredService}.
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
     * Create a {@link RegisteredServiceViewBean} bean from the provided {@link RegisteredService}.
     *
     * @param svc service being converted
     * @return the data bean representing the provided service
     */
    RegisteredServiceViewBean createServiceViewBean(RegisteredService svc);
}
