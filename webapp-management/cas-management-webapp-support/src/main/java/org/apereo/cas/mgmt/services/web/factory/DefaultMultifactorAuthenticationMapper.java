package org.apereo.cas.mgmt.services.web.factory;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;

/**
 * This is {@link DefaultMultifactorAuthenticationMapper}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultMultifactorAuthenticationMapper implements MultifactorAuthenticationMapper {
    @Override
    public void mapMultifactorPolicy(final RegisteredServiceMultifactorPolicy multifactorPolicy, final RegisteredServiceEditBean.ServiceData bean) {
        bean.getMultiAuth().setFailureMode(multifactorPolicy.getFailureMode().name());
        bean.getMultiAuth().setProviders(StringUtils.join(multifactorPolicy.getMultifactorAuthenticationProviders(), ','));

        bean.getMultiAuth().getPrincipalAttr().setNameTrigger(multifactorPolicy.getPrincipalAttributeNameTrigger());
        bean.getMultiAuth().getPrincipalAttr().setValueMatch(multifactorPolicy.getPrincipalAttributeValueToMatch());
    }

    @Override
    public RegisteredServiceMultifactorPolicy toMultifactorPolicy(final RegisteredServiceEditBean.ServiceData data) {
        if (StringUtils.isNotBlank(data.getMultiAuth().getProviders())) {
            final DefaultRegisteredServiceMultifactorPolicy policy = new DefaultRegisteredServiceMultifactorPolicy();
            policy.setFailureMode(
                    RegisteredServiceMultifactorPolicy.FailureModes.valueOf(data.getMultiAuth().getFailureMode().toUpperCase()));
            policy.setPrincipalAttributeNameTrigger(data.getMultiAuth().getPrincipalAttr().getNameTrigger());
            policy.setPrincipalAttributeValueToMatch(data.getMultiAuth().getPrincipalAttr().getValueMatch());
            policy.setMultifactorAuthenticationProviders(Sets.newHashSet(data.getMultiAuth().getProviders().split(",")));
            return policy;
        }
        return null;
    }
}
