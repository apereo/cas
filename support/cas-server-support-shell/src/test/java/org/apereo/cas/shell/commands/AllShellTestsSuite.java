package org.apereo.cas.shell.commands;

import org.apereo.cas.shell.commands.cipher.GenerateCryptoKeysCommandTests;
import org.apereo.cas.shell.commands.cipher.StringableCipherExecutorCommandTests;
import org.apereo.cas.shell.commands.db.GenerateDdlCommandTests;
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
    ValidateEndpointCommandTests.class,
    ValidateLdapConnectionCommandTests.class,
    StringableCipherExecutorCommandTests.class
})
@RunWith(JUnitPlatform.class)
public class AllShellTestsSuite {
}
