---
layout: default
title: CAS - Duo Security Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Duo Security User Registration

If users are unregistered with Duo Security or allowed through via a direct bypass, 
CAS will query Duo Security for the user account apriori to learn
whether user is registered or configured for direct bypass. If the account is configured for direct bypass or the
user account is not registered yet the new-user enrollment policy allows the user to skip registration, CAS will bypass
Duo Security altogether and shall not challenge the user and will also **NOT** report back a multifactor-enabled 
authentication context back to the application.

<div class="alert alert-warning">:warning: <strong>YMMV</strong><p>In recent conversations with Duo Security, it 
turns out that the API behavior has changed (for security reasons) where it may no longer accurately 
report back account status. This means even if the above conditions hold true, CAS may continue to route 
the user to Duo Security having received an eligibility status from the API. Duo Security is reportedly 
working on a fix to restore the API behavior in a more secure way. In the meanwhile, YMMV.</p></div>

If you would rather not rely on Duo Security's built-in registration flow and have your
own registration application that allows users to onboard and enroll with Duo Security, you can instruct CAS
to redirect to your enrollment application, if the user's account status is determined to require enrollment.
This typically means that you must turn on user-account-status checking in CAS so that it can verify
the user's account status directly with Duo Security. You must also make sure your integration type, as selected
in Duo Security's admin dashboard, is chosen to be the correct type that would allow CAS to execute such
requests and of course, the user in question must not have been onboard, enrolled or created previously anywhere
in Duo Security. 
                   
The redirect URL to your enrollment application may include a special `principal` parameter that contains
the user's identity as JWT. Cipher operations and settings must be enabled in CAS settings for Duo Security's
registration before this parameter can be built and added to the final URL.

{% include_cached casproperties.html properties="cas.authn.mfa.duo[].registration" %}
