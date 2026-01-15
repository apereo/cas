package org.apereo.cas.support.oauth.services;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link RegisteredServiceOAuthTokenExchangePolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface RegisteredServiceOAuthTokenExchangePolicy extends Serializable {

    /**
     * Is token exchange allowed?
     *
     * @param registeredService the registered service
     * @param resources         the resources
     * @param audience          the audience
     * @param requestedType     the requested token type
     * @return true /false
     */
    boolean isTokenExchangeAllowed(RegisteredService registeredService, Set<String> resources,
                                   Set<String> audience, String requestedType);

    /**
     * Can subject token act.
     *
     * @param subject        the subject
     * @param actor          the actor
     * @param actorTokenType the actor token type
     * @return true/false
     */
    boolean canSubjectTokenActAs(Authentication subject, Authentication actor, String actorTokenType);
}
