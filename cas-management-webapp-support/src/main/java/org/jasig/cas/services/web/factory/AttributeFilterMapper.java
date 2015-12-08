package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.RegisteredServiceAttributeFilter;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;

/**
 * Interface for converting {@link RegisteredServiceAttributeFilter} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface AttributeFilterMapper {
    /**
     * Map {@link RegisteredServiceAttributeFilter} onto the target {@link ServiceData} data bean.
     *
     * @param filter the source attribute filter
     * @param bean   the destination data bean
     */
    void mapAttributeFilter(RegisteredServiceAttributeFilter filter, ServiceData bean);

    /**
     * Create a {@link RegisteredServiceAttributeFilter} represented by the specified {@link ServiceData} bean. Return
     * null if a supported {@link RegisteredServiceAttributeFilter} couldn't be created.
     *
     * @param data a source data bean
     * @return the attribute filter represented by the specified data bean
     */
    RegisteredServiceAttributeFilter toAttributeFilter(ServiceData data);
}
