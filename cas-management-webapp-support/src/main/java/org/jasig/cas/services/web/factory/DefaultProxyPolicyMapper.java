package org.jasig.cas.services.web.factory;

import org.jasig.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.services.web.beans.RegisteredServiceProxyPolicyBean;

/**
 * Default mapper for converting {@link RegisteredServiceProxyPolicy} to/from {@link ServiceData}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public final class DefaultProxyPolicyMapper implements ProxyPolicyMapper {
    @Override
    public void mapProxyPolicy(final RegisteredServiceProxyPolicy policy, final ServiceData bean) {
        final RegisteredServiceProxyPolicyBean cBean = bean.getProxyPolicy();
        if (policy == null || policy instanceof RefuseRegisteredServiceProxyPolicy) {
            cBean.setType(RegisteredServiceProxyPolicyBean.Types.REFUSE.toString());
        } else if (policy instanceof RegexMatchingRegisteredServiceProxyPolicy) {
            final RegexMatchingRegisteredServiceProxyPolicy regex = (RegexMatchingRegisteredServiceProxyPolicy) policy;
            cBean.setType(RegisteredServiceProxyPolicyBean.Types.REGEX.toString());
            cBean.setValue(regex.getPattern().toString());
        }
    }
}
