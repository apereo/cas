package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.mgmt.services.web.beans.AbstractRegisteredServiceAttributeReleasePolicyStrategyBean;
import org.apereo.cas.mgmt.services.web.beans.FormData;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceAttributeReleasePolicyStrategyViewBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceAttributeReleasePolicyViewBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceProxyPolicyBean;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.GroovyScriptAttributeReleasePolicy;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceProxyPolicy;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicy;
import org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicy;

import java.util.List;

/**
 * Default implmentation of {@link RegisteredServiceFactory}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public class DefaultRegisteredServiceFactory implements RegisteredServiceFactory {

    private final List<? extends FormDataPopulator> formDataPopulators;

    public DefaultRegisteredServiceFactory(final List<? extends FormDataPopulator> formDataPopulators) {
        this.formDataPopulators = formDataPopulators;
    }

    @Override
    public FormData createFormData() {
        final FormData data = new FormData();
        this.formDataPopulators.forEach(populator -> populator.populateFormData(data));
        return data;
    }

    @Override
    public RegisteredServiceViewBean createServiceViewBean(final RegisteredService svc) {
        final RegisteredServiceViewBean bean = new RegisteredServiceViewBean();
        bean.setEvalOrder(svc.getEvaluationOrder());
        bean.setAssignedId(String.valueOf(svc.getId()));
        bean.setSasCASEnabled(svc.getAccessStrategy().isServiceAccessAllowed());
        bean.setServiceId(svc.getServiceId());
        bean.setName(svc.getName());
        bean.setDescription(svc.getDescription());
        if (svc.getLogo() != null) {
            bean.setLogoUrl(svc.getLogo().toString());
        }
        mapAttributeReleasePolicy(svc.getAttributeReleasePolicy(),bean);
        mapProxyPolicy(svc.getProxyPolicy(),bean);

        return bean;
    }

    private void mapAttributeReleasePolicy(final RegisteredServiceAttributeReleasePolicy policy, final RegisteredServiceViewBean bean) {
        if (policy instanceof AbstractRegisteredServiceAttributeReleasePolicy) {
            final AbstractRegisteredServiceAttributeReleasePolicy attrPolicy = (AbstractRegisteredServiceAttributeReleasePolicy) policy;

            final RegisteredServiceAttributeReleasePolicyViewBean attrPolicyBean = bean.getAttrRelease();
            attrPolicyBean.setReleasePassword(attrPolicy.isAuthorizedToReleaseCredentialPassword());
            attrPolicyBean.setReleaseTicket(attrPolicy.isAuthorizedToReleaseProxyGrantingTicket());
            attrPolicyBean.setExcludeDefault(attrPolicy.isExcludeDefaultAttributes());

            if (attrPolicy instanceof ScriptedRegisteredServiceAttributeReleasePolicy) {
                attrPolicyBean.setAttrPolicy(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.SCRIPT.toString());
            } else if (attrPolicy instanceof GroovyScriptAttributeReleasePolicy) {
                attrPolicyBean.setAttrPolicy(AbstractRegisteredServiceAttributeReleasePolicyStrategyBean.Types.GROOVY.toString());
            } else if (attrPolicy instanceof ReturnAllAttributeReleasePolicy) {
                attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.ALL.toString());
            } else if (attrPolicy instanceof ReturnAllowedAttributeReleasePolicy) {
                final ReturnAllowedAttributeReleasePolicy attrPolicyAllowed = (ReturnAllowedAttributeReleasePolicy) attrPolicy;
                if (attrPolicyAllowed.getAllowedAttributes().isEmpty()) {
                    attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.NONE.toString());
                } else {
                    attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.ALLOWED.toString());
                }
            } else if (attrPolicy instanceof ReturnMappedAttributeReleasePolicy) {
                final ReturnMappedAttributeReleasePolicy attrPolicyAllowed = (ReturnMappedAttributeReleasePolicy) attrPolicy;
                if (attrPolicyAllowed.getAllowedAttributes().isEmpty()) {
                    attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.NONE.toString());
                } else {
                    attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.MAPPED.toString());
                }
            } else if (attrPolicy instanceof DenyAllAttributeReleasePolicy) {
                attrPolicyBean.setAttrPolicy(RegisteredServiceAttributeReleasePolicyStrategyViewBean.Types.DENY.toString());
            }
        }
    }

    private void mapProxyPolicy(final RegisteredServiceProxyPolicy policy, final RegisteredServiceViewBean bean) {
        final RegisteredServiceProxyPolicyBean proxyPolicyBean = bean.getProxyPolicy();

        if (policy instanceof RefuseRegisteredServiceProxyPolicy) {
            proxyPolicyBean.setType(RegisteredServiceProxyPolicyBean.Types.REFUSE);
        } else if (policy instanceof RegexMatchingRegisteredServiceProxyPolicy) {
            final RegexMatchingRegisteredServiceProxyPolicy option = (RegexMatchingRegisteredServiceProxyPolicy) policy;
            proxyPolicyBean.setType(RegisteredServiceProxyPolicyBean.Types.REGEX);
            proxyPolicyBean.setValue(option.getPattern().toString());
        }
    }
}
