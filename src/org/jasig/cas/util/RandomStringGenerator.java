package org.jasig.cas.util;

/**
 * Interface to return a random String.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface RandomStringGenerator {

    /**
     * @return the minimum length as an int guaranteed by this generator.
     */
    int getMinLength();

    /**
     * @return the maximum length as an int gauranteed by this generator.
     */
    int getMaxLength();

    /**
     * @return the new random string
     */
    String getNewString();
}