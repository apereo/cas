package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.services.web.beans.RegisteredServiceViewBean;

/**
 * Interface for converting {@link RegisteredServiceAttributeReleasePolicy} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface AttributeReleasePolicyMapper {
    /**
     * Map {@link RegisteredServiceAttributeReleasePolicy} onto the target {@link ServiceData} data bean.
     *
     * @param policy the source attribute release policy
     * @param bean   the destination data bean
     */
    void mapAttributeReleasePolicy(RegisteredServiceAttributeReleasePolicy policy, ServiceData bean);

    /**
     * Map {@link RegisteredServiceAttributeReleasePolicy} onto the target {@link RegisteredServiceViewBean} data bean.
     *
     * @param policy the source attribute release policy
     * @param bean   the destination data bean
     */
    void mapAttributeReleasePolicy(RegisteredServiceAttributeReleasePolicy policy, RegisteredServiceViewBean bean);

    /**
     * Create a {@link RegisteredServiceAttributeReleasePolicy} represented by the specified {@link ServiceData} bean.
     * Return null if a supported {@link RegisteredServiceAttributeReleasePolicy} couldn't be created.
     *
     * @param data a source data bean
     * @return the attribute release policy represented by the specified data bean
     */
    RegisteredServiceAttributeReleasePolicy toAttributeReleasePolicy(ServiceData data);
}
