package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean.FormData;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Add all available attribute names to {@link FormData} for use by the edit service form.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public class AttributeFormDataPopulator implements FormDataPopulator {

    private IPersonAttributeDao personAttributeDao;

    public AttributeFormDataPopulator() {
    }

    /**
     * Default constructor.
     *
     * @param personAttributeDao the attribute source to retrieve available attribute names from.
     */
    public AttributeFormDataPopulator(final IPersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
    }

    @Override
    public void populateFormData(final FormData formData) {
        final List<String> possibleAttributeNames = new ArrayList<>(this.personAttributeDao.getPossibleUserAttributeNames());
        Collections.sort(possibleAttributeNames);
        formData.setAvailableAttributes(possibleAttributeNames);
    }
}
