package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;

/**
 * Interface for converting {@link RegisteredServiceAttributeReleasePolicy} to/from {@link RegisteredServiceEditBean.ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface AttributeReleasePolicyMapper {
    /**
     * Map {@link RegisteredServiceAttributeReleasePolicy} onto the target {@link RegisteredServiceEditBean.ServiceData} data bean.
     *
     * @param policy the source attribute release policy
     * @param bean   the destination data bean
     */
    void mapAttributeReleasePolicy(RegisteredServiceAttributeReleasePolicy policy, RegisteredServiceEditBean.ServiceData bean);

    /**
     * Map {@link RegisteredServiceAttributeReleasePolicy} onto the target {@link RegisteredServiceViewBean} data bean.
     *
     * @param policy the source attribute release policy
     * @param bean   the destination data bean
     */
    void mapAttributeReleasePolicy(RegisteredServiceAttributeReleasePolicy policy, RegisteredServiceViewBean bean);

    /**
     * Create a {@link RegisteredServiceAttributeReleasePolicy} represented by the specified
     * {@link RegisteredServiceEditBean.ServiceData} bean.
     * Return null if a supported {@link RegisteredServiceAttributeReleasePolicy} couldn't be created.
     *
     * @param data a source data bean
     * @return the attribute release policy represented by the specified data bean
     */
    RegisteredServiceAttributeReleasePolicy toAttributeReleasePolicy(RegisteredServiceEditBean.ServiceData data);
}
