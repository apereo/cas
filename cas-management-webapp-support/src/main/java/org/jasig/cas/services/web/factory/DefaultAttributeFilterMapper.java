package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.RegisteredServiceAttributeFilter;
import org.jasig.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;

/**
 * Default mapper for converting {@link RegisteredServiceAttributeFilter} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public class DefaultAttributeFilterMapper implements AttributeFilterMapper {
    @Override
    public void mapAttributeFilter(final RegisteredServiceAttributeFilter filter, final ServiceData bean) {
        if (filter instanceof RegisteredServiceRegexAttributeFilter) {
            final RegisteredServiceRegexAttributeFilter regex = (RegisteredServiceRegexAttributeFilter) filter;
            bean.getAttrRelease().setAttrFilter(regex.getPattern().pattern());
        }
    }
}
