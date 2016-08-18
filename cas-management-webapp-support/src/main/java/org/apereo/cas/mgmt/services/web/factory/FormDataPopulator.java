package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;

/**
 * Interface for beans that need to populate {@link RegisteredServiceEditBean.FormData} objects for the edit service interface.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface FormDataPopulator {
    /**
     * Method called to populate a {@link RegisteredServiceEditBean.FormData} object with dynamic form configuration data.
     *
     * @param formData the data bean being populated
     */
    void populateFormData(RegisteredServiceEditBean.FormData formData);
}
