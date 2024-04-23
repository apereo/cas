// CHECKSTYLE:OFF
package org.apereo.cas.support.inwebo.service.soap.generated;

import jakarta.xml.bind.annotation.XmlRegistry;

/**
 * The generated SOAP class.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@XmlRegistry
public class ObjectFactory {

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: soap
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link LoginSearch }
     *
     * @return the LoginSearch
     */
    public LoginSearch createLoginSearch() {
        return new LoginSearch();
    }

    /**
     * Create an instance of {@link LoginSearchResponse }
     *
     * @return the LoginSearchResponse
     */
    public LoginSearchResponse createLoginSearchResponse() {
        return new LoginSearchResponse();
    }

    /**
     * Create an instance of {@link LoginSearchResult }
     *
     * @return the LoginSearchResult
     */
    public LoginSearchResult createLoginSearchResult() {
        return new LoginSearchResult();
    }

    /**
     * Create an instance of {@link LoginQuery }
     */
    public LoginQuery createLoginQuery() {
        return new LoginQuery();
    }

    /**
     * Create an instance of {@link LoginQueryResponse }
     */
    public LoginQueryResponse createLoginQueryResponse() {
        return new LoginQueryResponse();
    }

    /**
     * Create an instance of {@link LoginQueryResult }
     */
    public LoginQueryResult createLoginQueryResult() {
        return new LoginQueryResult();
    }
}
