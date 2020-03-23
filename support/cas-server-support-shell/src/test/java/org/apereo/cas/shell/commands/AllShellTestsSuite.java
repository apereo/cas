package org.apereo.cas.shell.commands;

import org.apereo.cas.shell.commands.cipher.GenerateCryptoKeysCommandTests;
import org.apereo.cas.shell.commands.cipher.StringableCipherExecutorCommandTests;
import org.apereo.cas.shell.commands.db.GenerateDdlCommandTests;
import org.apereo.cas.shell.commands.jasypt.JasyptDecryptPropertyCommandTests;
import org.apereo.cas.shell.commands.jasypt.JasyptEncryptPropertyCommandTests;
import org.apereo.cas.shell.commands.jasypt.JasyptListAlgorithmsCommandTests;
import org.apereo.cas.shell.commands.jasypt.JasyptListProvidersCommandTests;
import org.apereo.cas.shell.commands.jasypt.JasyptTestAlgorithmsCommandTests;
import org.apereo.cas.shell.commands.properties.AddPropertiesToConfigurationCommandTests;
import org.apereo.cas.shell.commands.properties.FindPropertiesCommandTests;
import org.apereo.cas.shell.commands.properties.ListUndocumentedPropertiesCommandTests;
import org.apereo.cas.shell.commands.saml.GenerateSamlIdPMetadataCommandTests;
import org.apereo.cas.shell.commands.services.AnonymousUsernameAttributeProviderCommandTests;
import org.apereo.cas.shell.commands.services.GenerateYamlRegisteredServiceCommandTests;
import org.apereo.cas.shell.commands.services.ValidateRegisteredServiceCommandTests;
import org.apereo.cas.shell.commands.util.GenerateJwtCommandTests;
import org.apereo.cas.shell.commands.util.ValidateEndpointCommandTests;
import org.apereo.cas.shell.commands.util.ValidateLdapConnectionCommandTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllShellTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    GenerateCryptoKeysCommandTests.class,
    GenerateDdlCommandTests.class,
    GenerateJwtCommandTests.class,
    AnonymousUsernameAttributeProviderCommandTests.class,
    GenerateSamlIdPMetadataCommandTests.class,
    ValidateRegisteredServiceCommandTests.class,
    GenerateSamlIdPMetadataCommandTests.class,
    JasyptTestAlgorithmsCommandTests.class,
    ValidateEndpointCommandTests.class,
    ValidateLdapConnectionCommandTests.class,
    JasyptListProvidersCommandTests.class,
    JasyptListAlgorithmsCommandTests.class,
    StringableCipherExecutorCommandTests.class,
    FindPropertiesCommandTests.class,
    JasyptDecryptPropertyCommandTests.class,
    JasyptEncryptPropertyCommandTests.class,
    GenerateYamlRegisteredServiceCommandTests.class,
    ListUndocumentedPropertiesCommandTests.class,
    AddPropertiesToConfigurationCommandTests.class
})
@RunWith(JUnitPlatform.class)
public class AllShellTestsSuite {
}
