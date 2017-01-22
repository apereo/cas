package org.apereo.cas.mgmt.services.web.factory;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;

import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        bean.getMultiAuth().setProviders(multifactorPolicy.getMultifactorAuthenticationProviders().stream().collect(Collectors.joining(",")));

        bean.getMultiAuth().getPrincipalAttr().setNameTrigger(multifactorPolicy.getPrincipalAttributeNameTrigger());
        bean.getMultiAuth().getPrincipalAttr().setValueMatch(multifactorPolicy.getPrincipalAttributeValueToMatch());

        bean.getMultiAuth().setBypassEnabled(multifactorPolicy.isBypassEnabled());
    }

    @Override
    public RegisteredServiceMultifactorPolicy toMultifactorPolicy(final RegisteredServiceEditBean.ServiceData data) {
        if (StringUtils.isNotBlank(data.getMultiAuth().getProviders())) {
            final DefaultRegisteredServiceMultifactorPolicy policy = new DefaultRegisteredServiceMultifactorPolicy();
            policy.setFailureMode(
                    RegisteredServiceMultifactorPolicy.FailureModes.valueOf(data.getMultiAuth().getFailureMode().toUpperCase()));
            policy.setPrincipalAttributeNameTrigger(data.getMultiAuth().getPrincipalAttr().getNameTrigger());
            policy.setPrincipalAttributeValueToMatch(data.getMultiAuth().getPrincipalAttr().getValueMatch());
            final String[] providers = data.getMultiAuth().getProviders().split(",");
            policy.setMultifactorAuthenticationProviders(Stream.of(providers).collect(Collectors.toSet()));
            policy.setBypassEnabled(data.getMultiAuth().isBypassEnabled());
            return policy;
        }
        return null;
    }
}
