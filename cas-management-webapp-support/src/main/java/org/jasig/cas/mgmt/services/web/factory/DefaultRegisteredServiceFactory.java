package org.jasig.cas.mgmt.services.web.factory;

import org.jasig.cas.services.AbstractRegisteredService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAccessStrategy;
import org.jasig.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.jasig.cas.services.RegisteredServiceProxyPolicy;
import org.jasig.cas.services.RegisteredServiceUsernameAttributeProvider;
import org.jasig.cas.mgmt.services.web.beans.RegisteredServiceEditBean.FormData;
import org.jasig.cas.mgmt.services.web.beans.RegisteredServiceEditBean.ServiceData;
import org.jasig.cas.mgmt.services.web.beans.RegisteredServiceViewBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Default implmentation of {@link RegisteredServiceFactory}.
 *
 * @author Daniel Frett
 * @since 4.2
 */
@RefreshScope
@Component("registeredServiceFactory")
public class DefaultRegisteredServiceFactory implements RegisteredServiceFactory {
    @Autowired(required = false)
    private ApplicationContext applicationContext;

    
    @Autowired(required = false)
    @Qualifier("accessStrategyMapper")
    private AccessStrategyMapper accessStrategyMapper;

    
    @Autowired(required = false)
    @Qualifier("attributeReleasePolicyMapper")
    private AttributeReleasePolicyMapper attributeReleasePolicyMapper;

    
    @Autowired(required = false)
    @Qualifier("proxyPolicyMapper")
    private ProxyPolicyMapper proxyPolicyMapper;

    
    @Autowired(required = false)
    @Qualifier("registeredServiceMapper")
    private RegisteredServiceMapper registeredServiceMapper;

    
    @Autowired(required = false)
    @Qualifier("usernameAttributeProviderMapper")
    private UsernameAttributeProviderMapper usernameAttributeProviderMapper;

    
    @Autowired
    private List<? extends FormDataPopulator> formDataPopulators;

    public void setApplicationContext(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
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
        // use default mappers from spring context
        if (this.applicationContext != null) {
            if (this.accessStrategyMapper == null) {
                this.accessStrategyMapper = this.applicationContext.getBean(
                        DefaultAccessStrategyMapper.BEAN_NAME,
                        AccessStrategyMapper.class);
            }
            if (this.attributeReleasePolicyMapper == null) {
                this.attributeReleasePolicyMapper = this.applicationContext.getBean(
                        DefaultAttributeReleasePolicyMapper.BEAN_NAME,
                        AttributeReleasePolicyMapper.class);
            }
            if (this.proxyPolicyMapper == null) {
                this.proxyPolicyMapper = this.applicationContext.getBean(
                        DefaultProxyPolicyMapper.BEAN_NAME,
                        ProxyPolicyMapper.class);
            }
            if (this.registeredServiceMapper == null) {
                this.registeredServiceMapper = this.applicationContext.getBean(
                        DefaultRegisteredServiceMapper.BEAN_NAME,
                        RegisteredServiceMapper.class);
            }
            if (this.usernameAttributeProviderMapper == null) {
                this.usernameAttributeProviderMapper = this.applicationContext.getBean(
                        DefaultUsernameAttributeProviderMapper.BEAN_NAME,
                        UsernameAttributeProviderMapper.class);
            }
        }

        // initialize default mappers if any are still missing
        if (this.accessStrategyMapper == null) {
            this.accessStrategyMapper = new DefaultAccessStrategyMapper();
        }
        if (this.attributeReleasePolicyMapper == null) {
            final DefaultAttributeReleasePolicyMapper policyMapper = new DefaultAttributeReleasePolicyMapper();
            policyMapper.setApplicationContext(this.applicationContext);
            policyMapper.initializeDefaults();
            this.attributeReleasePolicyMapper = policyMapper;
        }
        if (this.proxyPolicyMapper == null) {
            this.proxyPolicyMapper = new DefaultProxyPolicyMapper();
        }
        if (this.registeredServiceMapper == null) {
            this.registeredServiceMapper = new DefaultRegisteredServiceMapper();
        }
        if (this.usernameAttributeProviderMapper == null) {
            this.usernameAttributeProviderMapper = new DefaultUsernameAttributeProviderMapper();
        }
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
        }

        return svc;
    }
}
