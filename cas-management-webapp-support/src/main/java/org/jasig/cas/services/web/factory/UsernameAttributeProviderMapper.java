package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.RegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;

/**
 * Interface for converting {@link RegisteredServiceUsernameAttributeProvider} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface UsernameAttributeProviderMapper {
    /**
     * Map {@link RegisteredServiceUsernameAttributeProvider} onto the target {@link ServiceData} data bean.
     *
     * @param provider the source username attribute provider
     * @param bean     the destination data bean
     */
    void mapUsernameAttributeProvider(RegisteredServiceUsernameAttributeProvider provider, ServiceData bean);

    /**
     * Create a {@link RegisteredServiceUsernameAttributeProvider} represented by the specified {@link ServiceData}
     * bean. Return null if a supported {@link RegisteredServiceUsernameAttributeProvider} couldn't be created.
     *
     * @param data a source data bean
     * @return the username attribute provider represented by the specified data bean
     */
    RegisteredServiceUsernameAttributeProvider toUsernameAttributeProvider(ServiceData data);
}
