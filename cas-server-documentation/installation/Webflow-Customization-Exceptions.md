---
layout: default
title: CAS - Web Flow Customization
---

# Webflow Errors Customization

By default CAS is configured to recognize and handle 10 exceptions that are subclasses of `GeneralSecurityException` or
`PreventedException` in the web flow during authentication transactions processing. Handling is defined by a component called
`AuthenticationExceptionHandler` that has a pre-configured map of these default exceptions which is able to deduce from it a next webflow
event when CAS server catches and passes one of these exceptions to it. The event returned determines the next transition to a state and in most cases that state is the login view. The trick here is that each of those exceptions have the specific message bundle mapping in `messages.properties` like so:

````properties
# Authentication failure messages
authenticationFailure.AccountDisabledException=This account has been disabled.
authenticationFailure.AccountLockedException=This account has been locked.
authenticationFailure.CredentialExpiredException=Your password has expired.
authenticationFailure.InvalidLoginLocationException=You cannot login from this workstation.
authenticationFailure.InvalidLoginTimeException=Your account is forbidden to login at this time.
authenticationFailure.AccountNotFoundException=Invalid credentials.
authenticationFailure.FailedLoginException=Invalid credentials.
authenticationFailure.UnauthorizedServiceForPrincipalException=Service access denied due to missing privileges.
authenticationFailure.UNKNOWN=Invalid credentials.
```

so that a specific message could be presented to end users on the login form. Any un-recognized or un-mapped exceptions of type
`GeneralSecurityException`, `PreventedException` or `AbstractTicketException` presented to `AuthenticationExceptionHandler` results in  the `UNKNOWN` mapping with a generic `Invalid credentials.` message.

Suppose that there is a need for a custom authentication handler that throws a custom `GeneralSecurityException` with a very specific
message in this case presented to UI (suppose that the exception type thrown is `com.mycompany.MyAuthenticationException`). To achieve
this customization, one would need to do the following:

 1. Define the fully qualified name of the exception class in `application.properties` (comma-separated class names in case of multiple
  mappings):

 ```properties
 cas.custom.authentication.exceptions=com.mycompany.MyAuthenticationException
 ```
 2. Define custom message mapping in `messages.properties` (or the variant for the locale in question):

 ```properties
 authenticationFailure.MyAuthenticationException=Authentication has failed, but it did it my way!!!
 ```
