/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas;

/**
 * Class that exposes the CAS version. Fetches the "Implementation-Version" manifest attribute from the jar file.
 * 
 * @author Dmitriy Kopylenko
 */
public class CasVersion {

    /**
     * Return the full Spring version string.
     * 
     * @see java.lang.Package#getImplementationVersion
     */
    public static String getVersion() {
        return CasVersion.class.getPackage().getImplementationVersion();
    }

}