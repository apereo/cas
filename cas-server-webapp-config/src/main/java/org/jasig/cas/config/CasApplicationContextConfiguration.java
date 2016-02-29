package org.jasig.cas.config;

import org.jasig.cas.authentication.principal.ServiceFactory;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.ticket.UniqueTicketIdGenerator;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
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
 * @since 4.3.0
 */
@Configuration("casApplicationContextConfiguration")
public class CasApplicationContextConfiguration {

    /**
     * The constant URL_HANDLER_MAPPING_ORDER.
     */
    private static final int URL_HANDLER_MAPPING_ORDER = 1000;

    /**
     * The Service ticket unique id generator.
     */
    @Autowired
    @Qualifier("serviceTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator serviceTicketUniqueIdGenerator;

    /**
     * The Web application service factory.
     */
    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    /**
     * The Default argument extractor.
     */
    @NotNull
    @Autowired
    @Qualifier("defaultArgumentExtractor")
    private ArgumentExtractor defaultArgumentExtractor;

    /**
     * The Cas spring bean job factory.
     */
    @Autowired
    @Qualifier("casSpringBeanJobFactory")
    private SpringBeanJobFactory casSpringBeanJobFactory;

    /**
     * The Wait for jobs to complete on shutdown.
     */
    @Value("${scheduler.shutdown.wait:true}")
    private boolean waitForJobsToCompleteOnShutdown;

    /**
     * The Interrupt jobs.
     */
    @Value("${scheduler.shutdown.interruptJobs:true}")
    private boolean interruptJobs;

    /**
     * Advisor auto proxy creator default advisor auto proxy creator.
     *
     * @return the default advisor auto proxy creator
     */
    @Bean(name = "advisorAutoProxyCreator")
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    /**
     * Service factory list list.
     *
     * @return the list
     */
    @Bean(name = "serviceFactoryList")
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
    @Bean(name = "argumentExtractors")
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
    @Bean(name = "uniqueIdGeneratorsMap")
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
    @Bean(name = "passThroughController")
    protected UrlFilenameViewController passThroughController() {
        return new UrlFilenameViewController();
    }

    /**
     * Root controller controller.
     *
     * @return the controller
     */
    @Bean(name="rootController")
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
     * Scheduler scheduler factory bean.
     *
     * @return the scheduler factory bean
     */
    @Bean(name = "scheduler")
    public SchedulerFactoryBean scheduler() {
        final SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setWaitForJobsToCompleteOnShutdown(this.waitForJobsToCompleteOnShutdown);
        factory.setJobFactory(this.casSpringBeanJobFactory);

        final Properties properties = new Properties();
        properties.put("org.quartz.scheduler.interruptJobsOnShutdown", this.interruptJobs);
        properties.put("org.quartz.scheduler.interruptJobsOnShutdownWithWait", this.interruptJobs);
        factory.setQuartzProperties(properties);
        return factory;
    }

    /**
     * Handler mapping c simple url handler mapping.
     *
     * @return the simple url handler mapping
     */
    @Bean(name = "handlerMappingC")
    public SimpleUrlHandlerMapping handlerMappingC() {
        final SimpleUrlHandlerMapping bean = new SimpleUrlHandlerMapping();
        bean.setOrder(URL_HANDLER_MAPPING_ORDER);
        bean.setAlwaysUseFullPath(true);

        final Properties properties = new Properties();
        properties.put("/authorizationFailure.html", passThroughController());
        properties.put("/", rootController());
        bean.setMappings(properties);
        return bean;
    }
}
