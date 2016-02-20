package org.jasig.cas.config;

import org.jasig.cas.authentication.principal.ServiceFactory;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;

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

    private static final int URL_HANDLER_MAPPING_ORDER = 1000;
    
    @Autowired
    @Qualifier("serviceTicketUniqueIdGenerator")
    private UniqueTicketIdGenerator serviceTicketUniqueIdGenerator;

    @Autowired
    @Qualifier("webApplicationServiceFactory")
    private ServiceFactory<WebApplicationService> webApplicationServiceFactory;

    @NotNull
    @Autowired
    @Qualifier("defaultArgumentExtractor")
    private ArgumentExtractor defaultArgumentExtractor;

    @NotNull
    @Autowired
    @Qualifier("passThroughController")
    private Controller passThroughController;
        
    @Autowired
    @Qualifier("casSpringBeanJobFactory")
    private SpringBeanJobFactory casSpringBeanJobFactory;

    @Value("${scheduler.shutdown.wait:true}")
    private boolean waitForJobsToCompleteOnShutdown;

    @Value("${scheduler.shutdown.interruptJobs:true}")
    private boolean interruptJobs;

    @Bean(name = "advisorAutoProxyCreator")
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    @Bean(name = "serviceFactoryList")
    public List serviceFactoryList() {
        final List<ServiceFactory> list = new ArrayList<>();
        list.add(this.webApplicationServiceFactory);
        return list;
    }

    @Bean(name = "argumentExtractors")
    public List argumentExtractors() {
        final List<ArgumentExtractor> list = new ArrayList<>();
        list.add(this.defaultArgumentExtractor);
        return list;
    }

    @Bean(name = "uniqueIdGeneratorsMap")
    public Map uniqueIdGeneratorsMap() {
        final Map<String, UniqueTicketIdGenerator> map = new HashMap<>();
        map.put("org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl", this.serviceTicketUniqueIdGenerator);
        return map;
    }

    @Bean(name = "passThroughController")
    public UrlFilenameViewController passThroughController() {
        return new UrlFilenameViewController();
    }

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

    @Bean(name = "handlerMappingC")
    public SimpleUrlHandlerMapping handlerMappingC() {
        final SimpleUrlHandlerMapping bean = new SimpleUrlHandlerMapping();
        bean.setOrder(URL_HANDLER_MAPPING_ORDER);
        bean.setAlwaysUseFullPath(true);

        final Properties properties = new Properties();
        properties.put("/authorizationFailure.html", this.passThroughController);
        bean.setMappings(properties);
        return bean;
    }
}
