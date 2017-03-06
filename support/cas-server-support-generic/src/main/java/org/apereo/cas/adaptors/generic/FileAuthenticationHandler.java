package org.apereo.cas.adaptors.generic;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.springframework.core.io.Resource;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

/**
 * Class designed to read data from a file in the format of USERNAME SEPARATOR
 * PASSWORD that will go line by line and look for the username. If it finds the
 * username it will compare the supplied password (first put through a
 * PasswordTranslator) that is compared to the password provided in the file. If
 * there is a match, the user is authenticated. Note that the default password
 * translator is a plaintext password translator and the default separator is
 * "::" (without quotes).
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.0.0
 */
public class FileAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    /** The default separator in the file. */
    public static final String DEFAULT_SEPARATOR = "::";

    /** The separator to use. */
    private final String separator;

    /** The filename to read the list of usernames from. */
    private final Resource fileName;

    public FileAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory,
                                     final Resource fileName, final String separator) {
        super(name, servicesManager, principalFactory, null);
        this.fileName = fileName;
        this.separator = separator;
    }

    @Override
    protected HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential transformedCredential, 
                                                                 final String originalPassword)
            throws GeneralSecurityException, PreventedException {
        try {
            if (this.fileName == null) {
                throw new FileNotFoundException("Filename does not exist");
            }
            final String username = transformedCredential.getUsername();
            final String passwordOnRecord = getPasswordOnRecord(username);
            if (StringUtils.isBlank(passwordOnRecord)) {
                throw new AccountNotFoundException(username + " not found in backing file.");
            }
            if (matches(originalPassword, passwordOnRecord)) {
                return createHandlerResult(transformedCredential, this.principalFactory.createPrincipal(username), null);
            }
        } catch (final IOException e) {
            throw new PreventedException("IO error reading backing file", e);
        }
        throw new FailedLoginException();
    }
    
    /**
     * Gets the password on record.
     *
     * @param username the username
     * @return the password on record
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private String getPasswordOnRecord(final String username) throws IOException {
        return Files.lines(fileName.getFile().toPath())
                .map(line -> line.split(this.separator))
                .filter(lineFields -> {
                    final String userOnRecord = lineFields[0];
                    return username.equals(userOnRecord);
                })
                .map(lineFields -> lineFields[1])
                .findFirst()
                .orElse(null);
    }
}
