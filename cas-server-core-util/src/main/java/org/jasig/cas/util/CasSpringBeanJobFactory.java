package org.jasig.cas.util;

import org.quartz.spi.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.validation.constraints.NotNull;

/**
 * Creates quartz job, and autowires them based on the application context.
 * @author Misagh Moayyed
 * @since 4.2
 */

public class CasSpringBeanJobFactory extends SpringBeanJobFactory {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @NotNull
    private final ApplicationContext applicationContext;

    /**
     * Instantiates a new Cas spring bean job factory.
     *
     * @param applicationContext the application context
     */
    @Autowired
    public CasSpringBeanJobFactory(final ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
        final AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
        final Object job = super.createJobInstance(bundle);
        logger.debug("Created job {} for bundle {}", job, bundle);
        beanFactory.autowireBean(job);
        logger.debug("Autowired job per the application context");
        return job;
    }
}
