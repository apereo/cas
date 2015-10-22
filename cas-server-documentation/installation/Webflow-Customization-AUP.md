---
layout: default
title: CAS - Web Flow Acceptable Usage Policy
---

#Acceptable Usage Policy

CAS presents the ability to allow the user to accept the usage policy before moving on to the application. The task of 
remembering the user's choice is kept in memory by default and will be lost upon container restarts and/or in clustered 
deployments. Production-level deployments of this feature would require modifications to the flow such that the retrieval 
and/or acceptance of the policy would be handled via an external storage mechanism such as LDAP or JDBC.  

##Configuration

###Enable Webflow

- In the `login-webflow.xml` file, enable the transition to `acceptableUsagePolicyCheck` by uncommenting the following entry:

{% highlight xml %}
<transition on="success" to="acceptableUsagePolicyCheck" />
{% endhighlight %}

- Enable the actual flow components by uncommenting the following entries:

{% highlight xml %}
<!-- Enable AUP flow
<action-state id="acceptableUsagePolicyCheck">
    <evaluate expression="acceptableUsagePolicyFormAction.verify(...)" />
    <transition on="success" to="sendTicketGrantingTicket" />
    <transition to="acceptableUsagePolicyView" />
</action-state>
...

-->
{% endhighlight %}

- Customize the policy by modifying `casAcceptableUsagePolicyView.jsp` located at `src/main/webapp/WEB-INF/view/jsp/default/ui`.

###Configure Storage

The task of remembering and accepting the policy is handled by `AcceptableUsagePolicyFormAction`. Adopters may extend this class to retrieve and 
persistent the user's choice via an external backend mechanism such as LDAP or JDBC.

{% highlight xml %}
<bean id="acceptableUsagePolicyFormAction"
      class="org.jasig.cas.web.flow.AcceptableUsagePolicyFormAction"/>
{% endhighlight %}
