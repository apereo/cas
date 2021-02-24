package org.apereo.cas.util.spring.boot;

import org.apereo.cas.util.LoggingUtils;


import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Failure analyzer for spring boot startup exceptions from {@link BeanDefinitionStoreException}.
 * @author Hal Deadman
 * @since 6.4.0
 */
@Slf4j
public class BeanDefinitionStoreFailureAnalyzer extends AbstractFailureAnalyzer<BeanDefinitionStoreException> {

    private static final String ANALYSIS = "Review the properties available for the configuration. Enable debug logging on "
            + BeanDefinitionStoreFailureAnalyzer.class.getName() + " to see exception stack trace";

    @Override
    protected FailureAnalysis analyze(final Throwable rootFailure, final BeanDefinitionStoreException cause) {
        if (LOGGER.isDebugEnabled()) {
            LoggingUtils.error(LOGGER, getDescription(cause), cause);
        }
        return new FailureAnalysis(getDescription(cause), ANALYSIS, cause);
    }

    private static String getDescription(final BeanDefinitionStoreException ex) {
        val causedMsg = ExceptionUtils.getRootCauseMessage(ex);
        val description = new StringWriter();
        val printer = new PrintWriter(description);

        printer.printf("Error creating bean");
        if (ex.getBeanName() != null) {
            printer.printf(" named %s", ex.getBeanName());
        }
        if (ex.getResourceDescription() != null) {
            printer.printf(", with resource description %s,", ex.getResourceDescription());
        }
        printer.printf(" due to: %s ", ex.getMessage());
        if (StringUtils.isNotBlank(causedMsg)) {
            printer.printf(" caused by %s ", causedMsg);
        }
        return description.toString();
    }
}
