package org.apereo.cas.logging;

import com.amazonaws.services.logs.AWSLogs;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;



    /*
     createIfNeeded | createLogGroupIfNeeded | createLogStreamIfNeeded | expectations
     null           | null                   | null                    | createLogGroup > 1, createLogStream > 1
     true           | null                   | null                    | createLogGroup > 1, createLogStream > 1
     null           | true                   | null                    | createLogGroup > 1, createLogStream never
     null           | null                   | true                    | createLogGroup never, createLogStream > 1
     null           | true                   | true                    | createLogGroup > 1, createLogStream > 1
     true           | true                   | null                    | createLogGroup > 1, createLogStream > 1
     ...
     */

public class CloudWatchAppenderTests {
    @Test
    @DisplayName("make sure that log4j plugin file is generated")
    void fileGenerated() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.add(builder.newAppender("cloudwatch", "CloudWatchAppender"));
        Configuration configuration = builder.build();
        Configurator.initialize(configuration);
        assertNotNull(configuration.getAppender("cloudwatch"));
    }

    @ParameterizedTest
    void specTest() {
        AWSLogs mock = Mockito.mock(AWSLogs.class);

    }

    private class TestCase {
        Boolean createIfNeeded
        Boolean createLogGroupIfNeeded
        Boolean createLoStreamIfNeeded

        // some lambda
    }
}
