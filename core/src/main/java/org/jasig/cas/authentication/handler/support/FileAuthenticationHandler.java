/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.util.PasswordTranslator;
import org.jasig.cas.util.support.PlainTextPasswordTranslator;

/**
 * Class designed to read data from a file in the format of USERNAME SEPARATOR
 * PASSWORD that will go line by line and look for the username. If it finds the
 * username it will compare the supplied password (first put through a
 * PasswordTranslator) that is compared to the password provided in the file. If
 * there is a match, the user is authenticated. Note that the default password
 * translator is a plaintext password translator and the defeault separator is
 * "::" (without quotes).
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class FileAuthenticationHandler extends
    AbstractUsernamePasswordAuthenticationHandler {

    private static final String DEFAULT_SEPARATOR = "::";

    private static final PasswordTranslator DEFAULT_PASSWORD_TRANSLATOR = new PlainTextPasswordTranslator();

    private String separator = DEFAULT_SEPARATOR;

    private PasswordTranslator passwordTranslator = DEFAULT_PASSWORD_TRANSLATOR;

    private String fileName;

    public boolean authenticateUsernamePasswordInternal(
        final UsernamePasswordCredentials credentials) {
        BufferedReader bufferedReader = null;

        if (credentials.getUserName() == null
            || credentials.getPassword() == null)
            return false;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(this
                .getClass().getResourceAsStream(this.fileName)));
            String line = bufferedReader.readLine();
            while (line != null) {
                final String[] lineFields = line.split(this.separator);
                final String userName = lineFields[0];
                final String password = lineFields[1];

                if (credentials.getUserName().equals(userName)) {
                    if (this.passwordTranslator.translate(
                        credentials.getPassword()).equals(password)) {
                        bufferedReader.close();
                        return true;
                    }
                    break;
                }
                line = bufferedReader.readLine();
            }
        }
        catch (Exception e) {
            log.error(e);
        }
        finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
            catch (IOException e) {
                log.error(e);
            }

        }
        return false;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.fileName == null || this.passwordTranslator == null
            || this.separator == null) {
            throw new IllegalStateException(
                "fileName, passwordTranslator and separator must be set on "
                    + this.getClass().getName());
        }
    }

    /**
     * @param fileName The fileName to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @param passwordTranslator The passwordTranslator to set.
     */
    public void setPasswordTranslator(PasswordTranslator passwordTranslator) {
        this.passwordTranslator = passwordTranslator;
    }

    /**
     * @param separator The separator to set.
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
