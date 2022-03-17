---
layout: default
title: CAS - Adaptive Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Adaptive Authentication

Adaptive authentication in CAS allows you to accept or reject authentication requests based on certain characteristics
of the client browser and/or device. When configured, you are provided with options to block authentication requests
from certain locations submitted by certain browser agents. For instance, you may consider authentication requests submitted
from `London, UK` to be considered suspicious, or you may want to block requests that are submitted from Internet Explorer, etc.

Adaptive authentication can also be configured to trigger multifactor based on specific 
days and times. For example, you may wish to trigger multifactor on select days or 
if the current hour is after 11pm or before 6am. Each rule block may be assigned 
to an mfa provider where successful matching of rules allows for the multifactor trigger to execute.

## Configuration

{% include_cached casproperties.html properties="cas.authn.adaptive.policy" %}

To enable adaptive authentication, you will need to allow CAS to geo-locate authentication requests.
To learn more, please [see this guide](../authentication/GeoTracking-Authentication-Requests.html)

## IP Intelligence

CAS provides you with the capability to examine the client IP address and decide 
whether access should be granted. This may be useful to detect bot, proxy or VPN 
traffic and protect your deployment from fraud, automated attacks, crawlers, etc.

To learn more, please [see this guide](../mfa/Adaptive-Authentication-IP-Intelligence.html).
