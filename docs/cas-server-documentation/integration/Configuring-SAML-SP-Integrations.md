---
layout: default
title: CAS - SAML SP Integrations
---

# SAML SP Integrations

CAS provides built-in integration support for a number of SAML2 service providers. Configuring these service providers
is simply about declaring the relevant properties in the CAS configuration as well as the configuration module below. Each integration,
when configured appropriately, will register the service provider with the CAS service registry as a SAML SP and will follow
a recipe (that is documented by the SP publicly) to configure attribute release policies, name ids and entity IDs. If you need to,
you can review the registration record inside the CAS service registry to adjust options.

**NOTE:** In the event that special attributes and/or name ids are required for the integration, you are required
to ensure all such [attributes are properly resolved](Attribute-Resolution.html) and are available to the CAS principal.

Support is enabled by including the following module in the Overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-support-saml-sp-integrations</artifactId>
     <version>${cas.version}</version>
</dependency>
```

The following SAML SP integrations are provided by CAS:

<div class="img-cloud">
<a href="http://www.incommon.org/federation/metadata.html"><img src="https://cloud.githubusercontent.com/assets/1205228/22052578/47d6f570-dd60-11e6-8f03-02cb99d6106d.gif" height="48" width="140"></a> <a href="https://support.zoom.us/hc/en-us/articles/201363003-Getting-Started-with-SSO"><img src="https://cloud.githubusercontent.com/assets/1205228/22052568/475f36d4-dd60-11e6-84f4-a88eb33f3f0a.jpg" height="30" width="130"></a> <a href="https://www.dropbox.com/guide/admin/security/configure-single-sign-on"><img src="https://cloud.githubusercontent.com/assets/1205228/22052575/47ba8930-dd60-11e6-8cb1-9334066d5f0f.png" height="48" width="140"></a> <a href="https://blog.samanage.com/company/saml-single-sign-on-support-samanage/"><img src="https://cloud.githubusercontent.com/assets/1205228/22052581/47e7bcfc-dd60-11e6-85e9-c09926736e5e.png" height="48" width="140"></a> <a href="https://help.salesforce.com/HTViewHelpDoc?id=sso_saml.htm"><img src="https://cloud.githubusercontent.com/assets/1205228/22052579/47da9d74-dd60-11e6-8eac-d66b67e2ebf7.png" height="60" width="140"></a>
<a href="https://community.box.com/t5/For-Admins/Single-Sign-On-SSO-with-Box-For-Administrators/ta-p/1263"><img src="https://cloud.githubusercontent.com/assets/1205228/22052572/47ace302-dd60-11e6-9842-4eda5a9ab5cf.png" height="48" width="140"></a> <a href="http://wiki.servicenow.com/index.php?title=SAML_2.0_Web_Browser_SSO_Profile"><img src="https://cloud.githubusercontent.com/assets/1205228/22052583/4805e2c2-dd60-11e6-8150-80aaa4bbab0e.png" height="20" width="140"></a> <a href="http://www.workday.com/"><img src="https://cloud.githubusercontent.com/assets/1205228/22052587/4816e04a-dd60-11e6-9ceb-6ccec1290e19.png" height="48" width="140"></a> <a href="http://www.testshib.org"><img src="https://cloud.githubusercontent.com/assets/1205228/22052582/4805064a-dd60-11e6-87aa-746deacca597.jpg" height="48" width="140"></a> <a href="https://help.webex.com/docs/DOC-1067"><img src="https://cloud.githubusercontent.com/assets/1205228/22052574/47af8206-dd60-11e6-95d0-5827e0d88d73.jpg" height="48" width="140"></a>
<a href="https://www.collegeboard.org/powerfaids/net-partner"><img src="https://cloud.githubusercontent.com/assets/1205228/22052570/47abea6a-dd60-11e6-85a0-387c3c2ce8e7.png" height="48" width="140"></a> <a href="https://msdn.microsoft.com/en-us/library/azure/dn641269.aspx"><img src="https://cloud.githubusercontent.com/assets/1205228/22053102/d64da904-dd63-11e6-8a68-977526634b9d.png" height="48" width="140"></a> <a href="https://asana.com/guide/help/premium/authentication#gl-saml"><img src="https://cloud.githubusercontent.com/assets/1205228/22052569/478b8c16-dd60-11e6-82f4-e292243ff076.png" height="48" width="140"></a> <a href="https://onlinehelp.tableau.com/current/server/en-us/saml_requ.htm"><img src="https://cloud.githubusercontent.com/assets/1205228/22052586/480cffc6-dd60-11e6-939c-1ceb34f3186d.png" height="48" width="140"></a> <a href="https://help.evernote.com/hc/en-us/articles/209005217-How-to-configure-SSO-for-your-business"><img src="https://cloud.githubusercontent.com/assets/1205228/22052577/47d6b6e6-dd60-11e6-810e-dd875bf25d17.png" height="48" width="140"></a> <a href="http://www.ellucian.com/Software/Colleague-WebAdvisor/"><img src="https://cloud.githubusercontent.com/assets/1205228/23185912/5c3f2f50-f89a-11e6-8450-6da44a1a9d9d.png" height="48" width="140"></a>
</div>

To see the relevant list of CAS properties, please [review this guide](../installation/Configuration-Properties.html#saml-sps).

<div class="alert alert-info"><strong>Configure Once, Run Everywhere</strong><p>If you have developed a recipe for integrating
with a SAML service provider, consider contributing that recipe to the project so its configuration
can be automated once and for all to use. Let the change become a feature of the project, rather than something you alone have to maintain.</p></div>

## Google Apps

The Google Apps SAML integration is also provided by CAS natively [based on this guide](Google-Apps-Integration.html).
