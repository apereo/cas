package org.apereo.cas.util.spring.boot;

import org.apereo.cas.configuration.model.core.CasServerProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.*;

/**
 * This is {@link BeanDefinitionStoreFailureAnalyzerTests}.
 *
 * @author Hal Deadman
 * @since 6.4.0
 */
@Tag("Utility")
class BeanDefinitionStoreFailureAnalyzerTests {

    @Test
    void analyzeBeanDefinitionStoreException() {
        val analysis = performAnalysis();
        val description = analysis.getDescription();
        assertThat(description).contains("not.defined");
    }

    @Test
    void analyzeBeanDefinitionStoreExceptionFullMsg() {
        val analysis = new BeanDefinitionStoreFailureAnalyzer().analyze(
            new BeanDefinitionStoreException("resourcedesc", "beanname", "themsg"));
        val description = analysis.getDescription();
        assertThat(description).contains("resourcedesc");
        assertThat(description).contains("beanname");
        assertThat(description).contains("themsg");
        val analysis2 = new BeanDefinitionStoreFailureAnalyzer().analyze(
            new BeanDefinitionStoreException("beanname", "themsg", new IllegalStateException("thecause")));
        val description2 = analysis2.getDescription();
        assertThat(description2).contains("beanname");
        assertThat(description2).contains("themsg");
        assertThat(description2).contains("thecause");
    }

    private static FailureAnalysis performAnalysis() {
        val failure = createFailure();
        assertNotNull(failure);
        return new BeanDefinitionStoreFailureAnalyzer().analyze(failure);
    }

    private static BeanDefinitionStoreException createFailure() {
        val bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("testBean",
            genericBeanDefinition(CasServerProperties.class)
                .addPropertyValue("name", "${not.defined}")
                .getBeanDefinition());

        val ppc = new PropertySourcesPlaceholderConfigurer();
        try {
            ppc.postProcessBeanFactory(bf);
        } catch (final BeanDefinitionStoreException e) {
            return e;
        }
        return null;
    }
}


