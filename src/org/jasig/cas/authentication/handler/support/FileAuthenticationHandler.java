/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.handler.support;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.util.PasswordTranslator;
import org.jasig.cas.util.support.PlainTextPasswordTranslator;

/**
 * Class designed to read data from a file in the format of USERNAME SEPARATOR PASSWORD that will go line by line and look for the username. If it
 * finds the username it will compare the supplied password (first put through a PasswordTranslator) that is compared to the password provided in the
 * file. If there is a match, the user is authenticated. Note that the default password translator is a plaintext password translator and the defeault
 * separator is "::" (without quotes).
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class FileAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

    private static final String DEFAULT_SEPARATOR = "::";

    private static final PasswordTranslator DEFAULT_PASSWORD_TRANSLATOR = new PlainTextPasswordTranslator();

    private String separator = DEFAULT_SEPARATOR;

    private PasswordTranslator passwordTranslator = DEFAULT_PASSWORD_TRANSLATOR;

    private String fileName;

    /**
     * @see org.jasig.cas.authentication.handler.AuthenticationHandler#authenticate(org.jasig.cas.authentication.AuthenticationRequest)
     */
    public boolean authenticateInternal(final Credentials request) throws AuthenticationException {
        final UsernamePasswordCredentials uRequest = (UsernamePasswordCredentials)request;
        
        if (uRequest.getUserName() == null || uRequest.getPassword() == null)
            return false;

        try {
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(this.fileName)));
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                final String[] lineFields = line.split(this.separator);
                final String userName = lineFields[0];
                final String password = lineFields[1];

                if (uRequest.getUserName().equals(userName)) {
                    if (this.passwordTranslator.translate(uRequest.getPassword()).equals(password)) {
                        bufferedReader.close();
                        return true;
                    }
                    break;
                }
            }

            bufferedReader.close();

        }
        catch (FileNotFoundException ffe) {
            System.out.println(ffe);
            return false;
        }
        catch (IOException ioe) {
            System.out.println(ioe);
            return false;
        }
        return false;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (this.fileName == null || this.passwordTranslator == null || this.separator == null) {
            throw new IllegalStateException("fileName, passwordTranslator and separator must be set on " + this.getClass().getName());
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
