# Logback Logging

Source: https://apereo.github.io/cas/development/logging/Logging-Logback.html

CAS does also support [Logback](https://logback.qos.ch/) as an alternative logging engine. At a high level, the Logback architecture is similar to that of [Log4j](https://apereo.github.io/cas/development/logging/Logging.html) where you have `Logger`, `Appender` and `Layout` components typically defined inside a `logback.xml` file.

Refer to the [Logback documentation](https://logback.qos.ch/documentation.html) to learn more.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-logback"
}
```

You must also make sure the following modules and dependencies are excluded from the WAR overlay:

```groovy
configurations.all {
    exclude(group: "org.apache.logging.log4j", module: "log4j-api")
    exclude(group: "org.apache.logging.log4j", module: "log4j-jakarta-web")
    exclude(group: "org.apache.logging.log4j", module: "log4j-web")
    exclude(group: "org.apache.logging.log4j", module: "log4j-jcl")
    exclude(group: "org.apache.logging.log4j", module: "log4j-slf4j-impl")
    exclude(group: "org.apache.logging.log4j", module: "log4j-slf4j2-impl")
    
    exclude(group: "org.apereo.cas", module: "cas-server-core-logging")
}
```

A sample `logback.xml` file follows:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="30 seconds">
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <layout class="ch.qos.logback.classic.PatternLayout">
            <Pattern>%white(%d{yyyy-MM-dd HH:mm:ss}) %highlight(%-5level) %cyan(%logger{15}) - %msg%n</Pattern>
        </layout>
    </appender>
    <logger name="org.apereo.cas" level="info" additivity="false">
        <appender-ref ref="console" />
    </logger>
    <root level="info">
        <appender-ref ref="console" />
    </root>
</configuration>
```

> **Be Careful**
>
> Sanitizing log data to remove sensitive ticket ids such as ticket-granting tickets or proxy-granting tickets is not handled by CAS when Logback is used. While this may be worked out in future releases, you should be extra careful to cleanse log data prior to sharing it with external systems such as Splunk or Syslog, etc.

The following settings and properties are available from the CAS configuration catalog:

- `logging.charset.console` (Third party; type `Charset`): Charset to use for console output.
- `logging.charset.file` (Third party; type `Charset`): Charset to use for file output.
- `logging.config` (Third party; type `String`): Location of the logging configuration file. For instance, `classpath:logback.xml` for Logback.
- `logging.console.enabled` (Third party; type `Boolean`): Whether to enable console-based logging.
- `logging.exception-conversion-word` (Third party; type `String`; default `%wEx`): Conversion word used when logging exceptions.
- `logging.file` (Third party; type `String`; deprecation `ERROR`, replaced by `logging.file.name`): Log file name (for instance, `myapp.log`). Names can be an exact location or relative to the current directory.
- `logging.file.clean-history-on-start` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `logging.logback.rollingpolicy.clean-history-on-start`)
- `logging.file.max-history` (Third party; type `Integer`; deprecation `ERROR`, replaced by `logging.logback.rollingpolicy.max-history`)
- `logging.file.max-size` (Third party; type `DataSize`; deprecation `ERROR`, replaced by `logging.logback.rollingpolicy.max-file-size`)
- `logging.file.name` (Third party; type `String`): Log file name (for instance, `myapp.log`). Names can be an exact location or relative to the current directory.
- `logging.file.path` (Third party; type `String`): Location of the log file. For instance, `/var/log`.
- `logging.file.total-size-cap` (Third party; type `DataSize`; deprecation `ERROR`, replaced by `logging.logback.rollingpolicy.total-size-cap`)
- `logging.group` (Third party; type `Map<String,List<String>>`): Log groups to quickly change multiple loggers at the same time. For instance, `logging.group.db=org.hibernate,org.springframework.jdbc`.
- `logging.include-application-group` (Third party; type `Boolean`; default `true`): Whether to include the application group in the logs.
- `logging.include-application-name` (Third party; type `Boolean`; default `true`): Whether to include the application name in the logs.
- `logging.level` (Third party; type `Map<String,String>`): Log levels severity mapping. For instance, `logging.level.org.springframework=DEBUG`.
- `logging.log4j2.config.override` (Third party; type `List<String>`): Overriding configuration files used to create a composite configuration. Can be prefixed with 'optional:' to only load the override if it exists.
- `logging.log4j2.rollingpolicy.cron` (Third party; type `String`; default `0 0 0 * * ?`): Cron expression used when the strategy is 'cron'.
- `logging.log4j2.rollingpolicy.file-name-pattern` (Third party; type `String`; default `${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz`): Pattern for rolled-over log file names.
- `logging.log4j2.rollingpolicy.max-file-size` (Third party; type `DataSize`; default `10MB`): Maximum log file size.
- `logging.log4j2.rollingpolicy.max-history` (Third party; type `Integer`; default `7`): Maximum number of archive log files to keep.
- `logging.log4j2.rollingpolicy.strategy` (Third party; type `RollingPolicyStrategy`; default `size`): Rolling policy strategy.
- `logging.log4j2.rollingpolicy.time-interval` (Third party; type `Integer`; default `1`): Time based triggering interval when the strategy is 'time' or 'size-and-time'.
- `logging.log4j2.rollingpolicy.time-modulate` (Third party; type `Boolean`; default `false`): Whether to align the next rollover time to occur at the top of the interval when the strategy is time based.
- `logging.logback.rollingpolicy.clean-history-on-start` (Third party; type `Boolean`; default `false`): Whether to clean the archive log files on startup.
- `logging.logback.rollingpolicy.file-name-pattern` (Third party; type `String`; default `${LOG_FILE}.%d{yyyy-MM-dd}.%i.gz`): Pattern for rolled-over log file names.
- `logging.logback.rollingpolicy.max-file-size` (Third party; type `DataSize`; default `10MB`): Maximum log file size.
- `logging.logback.rollingpolicy.max-history` (Third party; type `Integer`; default `7`): Maximum number of archive log files to keep.
- `logging.logback.rollingpolicy.total-size-cap` (Third party; type `DataSize`; default `0B`): Total size of log backups to be kept.
- `logging.path` (Third party; type `String`; deprecation `ERROR`, replaced by `logging.file.path`): Location of the log file. For instance, `/var/log`.
- `logging.pattern.console` (Third party; type `String`): Appender pattern for output to the console. Its default value varies according to the logging system.
- `logging.pattern.correlation` (Third party; type `String`): Appender pattern for log correlation. Its default value varies according to the logging system.
- `logging.pattern.dateformat` (Third party; type `String`; default `yyyy-MM-dd'T'HH:mm:ss.SSSXXX`): Appender pattern for log date format.
- `logging.pattern.file` (Third party; type `String`): Appender pattern for output to a file. Its default value varies according to the logging system.
- `logging.pattern.level` (Third party; type `String`; default `%5p`): Appender pattern for log level.
- `logging.pattern.rolling-file-name` (Third party; type `String`; deprecation `ERROR`, replaced by `logging.logback.rollingpolicy.file-name-pattern`)
- `logging.register-shutdown-hook` (Third party; type `Boolean`; default `true`): Register a shutdown hook for the logging system when it is initialized. Disabled automatically when deployed as a war file.
- `logging.structured.ecs.service.environment` (Third party; type `String`): Structured ECS service environment.
- `logging.structured.ecs.service.name` (Third party; type `String`): Structured ECS service name (defaults to 'spring.application.name').
- `logging.structured.ecs.service.node-name` (Third party; type `String`): Structured ECS service node name.
- `logging.structured.ecs.service.version` (Third party; type `String`): Structured ECS service version (defaults to 'spring.application.version').
- `logging.structured.format.console` (Third party; type `String`): Structured logging format for output to the console. Must be either a format id or a fully qualified class name.
- `logging.structured.format.file` (Third party; type `String`): Structured logging format for output to a file. Must be either a format id or a fully qualified class name.
- `logging.structured.gelf.host` (Third party; type `String`): Structured GELF host (defaults to 'spring.application.name').
- `logging.structured.gelf.service.version` (Third party; type `String`): Structured GELF service version (defaults to 'spring.application.version').
- `logging.structured.json.add` (Third party; type `Map<String,String>`): Additional members that should be added to structured logging JSON
- `logging.structured.json.context.include` (Third party; type `Boolean`): Whether context data should be included in the JSON.
- `logging.structured.json.context.prefix` (Third party; type `String`): The prefix to use when inserting context data.
- `logging.structured.json.customizer` (Third party; type `Set<Class<? extends StructuredLoggingJsonMembersCustomizer<?>>>`): Fully qualified class names of StructuredLoggingJsonMembersCustomizer implementations
- `logging.structured.json.exclude` (Third party; type `Set<String>`): Member paths that should be excluded from structured logging JSON
- `logging.structured.json.include` (Third party; type `Set<String>`): Member paths that should be included in structured logging JSON
- `logging.structured.json.rename` (Third party; type `Map<String,String>`): Mapping between member paths and an alternative name that should be used in structured logging JSON
- `logging.structured.json.stacktrace.include-common-frames` (Third party; type `Boolean`): Whether common frames should be included.
- `logging.structured.json.stacktrace.include-hashes` (Third party; type `Boolean`): Whether stack trace hashes should be included.
- `logging.structured.json.stacktrace.max-length` (Third party; type `Integer`): Maximum length to print.
- `logging.structured.json.stacktrace.max-throwable-depth` (Third party; type `Integer`): Maximum throwable depth to print.
- `logging.structured.json.stacktrace.printer` (Third party; type `String`): Name of the printer to use. Can be 'standard', 'logging-system', or the fully-qualified class name of a StackTracePrinter. When not specified 'logging-system' or 'standard' will be used depending if other properties are set.
- `logging.structured.json.stacktrace.root` (Third party; type `StructuredLoggingJsonProperties.StackTrace.Root`): Root ordering (root first or root last).
- `logging.threshold.console` (Third party; type `String`; default `TRACE`): Log level threshold for console output.
- `logging.threshold.file` (Third party; type `String`; default `TRACE`): Log level threshold for file output.
