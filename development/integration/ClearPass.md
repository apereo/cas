---
layout: default
title: CAS - ClearPass
---

# ClearPass: Credential Caching and Replay
To enable single sign-on into some legacy application it may be necessary to provide them with the actual cleartext password. While such approach inevitably increases security risk, at times this may be a necessary evil in order to integrate applications with CAS.


<div class="alert alert-warning"><strong>Usage Warning!</strong><p>ClearPass is turned off by default. No applications will be able to obtain the user credentials unless ClearPass is explicitly turned on by the below configuration.</p></div>

## Architecture
