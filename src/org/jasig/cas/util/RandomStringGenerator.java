package org.jasig.cas.util;

/**
 * Interface to return a random String.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface RandomStringGenerator {

    int getMinLength();

    int getMaxLength();

    String getNewString();
}
