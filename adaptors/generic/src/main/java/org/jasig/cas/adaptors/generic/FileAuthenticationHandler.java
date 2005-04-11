/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.generic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.jasig.cas.authentication.handler.PlainTextPasswordEncoder;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.beans.factory.InitializingBean;

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
public final class FileAuthenticationHandler extends
    AbstractUsernamePasswordAuthenticationHandler implements InitializingBean {

    /** The default separator in the file. */
    private static final String DEFAULT_SEPARATOR = "::";

    /** The default PasswordTranslator (PlainText). */
    private static final PasswordEncoder DEFAULT_PASSWORD_TRANSLATOR = new PlainTextPasswordEncoder();

    /** The separator to use. */
    private String separator = DEFAULT_SEPARATOR;

    /** The PasswordTranslator to use. */
    private PasswordEncoder passwordTranslator = DEFAULT_PASSWORD_TRANSLATOR;

    /** The filename to read the list of usernames from. */
    private String fileName;
    
    /** Log instance. */
    private final Log log = LogFactory.getLog(getClass());

    public boolean authenticateUsernamePasswordInternal(
        final UsernamePasswordCredentials credentials) {
        BufferedReader bufferedReader = null;

        if (credentials.getUserName() == null
            || credentials.getPassword() == null) {
            return false;
        }

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(this
                .getClass().getResourceAsStream(this.fileName)));
            String line = bufferedReader.readLine();
            while (line != null) {
                final String[] lineFields = line.split(this.separator);
                final String userName = lineFields[0];
                final String password = lineFields[1];

                if (credentials.getUserName().equals(userName)) {
                    if (this.passwordTranslator.encode(
                        credentials.getPassword()).equals(password)) {
                        bufferedReader.close();
                        return true;
                    }
                    break;
                }
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            log.error(e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
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
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    /**
     * @param passwordTranslator The passwordTranslator to set.
     */
    public void setPasswordTranslator(final PasswordEncoder passwordTranslator) {
        this.passwordTranslator = passwordTranslator;
    }

    /**
     * @param separator The separator to set.
     */
    public void setSeparator(final String separator) {
        this.separator = separator;
    }
}
