package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.PrefixedEnvironmentPropertiesFactoryBean;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.NamedStubPersonAttributeDao;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link CasApplicationContextConfiguration}.that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casApplicationContextConfiguration")
public class CasApplicationContextConfiguration {
    
    /**
     * The Service ticket unique id generator.
     */
    @Resource(name="serviceTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator serviceTicketUniqueIdGenerator;

    /**
     * The Web application service factory.
     */
    @Resource(name="webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    /**
     * The Default argument extractor.
     */
    
    @Resource(name="defaultArgumentExtractor")
    private ArgumentExtractor defaultArgumentExtractor;
    
    /**
     * Advisor auto proxy creator default advisor auto proxy creator.
     *
     * @return the default advisor auto proxy creator
     */
    @RefreshScope
    @Bean
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    /**
     * Service factory list list.
     *
     * @return the list
     */
    @RefreshScope
    @Bean
    public List serviceFactoryList() {
        final List<ServiceFactory> list = new ArrayList<>();
        list.add(this.webApplicationServiceFactory);
        return list;
    }

    /**
     * Argument extractors list.
     *
     * @return the list
     */
    @RefreshScope
    @Bean
    public List argumentExtractors() {
        final List<ArgumentExtractor> list = new ArrayList<>();
        list.add(this.defaultArgumentExtractor);
        return list;
    }

    /**
     * Unique id generators map map.
     *
     * @return the map
     */
    @RefreshScope
    @Bean
    public Map uniqueIdGeneratorsMap() {
        final Map<String, UniqueTicketIdGenerator> map = new HashMap<>();
        map.put(SimpleWebApplicationServiceImpl.class.getName(), this.serviceTicketUniqueIdGenerator);
        return map;
    }

    /**
     * Pass through controller url filename view controller.
     *
     * @return the url filename view controller
     */
    @RefreshScope
    @Bean
    protected UrlFilenameViewController passThroughController() {
        return new UrlFilenameViewController();
    }

    /**
     * Root controller controller.
     *
     * @return the controller
     */
    @RefreshScope
    @Bean
    protected Controller rootController() {
        return new ParameterizableViewController() {
            @Override
            protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) 
                    throws Exception {
                final String queryString = request.getQueryString();
                final String url = request.getContextPath() + "/login" + (queryString != null ? '?' + queryString : "");
                return new ModelAndView(new RedirectView(response.encodeURL(url)));
            }
        };
    }

    /**
     * Stub attribute repository person attribute dao.
     *
     * @param factoryBean the factory bean
     * @return the person attribute dao
     */
    @Bean
    public IPersonAttributeDao stubAttributeRepository(@Qualifier("casAttributesToResolve")
                                                   final FactoryBean<Properties> factoryBean) {
        try {
            final NamedStubPersonAttributeDao dao = new NamedStubPersonAttributeDao();
            dao.setBackingMap(new HashMap(factoryBean.getObject()));
            return dao;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cas attributes to resolve factory bean.
     *
     * @return the factory bean
     */
    @Bean
    public FactoryBean<Properties> casAttributesToResolve() {
        final PrefixedEnvironmentPropertiesFactoryBean bean = new PrefixedEnvironmentPropertiesFactoryBean();
        bean.setPrefix("cas.attrs.resolve.");
        return bean;
    }
}
