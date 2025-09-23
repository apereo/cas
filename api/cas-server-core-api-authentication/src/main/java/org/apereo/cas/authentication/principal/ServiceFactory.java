package org.apereo.cas.authentication.principal;

import org.springframework.core.Ordered;

import jakarta.servlet.http.HttpServletRequest;

/**
 * The {@link ServiceFactory} is responsible for creating service objects.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 4.2
 */
public interface ServiceFactory<T extends Service> extends Ordered {

    /**
     * Flag as a request attribute to indicate if the factory
     * should collect service attributes such as request headers, cookies, etc.
     */
    String COLLECT_SERVICE_ATTRIBUTES = "collectServiceAttributes";

    /**
     * Create service object based on the parameters of the request.
     *
     * @param request the request
     * @return the service
     */
    T createService(HttpServletRequest request);

    /**
     * Create service t.
     *
     * @param id      the id
     * @param request the request
     * @return the t
     */
    T createService(String id, HttpServletRequest request);

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
    <T extends Service> T createService(String id, Class<T> clazz);

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
    <T extends Service> T createService(HttpServletRequest request, Class<T> clazz);

}
