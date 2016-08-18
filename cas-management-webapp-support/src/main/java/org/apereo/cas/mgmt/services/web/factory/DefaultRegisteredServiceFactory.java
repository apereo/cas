package org.apereo.cas.mgmt.services.web.factory;

import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean.FormData;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.apereo.cas.mgmt.services.web.beans.RegisteredServiceViewBean;
import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceProxyPolicy;
import org.apereo.cas.services.RegisteredServiceUsernameAttributeProvider;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implmentation of {@link RegisteredServiceFactory}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
public class DefaultRegisteredServiceFactory implements RegisteredServiceFactory {

    private MultifactorAuthenticationMapper multifactorAuthenticationMapper = new DefaultMultifactorAuthenticationMapper();
    
    private AccessStrategyMapper accessStrategyMapper = new DefaultAccessStrategyMapper();

    private AttributeReleasePolicyMapper attributeReleasePolicyMapper =
            new DefaultAttributeReleasePolicyMapper(new DefaultAttributeFilterMapper(),
                                                    new DefaultPrincipalAttributesRepositoryMapper());

    private ProxyPolicyMapper proxyPolicyMapper = new DefaultProxyPolicyMapper();

    private RegisteredServiceMapper registeredServiceMapper = new DefaultRegisteredServiceMapper();

    private UsernameAttributeProviderMapper usernameAttributeProviderMapper = 
            new DefaultUsernameAttributeProviderMapper();

    private List<? extends FormDataPopulator> formDataPopulators = new ArrayList<>();

    public void setMultifactorAuthenticationMapper(final MultifactorAuthenticationMapper multifactorAuthenticationMapper) {
        this.multifactorAuthenticationMapper = multifactorAuthenticationMapper;
    }

    public void setAccessStrategyMapper(final AccessStrategyMapper accessStrategyMapper) {
        this.accessStrategyMapper = accessStrategyMapper;
    }

    public void setAttributeReleasePolicyMapper(final AttributeReleasePolicyMapper attributeReleasePolicyMapper) {
        this.attributeReleasePolicyMapper = attributeReleasePolicyMapper;
    }

    public void setProxyPolicyMapper(final ProxyPolicyMapper proxyPolicyMapper) {
        this.proxyPolicyMapper = proxyPolicyMapper;
    }

    public void setRegisteredServiceMapper(final RegisteredServiceMapper registeredServiceMapper) {
        this.registeredServiceMapper = registeredServiceMapper;
    }

    public void setUsernameAttributeProviderMapper(
            final UsernameAttributeProviderMapper usernameAttributeProviderMapper) {
        this.usernameAttributeProviderMapper = usernameAttributeProviderMapper;
    }

    public void setFormDataPopulators(final List<? extends FormDataPopulator> formDataPopulators) {
        this.formDataPopulators = formDataPopulators;
    }

    /**
     * Initializes some default mappers after any custom mappers have been wired.
     */
    @PostConstruct
    public void initializeDefaults() {
    }

    @Override
    public FormData createFormData() {
        final FormData data = new FormData();
        this.formDataPopulators.stream().forEach(populator -> populator.populateFormData(data));
        return data;
    }

    @Override
    public ServiceData createServiceData(final RegisteredService svc) {
        final ServiceData bean = new ServiceData();

        this.registeredServiceMapper.mapRegisteredService(svc, bean);
        this.accessStrategyMapper.mapAccessStrategy(svc.getAccessStrategy(), bean);
        this.usernameAttributeProviderMapper.mapUsernameAttributeProvider(svc.getUsernameAttributeProvider(), bean);
        this.proxyPolicyMapper.mapProxyPolicy(svc.getProxyPolicy(), bean);
        this.attributeReleasePolicyMapper.mapAttributeReleasePolicy(svc.getAttributeReleasePolicy(), bean);
        this.multifactorAuthenticationMapper.mapMultifactorPolicy(svc.getMultifactorPolicy(), bean);
        
        return bean;
    }

    @Override
    public RegisteredServiceViewBean createServiceViewBean(final RegisteredService svc) {
        final RegisteredServiceViewBean bean = new RegisteredServiceViewBean();

        this.registeredServiceMapper.mapRegisteredService(svc, bean);
        this.accessStrategyMapper.mapAccessStrategy(svc.getAccessStrategy(), bean);
        this.usernameAttributeProviderMapper.mapUsernameAttributeProvider(svc.getUsernameAttributeProvider(), bean);
        this.proxyPolicyMapper.mapProxyPolicy(svc.getProxyPolicy(), bean);
        this.attributeReleasePolicyMapper.mapAttributeReleasePolicy(svc.getAttributeReleasePolicy(), bean);

        return bean;
    }

    @Override
    public RegisteredService createRegisteredService(final ServiceData data) {
        final RegisteredService svc = this.registeredServiceMapper.toRegisteredService(data);

        if (svc instanceof AbstractRegisteredService) {
            final AbstractRegisteredService absSvc = (AbstractRegisteredService) svc;

            final RegisteredServiceAccessStrategy accessStrategy = this.accessStrategyMapper.toAccessStrategy(data);
            if (accessStrategy != null) {
                absSvc.setAccessStrategy(accessStrategy);
            }

            final RegisteredServiceUsernameAttributeProvider usernameAttributeProvider =
                    this.usernameAttributeProviderMapper.toUsernameAttributeProvider(data);
            if (usernameAttributeProvider != null) {
                absSvc.setUsernameAttributeProvider(usernameAttributeProvider);
            }

            final RegisteredServiceProxyPolicy proxyPolicy = this.proxyPolicyMapper.toProxyPolicy(data);
            if (proxyPolicy != null) {
                absSvc.setProxyPolicy(proxyPolicy);
            }

            final RegisteredServiceAttributeReleasePolicy attrPolicy = this.attributeReleasePolicyMapper
                    .toAttributeReleasePolicy(data);
            if (attrPolicy != null) {
                absSvc.setAttributeReleasePolicy(attrPolicy);
            }
            
            final RegisteredServiceMultifactorPolicy mfaPolicy = this.multifactorAuthenticationMapper.toMultifactorPolicy(data);
            if (mfaPolicy != null) {
                absSvc.setMultifactorPolicy(mfaPolicy);
            }
        }

        return svc;
    }
}
