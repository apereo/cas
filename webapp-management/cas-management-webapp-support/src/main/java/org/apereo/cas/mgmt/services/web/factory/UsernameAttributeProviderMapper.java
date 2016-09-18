package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.services.RegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;

/**
 * Interface for converting {@link RegisteredServiceUsernameAttributeProvider} to/from {@link RegisteredServiceEditBean.ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface UsernameAttributeProviderMapper {
    /**
     * Map {@link RegisteredServiceUsernameAttributeProvider} onto the target {@link RegisteredServiceEditBean.ServiceData} data bean.
     *
     * @param provider the source username attribute provider
     * @param bean     the destination data bean
     */
    void mapUsernameAttributeProvider(RegisteredServiceUsernameAttributeProvider provider, RegisteredServiceEditBean.ServiceData bean);

    /**
     * Map {@link RegisteredServiceUsernameAttributeProvider} onto the target {@link RegisteredServiceViewBean} data
     * bean.
     *
     * @param provider the source username attribute provider
     * @param bean     the destination data bean
     */
    void mapUsernameAttributeProvider(RegisteredServiceUsernameAttributeProvider provider,
                                      RegisteredServiceViewBean bean);

    /**
     * Create a {@link RegisteredServiceUsernameAttributeProvider}
     * represented by the specified {@link RegisteredServiceEditBean.ServiceData}
     * bean. Return null if a supported {@link RegisteredServiceUsernameAttributeProvider}
     * couldn't be created.
     *
     * @param data a source data bean
     * @return the username attribute provider represented by the specified data bean
     */
    RegisteredServiceUsernameAttributeProvider toUsernameAttributeProvider(RegisteredServiceEditBean.ServiceData data);
}
