package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider;
import org.apereo.cas.shell.commands.cipher.GenerateCryptoKeysCommand;
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
import org.apereo.cas.shell.commands.services.GenerateYamlRegisteredServiceCommand;
import org.apereo.cas.shell.commands.services.ValidateRegisteredServiceCommand;
import org.apereo.cas.shell.commands.util.ValidateEndpointCommand;
import org.apereo.cas.shell.commands.util.ValidateLdapConnectionCommand;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;

/**
 * This is {@link CasCommandLineShellAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core, module = "shell")
@AutoConfiguration
public class CasCommandLineShellAutoConfiguration {

    @Bean
    public PromptProvider shellPromptProvider() {
        return () -> new AttributedString("cas>", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }

    @Bean
    public GenerateCryptoKeysCommand generateCryptoKeysCommand() {
        return new GenerateCryptoKeysCommand();
    }

    @Bean
    public StringableCipherExecutorCommand stringableCipherExecutorCommand() {
        return new StringableCipherExecutorCommand();
    }

    @Bean
    public GenerateDdlCommand generateDdlCommand() {
        return new GenerateDdlCommand();
    }

    @Bean
    public JasyptDecryptPropertyCommand jasyptDecryptPropertyCommand() {
        return new JasyptDecryptPropertyCommand();
    }

    @Bean
    public JasyptEncryptPropertyCommand jasyptEncryptPropertyCommand() {
        return new JasyptEncryptPropertyCommand();
    }

    @Bean
    public JasyptListAlgorithmsCommand jasyptListAlgorithmsCommand() {
        return new JasyptListAlgorithmsCommand();
    }

    @Bean
    public JasyptListProvidersCommand jasyptListProvidersCommand() {
        return new JasyptListProvidersCommand();
    }

    @Bean
    public JasyptTestAlgorithmsCommand jasyptTestAlgorithmsCommand() {
        return new JasyptTestAlgorithmsCommand();
    }

    @Bean
    public GenerateFullJwtCommand generateFullJwtCommand() {
        return new GenerateFullJwtCommand();
    }

    @Bean
    public GenerateJwtCommand generateJwtCommand() {
        return new GenerateJwtCommand();
    }

    @Bean
    public GenerateOidcJsonWebKeystoreCommand generateOidcJsonWebKeystoreCommand() {
        return new GenerateOidcJsonWebKeystoreCommand();
    }

    @Bean
    public ConvertPropertiesToYAMLCommand convertPropertiesToYAMLCommand() {
        return new ConvertPropertiesToYAMLCommand();
    }

    @Bean
    public AddPropertiesToConfigurationCommand addPropertiesToConfigurationCommand() {
        return new AddPropertiesToConfigurationCommand();
    }

    @Bean
    public ExportPropertiesCommand exportPropertiesCommand() {
        return new ExportPropertiesCommand();
    }

    @Bean
    public FindPropertiesCommand findPropertiesCommand() {
        return new FindPropertiesCommand();
    }

    @Bean
    public GenerateSamlIdPMetadataCommand generateSamlIdPMetadataCommand() {
        return new GenerateSamlIdPMetadataCommand();
    }

    @Bean
    public AnonymousRegisteredServiceUsernameAttributeProvider anonymousRegisteredServiceUsernameAttributeProvider() {
        return new AnonymousRegisteredServiceUsernameAttributeProvider();
    }

    @Bean
    public GenerateYamlRegisteredServiceCommand generateYamlRegisteredServiceCommand() {
        return new GenerateYamlRegisteredServiceCommand();
    }

    @Bean
    public ValidateRegisteredServiceCommand validateRegisteredServiceCommand() {
        return new ValidateRegisteredServiceCommand();
    }

    @Bean
    public ValidateEndpointCommand validateEndpointCommand() {
        return new ValidateEndpointCommand();
    }

    @Bean
    public ValidateLdapConnectionCommand validateLdapConnectionCommand() {
        return new ValidateLdapConnectionCommand();
    }
}
