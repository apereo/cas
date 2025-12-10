package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.shell.commands.CasShellCommand;
import org.apereo.cas.shell.commands.cipher.StringableCipherExecutorCommand;
import org.apereo.cas.shell.commands.db.GenerateDdlCommand;
import org.apereo.cas.shell.commands.jasypt.JasyptDecryptPropertyCommand;
import org.apereo.cas.shell.commands.jasypt.JasyptEncryptPropertyCommand;
import org.apereo.cas.shell.commands.jasypt.JasyptListAlgorithmsCommand;
import org.apereo.cas.shell.commands.jasypt.JasyptListProvidersCommand;
import org.apereo.cas.shell.commands.jasypt.JasyptTestAlgorithmsCommand;
import org.apereo.cas.shell.commands.jwt.GenerateFullJwtCommand;
import org.apereo.cas.shell.commands.jwt.GenerateJwtCommand;
import org.apereo.cas.shell.commands.oidc.GenerateOidcJsonWebKeystoreCommand;
import org.apereo.cas.shell.commands.properties.AddPropertiesToConfigurationCommand;
import org.apereo.cas.shell.commands.properties.ConvertPropertiesToYAMLCommand;
import org.apereo.cas.shell.commands.properties.ExportPropertiesCommand;
import org.apereo.cas.shell.commands.properties.FindPropertiesCommand;
import org.apereo.cas.shell.commands.saml.GenerateSamlIdPMetadataCommand;
import org.apereo.cas.shell.commands.services.AnonymousUsernameAttributeProviderCommand;
import org.apereo.cas.shell.commands.services.GenerateYamlRegisteredServiceCommand;
import org.apereo.cas.shell.commands.services.ValidateRegisteredServiceCommand;
import org.apereo.cas.shell.commands.util.ValidateEndpointCommand;
import org.apereo.cas.shell.commands.util.ValidateLdapConnectionCommand;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.shell.boot.SpringShellProperties;
import org.springframework.shell.core.NonInteractiveShellRunner;
import org.springframework.shell.core.ShellRunner;
import org.springframework.shell.core.command.CommandContext;
import org.springframework.shell.core.command.CommandParser;
import org.springframework.shell.core.command.CommandRegistry;
import org.springframework.shell.core.command.ExitStatus;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.support.CommandFactoryBean;
import org.springframework.shell.core.commands.AbstractCommand;
import org.springframework.shell.jline.JLineInputProvider;
import org.springframework.shell.jline.JLineShellRunner;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.util.ReflectionUtils;

