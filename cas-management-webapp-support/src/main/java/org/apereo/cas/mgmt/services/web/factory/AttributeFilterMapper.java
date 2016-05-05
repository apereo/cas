package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;

/**
 * Interface for converting {@link RegisteredServiceAttributeFilter} to/from {@link RegisteredServiceEditBean.ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface AttributeFilterMapper {
    /**
     * Map {@link RegisteredServiceAttributeFilter} onto the target {@link RegisteredServiceEditBean.ServiceData} data bean.
     *
     * @param filter the source attribute filter
     * @param bean   the destination data bean
     */
    void mapAttributeFilter(RegisteredServiceAttributeFilter filter, RegisteredServiceEditBean.ServiceData bean);

    /**
     * Create a {@link RegisteredServiceAttributeFilter} represented by the specified
     * {@link RegisteredServiceEditBean.ServiceData} bean. Return
     * null if a supported {@link RegisteredServiceAttributeFilter} couldn't be created.
     *
     * @param data a source data bean
     * @return the attribute filter represented by the specified data bean
     */
    RegisteredServiceAttributeFilter toAttributeFilter(RegisteredServiceEditBean.ServiceData data);
}
