package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.FormData;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Add all available attribute names to {@link FormData} for use by the edit service form.
 *
 * @author Daniel Frett
 * @since 4.2
 */
@Component("attributeFormDataPopulator")
public final class AttributeFormDataPopulator implements FormDataPopulator {
    /**
     * Instance of AttributeRegistry.
     */
    @NotNull
    private final IPersonAttributeDao personAttributeDao;

    /**
     * Default constructor.
     *
     * @param personAttributeDao the attribute source to retrieve available attribute names from.
     */
    @Autowired
    public AttributeFormDataPopulator(@Qualifier("attributeRepository") final IPersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
    }

    @Override
    public void populateFormData(final FormData formData) {
        final List<String> possibleAttributeNames = new ArrayList<>(personAttributeDao.getPossibleUserAttributeNames());
        Collections.sort(possibleAttributeNames);
        formData.setAvailableAttributes(possibleAttributeNames);
    }
}