/**
 * This is {@link CasCommandLineShellAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties({CasConfigurationProperties.class, SpringShellProperties.class})
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core, module = "shell")
@AutoConfiguration
public class CasCommandLineShellAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "casShellRunner")
    public ShellRunner shellRunner(final SpringShellProperties properties,
                                   final JLineInputProvider inputProvider,
                                   final PromptProvider promptProvider,
                                   final CommandParser commandParser,
                                   final CommandRegistry commandRegistry) {
        inputProvider.setPromptProvider(promptProvider);
        return properties.getInteractive().isEnabled()
            ? new JLineShellRunner(inputProvider, commandParser, commandRegistry)
            : new NonInteractiveShellRunner(commandParser, commandRegistry);
    }

    @Bean
    @Lazy(false)
    public InitializingBean casShellCommandRegistar(
        @Qualifier("commandRegistry")
        final CommandRegistry commandRegistry,
        final ConfigurableApplicationContext applicationContext) {
        return () -> {
            val commands = applicationContext.getBeansOfType(CasShellCommand.class).values();
            commands.forEach(command -> {
                val cls = command.getClass();
                val methods = MethodIntrospector.selectMethods(cls,
                    (ReflectionUtils.MethodFilter) method -> AnnotatedElementUtils.hasAnnotation(method, Command.class));
                for (val method : methods) {
                    val factoryBean = new CommandFactoryBean(method);
                    factoryBean.setApplicationContext(applicationContext);
                    commandRegistry.registerCommand(factoryBean.getObject());
                }
            });
        };
    }


    @Bean
    public PromptProvider promptProvider() {
        return () -> new AttributedString("cas>", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }

    @Bean
    @SuppressWarnings("ConditionalOnProperty")
    @ConditionalOnProperty(prefix = "spring.shell.interactive", name = "enabled", havingValue = "false")
    org.springframework.shell.core.command.Command quitCommand() {
        return new AbstractCommand("quit", "Exit the shell") {
            @Override
            public ExitStatus doExecute(final CommandContext commandContext) {
                println("Quitting the shell...", commandContext);
                return ExitStatus.EXECUTION_ERROR;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "stringableCipherExecutorCommand")
    public StringableCipherExecutorCommand stringableCipherExecutorCommand() {
        return new StringableCipherExecutorCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "generateDdlCommand")
    public GenerateDdlCommand generateDdlCommand() {
        return new GenerateDdlCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "jasyptDecryptPropertyCommand")
    public JasyptDecryptPropertyCommand jasyptDecryptPropertyCommand() {
        return new JasyptDecryptPropertyCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "jasyptEncryptPropertyCommand")
    public JasyptEncryptPropertyCommand jasyptEncryptPropertyCommand() {
        return new JasyptEncryptPropertyCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "jasyptListAlgorithmsCommand")
    public JasyptListAlgorithmsCommand jasyptListAlgorithmsCommand() {
        return new JasyptListAlgorithmsCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "jasyptListProvidersCommand")
    public JasyptListProvidersCommand jasyptListProvidersCommand() {
        return new JasyptListProvidersCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "jasyptTestAlgorithmsCommand")
    public JasyptTestAlgorithmsCommand jasyptTestAlgorithmsCommand() {
        return new JasyptTestAlgorithmsCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "generateFullJwtCommand")
    public GenerateFullJwtCommand generateFullJwtCommand() {
        return new GenerateFullJwtCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "generateJwtCommand")
    public GenerateJwtCommand generateJwtCommand() {
        return new GenerateJwtCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "generateOidcJsonWebKeystoreCommand")
    public GenerateOidcJsonWebKeystoreCommand generateOidcJsonWebKeystoreCommand() {
        return new GenerateOidcJsonWebKeystoreCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "convertPropertiesToYAMLCommand")
    public ConvertPropertiesToYAMLCommand convertPropertiesToYAMLCommand() {
        return new ConvertPropertiesToYAMLCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "addPropertiesToConfigurationCommand")
    public AddPropertiesToConfigurationCommand addPropertiesToConfigurationCommand() {
        return new AddPropertiesToConfigurationCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "exportPropertiesCommand")
    public ExportPropertiesCommand exportPropertiesCommand() {
        return new ExportPropertiesCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "findPropertiesCommand")
    public FindPropertiesCommand findPropertiesCommand() {
        return new FindPropertiesCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "generateSamlIdPMetadataCommand")
    public GenerateSamlIdPMetadataCommand generateSamlIdPMetadataCommand() {
        return new GenerateSamlIdPMetadataCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "generateYamlRegisteredServiceCommand")
    public GenerateYamlRegisteredServiceCommand generateYamlRegisteredServiceCommand() {
        return new GenerateYamlRegisteredServiceCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "validateRegisteredServiceCommand")
    public ValidateRegisteredServiceCommand validateRegisteredServiceCommand() {
        return new ValidateRegisteredServiceCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "validateEndpointCommand")
    public ValidateEndpointCommand validateEndpointCommand() {
        return new ValidateEndpointCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "validateLdapConnectionCommand")
    public ValidateLdapConnectionCommand validateLdapConnectionCommand() {
        return new ValidateLdapConnectionCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "anonymousUsernameAttributeProviderCommand")
    public AnonymousUsernameAttributeProviderCommand anonymousUsernameAttributeProviderCommand() {
        return new AnonymousUsernameAttributeProviderCommand();
    }
}
