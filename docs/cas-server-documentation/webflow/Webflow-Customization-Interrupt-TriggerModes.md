---
layout: default
title: CAS - Authentication Interrupt
category: Webflow Management
---

{% include variables.html %}

# Authentication Interrupts Trigger Modes

Authentication interrupts and notifications are executed in the overall flow using one of the following strategies. The
trigger strategy is defined globally via CAS settings.

{% tabs interrupttriggermodes %}

{% tab interrupttriggermodes After Authentication %}

This is the default strategy that allows the interrupt query to execute after the
primary authentication event and before the single sign-on event. This means an authenticated user has been
identified by CAS and by extension is made available to the interrupt, and interrupt has the ability to
decide whether a single sign-on session can be established for the user.

<div class="alert alert-info">:information_source: <strong>Can We SSO Into Links?</strong><p>
No. The collection of <code>links</code> are just links and are not tied in any way to the 
CAS authentication sequence, meaning they do not activate a state, transition or view in 
that sequence to trigger CAS into generating tickets, executing certain 
actions, etc. Any link in this collection is exactly that; just a link. If a 
link points to applications that are integrated with CAS, accessing those 
applications via the link will prompt the user for credentials again 
especially if single sign-on isn't already established. Remember that 
interrupt notifications typically execute after the authentication step 
and before any single sign-on session is created.</p></div>

{% endtab %}

{% tab interrupttriggermodes After Single Sign-on %}

Alternatively, the interrupt query can execute once the single sign-on session has been established.
In this mode, the authenticated user has been identified by CAS and linked to the single sign-on session. Note that
interrupt here loses the ability to decide whether a single sign-on session can be established for the user, and interrupt
responses indicating this option will have no impact, since the query and interrupt responses
happen after the creation of the SSO session.

<div class="alert alert-info">:information_source: <strong>Can We SSO Into Links?</strong><p>
Yes. In this strategy, links to external applications presented by the interrupt response
should be able to take advantage of the established single sign-on session.</p>
</div>

{% endtab %}

{% endtabs %}
     
Note that as of this writing, interrupt trigger modes are global and cannot be controlled or defined on a per-application basis.
The construction of the authentication interrupt workflow is burned into the CAS webflow at initialization time and is not
alterable at runtime depending on the application or the user.
