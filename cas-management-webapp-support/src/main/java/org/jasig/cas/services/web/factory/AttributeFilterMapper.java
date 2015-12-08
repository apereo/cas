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
}
