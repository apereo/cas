package org.apereo.cas.mgmt.services.web.factory;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Default mapper for converting {@link RegisteredServiceAttributeFilter} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
@RefreshScope
@Component(DefaultAttributeFilterMapper.BEAN_NAME)
public class DefaultAttributeFilterMapper implements AttributeFilterMapper {
    /**
     * Name of this bean within the Spring context.
     */
    public static final String BEAN_NAME = "defaultAttributeFilterMapper";

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
