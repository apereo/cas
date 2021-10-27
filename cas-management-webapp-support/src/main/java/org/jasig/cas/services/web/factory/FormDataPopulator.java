package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.FormData;

/**
 * Interface for beans that need to populate {@link FormData} objects for the edit service interface.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface FormDataPopulator {
    /**
     * Method called to populate a {@link FormData} object with dynamic form configuration data.
     *
     * @param formData the data bean being populated
     */
    void populateFormData(FormData formData);
}
