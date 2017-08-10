package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.mgmt.services.web.beans.FormData;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegisteredServiceUsernameAttributeProvider;

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
        //bean.setLogoUrl(svc.getLogo().toString());
        //bean.setProxyPolicy(svc.getProxyPolicy());
        //bean.setAttrRelease(svc.getAttributeReleasePolicy());

        return bean;
    }
}
