package org.apereo.cas.authentication.handler;


/**
 * Strategy pattern component for transforming principal names in the authentication pipeline.
 *
 * @author Howard Gilbert
 * @since 3.3.6
 */
@FunctionalInterface
public interface PrincipalNameTransformer {

    /**
     * Transform the string typed into the login form into a tentative Principal Name to be
     * validated by a specific type of Authentication Handler.
     *
     * <p>The Principal Name eventually assigned by the PrincipalResolver may
     * be unqualified ("AENewman"). However, validation of the Principal name against a
     * particular backend source represented by a particular Authentication Handler may
     * require transformation to a temporary fully qualified format such as
     * AENewman@MAD.DCCOMICS.COM or MAD\AENewman. After validation, this form of the
     * Principal name is discarded in favor of the choice made by the Resolver.
     *
     * @param formUserId The raw userid typed into the login form
     * @return the string that the Authentication Handler should lookup in the backend system
     */
    String transform(String formUserId);
}

