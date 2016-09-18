package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.services.RegisteredServiceProxyPolicy;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;

/**
 * Interface for converting {@link RegisteredServiceProxyPolicy} to/from {@link RegisteredServiceEditBean.ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public interface ProxyPolicyMapper {
    /**
     * Map {@link RegisteredServiceProxyPolicy} onto the target {@link RegisteredServiceEditBean.ServiceData} data bean.
     *
     * @param policy the source proxy policy
     * @param bean   the destination data bean
     */
    void mapProxyPolicy(RegisteredServiceProxyPolicy policy, RegisteredServiceEditBean.ServiceData bean);

    /**
     * Map {@link RegisteredServiceProxyPolicy} onto the target {@link RegisteredServiceViewBean} data bean.
     *
     * @param policy the source proxy policy
     * @param bean   the destination data bean
     */
    void mapProxyPolicy(RegisteredServiceProxyPolicy policy, RegisteredServiceViewBean bean);

    /**
     * Create a {@link RegisteredServiceProxyPolicy} represented by the specified
     * {@link RegisteredServiceEditBean.ServiceData} bean. Return null
     * if a supported {@link RegisteredServiceProxyPolicy} couldn't be created.
     *
     * @param data a source data bean
     * @return the proxy policy represented by the specified data bean
     */
    RegisteredServiceProxyPolicy toProxyPolicy(RegisteredServiceEditBean.ServiceData data);
}
