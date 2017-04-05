package org.apereo.cas.authentication;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
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
    @Around(value = "execution(public org.apereo.cas.authentication.HandlerResult "
            + "org.apereo.cas.authentication.AuthenticationHandler.authenticate(..)) " +
            "&& args(credential)")
    public Object handleSurrogate(final ProceedingJoinPoint jp, final Credential credential) throws Throwable {
        System.out.println(jp.getSignature().toLongString());
        return jp.proceed();
    }
}
