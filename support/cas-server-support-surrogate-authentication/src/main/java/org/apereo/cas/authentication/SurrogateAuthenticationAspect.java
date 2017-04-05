package org.apereo.cas.authentication;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * This is {@link SurrogateAuthenticationAspect}.
 *
 * @author Jonathan Johnson
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Aspect
public class SurrogateAuthenticationAspect {
    @Pointcut(value = "execution(* *AuthenticationHandler.postAuthenticate(..))", argNames = "credential,result")
    public Object handleSurrogate(final JoinPoint jp,
                                  final Credential credential,
                                  final HandlerResult result) throws Throwable {
        System.out.println("dddddddddddd");
        return result;
    }
}
