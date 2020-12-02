// CHECKSTYLE:OFF
package org.apereo.cas.support.inwebo.service.soap;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * The generated SOAP class.
 *
 * @author Jerome LELEU
 * @since 6.3.0
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: soap
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link LoginSearch }
     * 
     */
    public LoginSearch createLoginSearch() {
        return new LoginSearch();
    }

    /**
     * Create an instance of {@link LoginSearchResponse }
     * 
     */
    public LoginSearchResponse createLoginSearchResponse() {
        return new LoginSearchResponse();
    }

    /**
     * Create an instance of {@link LoginSearchResult }
     * 
     */
    public LoginSearchResult createLoginSearchResult() {
        return new LoginSearchResult();
    }
}
