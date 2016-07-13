package org.apereo.cas.mgmt.services.web.factory;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean.ServiceData;

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

    @Override
    public RegisteredServiceAttributeFilter toAttributeFilter(final ServiceData data) {
        final String filter = data.getAttrRelease().getAttrFilter();
        if (StringUtils.isNotBlank(filter)) {
            return new RegisteredServiceRegexAttributeFilter(filter);
        }

        return null;
    }
}
