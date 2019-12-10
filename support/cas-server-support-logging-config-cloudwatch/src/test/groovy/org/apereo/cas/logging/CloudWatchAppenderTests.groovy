package org.apereo.cas.logging

import com.amazonaws.services.logs.AWSLogs
import com.amazonaws.services.logs.model.CreateLogGroupRequest
import com.amazonaws.services.logs.model.CreateLogStreamRequest
import com.amazonaws.services.logs.model.DescribeLogGroupsRequest
import com.amazonaws.services.logs.model.DescribeLogGroupsResult
import com.amazonaws.services.logs.model.DescribeLogStreamsRequest
import com.amazonaws.services.logs.model.DescribeLogStreamsResult
import com.amazonaws.services.logs.model.LogGroup
import com.amazonaws.services.logs.model.LogStream
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class CloudWatchAppenderTests {
    @Test
    void "make sure that log4j plugin file is generated"() {
        def builder = ConfigurationBuilderFactory.newConfigurationBuilder()
        def cw = builder.newAppender('cloudwatch', 'CloudWatchAppender')
        builder.add(cw)
        def configuration = builder.build()
        Configurator.initialize(configuration)

        assert configuration.getAppender('cloudwatch') != null
    }

    @Test
    void "`createIfNeeded` is false"() {
        def mock = Mockito.mock(AWSLogs)
        Mockito.when(mock.describeLogStreams(Mockito.any(DescribeLogStreamsRequest))).thenReturn(new DescribeLogStreamsResult().with {
            it.logStreams.add(new LogStream().with {
                it.logStreamName = 'test'
                it.uploadSequenceToken = 'test'
                it
            })
            it
        })
        Mockito.when(mock.describeLogGroups(Mockito.any(DescribeLogGroupsRequest))).thenReturn(new DescribeLogGroupsResult().with {
            it.logGroups.add(new LogGroup().with {
                it.logGroupName = 'test'
                it
            })
            it
        })

        // we do this because the lifecycle is a little different for this sort of programmatic configuration
        def appender = new CloudWatchAppender('test', 'test', 'test', '30', null, false, mock)
        appender.initialize()

        def builder = ConfigurationBuilderFactory.newConfigurationBuilder()
        def configuration = builder.build()
        configuration.addAppender(appender)
        Configurator.initialize(configuration)

        def logger = LogManager.getLogger('test')
        logger.info('here is a message')

        Mockito.verify(mock, Mockito.never()).createLogGroup()
        Mockito.verify(mock, Mockito.never()).createLogStream()
    }

    @Test
    void "`createIfNeeded` is true"() {
        def mock = Mockito.mock(AWSLogs)

        // we do this because the lifecycle is a little different for this sort of programmatic configuration
        def appender = new CloudWatchAppender('test', 'test', 'test', '30', null, true, mock)
        appender.initialize()

        def builder = ConfigurationBuilderFactory.newConfigurationBuilder()
        def configuration = builder.build()
        configuration.addAppender(appender)
        Configurator.initialize(configuration)

        def logger = LogManager.getLogger('test')
        logger.info('here is a message')

        Mockito.verify(mock, Mockito.atLeastOnce()).createLogGroup(Mockito.any(CreateLogGroupRequest))
        Mockito.verify(mock, Mockito.atLeastOnce()).createLogStream(Mockito.any(CreateLogStreamRequest))
    }
}
