package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.jasig.cas.services.RegisteredServiceAccessStrategy;
import org.jasig.cas.services.TimeBasedRegisteredServiceAccessStrategy;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.services.web.beans.RegisteredServiceSupportAccessEditBean;

/**
 * Default mapper for converting {@link RegisteredServiceAccessStrategy} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public final class DefaultAccessStrategyMapper implements AccessStrategyMapper {
    @Override
    public void mapAccessStrategy(final RegisteredServiceAccessStrategy accessStrategy, final ServiceData bean) {
        final RegisteredServiceSupportAccessEditBean accessBean = bean.getSupportAccess();
        accessBean.setCasEnabled(accessStrategy.isServiceAccessAllowed());
        accessBean.setSsoEnabled(accessStrategy.isServiceAccessAllowedForSso());

        if (accessStrategy instanceof DefaultRegisteredServiceAccessStrategy) {
            final DefaultRegisteredServiceAccessStrategy def = (DefaultRegisteredServiceAccessStrategy) accessStrategy;
            accessBean.setRequireAll(def.isRequireAllAttributes());
            accessBean.setRequiredAttr(def.getRequiredAttributes());
        }

        if (accessStrategy instanceof TimeBasedRegisteredServiceAccessStrategy) {
            final TimeBasedRegisteredServiceAccessStrategy def = (TimeBasedRegisteredServiceAccessStrategy)
                    accessStrategy;
            accessBean.setStartingTime(def.getStartingDateTime());
            accessBean.setEndingTime(def.getEndingDateTime());
        }
    }
}
