package org.apereo.cas.authentication.principal;

import javax.servlet.http.HttpServletRequest;

/**
 * The {@link ServiceFactory} is responsible for creating service objects.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 4.2
 */
public interface ServiceFactory<T extends Service> {

    /**
     * Create service object based on the parameters of the request.
     *
     * @param request the request
     * @return the service
     */
    T createService(HttpServletRequest request);

    /**
     * Create service based on an identifier.
     *
     * @param id the id
     * @return the service object
     */
    T createService(String id);

    /**
     * Create the service object based on an identifier.
     * Allows the final service object to be casted to the desired service class
     * that may not immediately inherit from {@link Service} itself.
     *
     * @param <T>   the type parameter
     * @param id    the id
     * @param clazz the clazz
     * @return the t
     */
    <T extends Service> T createService(String id, Class<? extends Service> clazz);

    /**
     * Create service based on the given parameters provided by the http request.
     * Allows the final service object to be casted to the desired service class
     * that may not immediately inherit from {@link Service} itself.
     *
     * @param <T>     the type parameter
     * @param request the request
     * @param clazz   the clazz
     * @return the t
     */
    <T extends Service> T createService(HttpServletRequest request, Class<? extends Service> clazz);
}
