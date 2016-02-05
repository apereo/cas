---
layout: default
title: CAS - Web Flow Acceptable Usage Policy
---

# Acceptable Usage Policy

CAS presents the ability to allow the user to accept the usage policy before moving on to the application. The task of
remembering the user's choice is kept in memory by default and will be lost upon container restarts and/or in clustered
deployments. Production-level deployments of this feature would require modifications to the flow such that the retrieval
and/or acceptance of the policy would be handled via an external storage mechanism such as LDAP or JDBC.  

## Configuration

Support is enabled by including the following dependency in the Maven WAR overlay:

{% highlight xml %}
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-webapp-actions-aup-webflow</artifactId>
  <version>${cas.version}</version>
</dependency>
{% endhighlight %}

### Enable Webflow

- In the `login-webflow.xml` file, enable the transition to `acceptableUsagePolicyCheck`
by uncommenting the following entry:

{% highlight xml %}
<transition on="success" to="acceptableUsagePolicyCheck" />
{% endhighlight %}

- Customize the policy by modifying `casAcceptableUsagePolicyView.jsp`.
