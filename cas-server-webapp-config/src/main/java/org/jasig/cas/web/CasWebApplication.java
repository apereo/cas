package org.jasig.cas.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * This is {@link CasWebApplication}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@SpringBootApplication(scanBasePackages = {"org.jasig.cas"},
        exclude={HibernateJpaAutoConfiguration.class})
@ImportResource(locations = {"/WEB-INF/spring-configuration/*.xml", 
                             "/WEB-INF/spring-configuration/*.groovy",
                             "/WEB-INF/deployerConfigContext.xml", 
                             "classpath*:/META-INF/spring/*.xml"})
@Import(AopAutoConfiguration.class)
public class CasWebApplication {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Instantiates a new Cas web application.
     */
    protected CasWebApplication() {}


    /**
     * Main entry point of the CAS web application.
     *
     * @param args the args
     */
    public static void main(final String[] args) {
        SpringApplication.run(CasWebApplication.class, args);
    }
    
}
