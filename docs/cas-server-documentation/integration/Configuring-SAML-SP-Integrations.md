---
layout: default
title: CAS - SAML SP Integrations
category: Integration
---

{% include variables.html %}

# SAML SP Integrations

CAS provides built-in integration support for a number of SAML2 service providers. Configuring these service providers
is about declaring the relevant properties in the CAS configuration as well as the configuration module below. Each integration,
when configured appropriately, will register the service provider with the CAS service registry as a SAML SP and will follow
a recipe (that is documented by the SP publicly) to configure attribute release policies, name ids and entity IDs. If you need to,
you can review the registration record inside the CAS service registry to adjust options.

**NOTE:** In the event that special attributes and/or name ids are required for the integration, you are required
to ensure all such [attributes are properly resolved](Attribute-Resolution.html) and are available to the CAS principal.

<div class="alert alert-warning">:warning: <strong>Remember</strong><p>SAML2 service provider integrations listed here attempt to automate CAS configuration based on known and documented integration guidelines and recipes provided by the service provider owned by the vendor. These recipes can change and break CAS over time and needless to say, they need to be properly and thoroughly tested as the project itself does not have a subscription to each application to test for correctness. YMMV. If you find an issue with an automated integration strategy here, please <strong>speak up</strong>.</p></div>

Support is enabled by including the following module in the WAR Overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-saml-sp-integrations" %}

The following SAML SP integrations, as samples, are provided by CAS:

<div class="img-cloud">
<a href="javascript:void(0)">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796715-5bc5c1de-0307-4ea7-984a-0e7776a3eec1.png" height="48" width="110"></a> 

<a href="https://zoom.us">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796726-3035dbe1-2717-40b0-aab5-a2c41f57cde6.png" height="30" width="110"></a> 

<a href="https://www.dropbox.com/guide/admin/security/configure-single-sign-on">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796752-9e53b09e-2d10-4ef9-aef5-db42681932d5.png" height="48" width="140"></a> 

<a href="https://samanage.com">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796766-53e570b8-a233-4e65-ad71-618462591911.png" height="48" width="140"></a> 

<a href="https://help.salesforce.com/HTViewHelpDoc?id=sso_saml.htm">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796783-cb802464-f540-438a-9962-ec0d4d310455.png" height="60" width="100"></a>

<a href="https://box.com">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796794-96b90d2a-92e7-494d-83e5-7875c6ee7096.png" height="48" width="90"></a> 

<a href="http://servicenow.com">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796813-51c1e6e2-fd2a-498c-ab1c-11456720237d.png" height="20" width="100"></a> 

<a href="http://www.workday.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796838-fd211896-06cd-4378-a2b7-a46ab3407948.png" height="48" width="100"></a>

<a href="https://help.webex.com/en-us/article/g5ey83/Configure-Single-Sign-On-for-Cisco-Webex-Site">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796858-7399f83a-059c-499f-a577-06e5ab4d1708.png" height="48" width="100"></a>

<a href="https://powerfaids.collegeboard.org">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796867-579269a0-9117-42c9-aa12-728bcf378ac6.png" height="48" width="140"></a> 

<a href="https://msdn.microsoft.com/en-us/library/azure/dn641269.aspx">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796891-6ac3488d-5ae3-4e59-a3be-ce2593026032.png" height="48" width="110"></a> 

<a href="https://asana.com">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796907-c6e9df3b-c51e-4da4-a7b3-56ffdbd6711c.png" height="48" width="100"></a> 

<a href="https://onlinehelp.tableau.com/current/server/en-us/saml_requ.htm">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796919-1943f738-f192-4e23-83b8-027e2e341220.png" height="48" width="140"></a> 

<a href="https://evernote.com">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796964-51945275-2f35-4826-a8af-1c01417cdc8e.png" height="48" width="110"></a> 

<a href="http://www.ellucian.com/Software/Colleague-WebAdvisor/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796972-548fdc7f-983b-4cd2-a538-7181c75e1a88.png" height="48" width="140"></a>

<a href="https://docs.openathens.net">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233796988-85683cf4-4128-4d38-84bd-8da3adb4dac1.png" height="48" width="140"></a>

<a href="http://server.arcgis.com/en/portal/latest/administer/linux/configuring-a-saml-compliant-identity-provider-with-your-portal.htm">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233797015-9bc9a9e1-d3e7-4d70-8d1c-2ea59479bada.png" height="48" width="130"></a>

<a href="https://helpx.adobe.com/enterprise/kb/configure_shibboleth_idp_for_use_with_Adobe_SSO.html">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233797023-8d3dd298-d35e-474f-9692-e3a268e83957.png" height="48" width="90"></a>

<a href="javascript:void(0)">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233797059-4dd4895b-870e-4b8f-ab16-7712fc52ae08.png" height="48" width="120"></a>

<a href="https://sc.edu">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233797078-f6a107e8-7efa-4265-a2c1-b1739cbfefc7.png" height="48" width="120"></a>

<a href="https://get.slack.help/hc/en-us/articles/205168057">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233797093-200aebac-3c48-4563-9962-8ca591b44cdd.png" height="48" width="110"></a>

<a href="https://support.zendesk.com/hc/en-us/articles/203663676-Using-SAML-for-single-sign-on-Professional-and-Enterprise-">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233797108-625c0f96-9da1-4335-85a3-50241b5a811b.png" height="48" width="120"></a>

<a href="javascript:void(0)">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/233797125-6bf0d02d-0d32-4661-a05d-bfd26383d6f0.png" height="48" width="90"></a>

<a href="https://www.cherwell.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/30205883-84174ebc-949f-11e7-9afc-a66c2ab19f59.png" height="48" width="110"></a>

<a href="javascript:void(0)">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/30205852-69921a5e-949f-11e7-8326-ba4c00fceba4.png" height="48" width="120"></a>

<a href="https://www.everbridge.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/30205910-a560ec90-949f-11e7-8485-e3a833f8109b.png" height="48" width="120"></a>

<a href="javascript:void(0)">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/30221936-5e90af04-94da-11e7-8046-483fc26a1c01.png" height="48" width="170"></a>

<a href="https://newrelic.com">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/30247067-541cef96-9620-11e7-88d7-c3749ba55ecf.png" height="48" width="90"></a>

<a href="https://www.egnyte.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/30247063-218f3962-9620-11e7-9ba4-54f7112fee13.png" height="48" width="120"></a>

<a href="https://www.yuja.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/30271142-6dd0b58c-9704-11e7-9138-0b86d5799403.png" height="48" width="100"></a>

<a href="https://www.symplicity.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/30271318-192b48fc-9705-11e7-9c18-3be401a39e84.png" height="48" width="200"></a>

<a href="http://www.accruent.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/30735450-f9a12792-9f8b-11e7-941d-7ab4ac7628b9.png" height="48" width="120"></a>

<a href="https://docs.gitlab.com/ee/administration/auth/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/33747990-4d8da06e-db83-11e7-9551-f52630f7d4f0.png" height="38" width="120"></a>

<a href="https://www.appdynamics.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/33800340-3c2c072e-dcfb-11e7-9f10-7a7b2488c9b2.png" height="58" width="120"></a>

<a href="https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_providers_enable-console-saml.html">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/34523704-d33a2054-f0ad-11e7-9fd7-444c30772a2a.png" height="58" width="120"></a>

<a href="https://www.concursolutions.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/36302734-ca3327ae-12c6-11e8-836f-e9a1550253d2.png" height="58" width="170"></a>

<a href="https://www.polleverywhere.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/36302795-026f57d2-12c7-11e8-931e-0f8700df1864.png" height="58" width="180"></a>

<a href="https://www.blackbaud.com">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/39860502-c72d1614-5452-11e8-956d-28a4b3a7a757.png" height="58" width="150"></a>

<a href="javascript:void(0)">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/39876988-eb009f5c-5489-11e8-8b74-940f75997f41.png" height="58" width="130"></a>

<a href="https://www.warpwire.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/39861404-b73f4bca-5455-11e8-9035-e0ecf1f8dcd0.png" height="58" width="150"></a>

<a href="https://rocket.chat">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/39877039-1128c7ae-548a-11e8-8735-1abf90883df6.png" height="58" width="150"></a>

<a href="https://armssoftware.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55328877-35e0a400-5442-11e9-8848-cda5a9efe1c5.png" height="50" width="100"></a>

<a href="https://www.ahpcare.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55275480-e5334480-52a3-11e9-982a-1fad518258c5.png" height="50" width="190"></a>

<a href="https://www.neogov.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55275543-f3359500-52a4-11e9-9407-02ba4fc21a80.png" height="38" width="120"></a>

<a href="https://www.conexed.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55275622-ff6e2200-52a5-11e9-833d-f48518e58d0e.png" height="58" width="170"></a>

<a href="javascript:void(0)">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55275721-62ac8400-52a7-11e9-89c2-121f2c494920.png" height="98" width="90"></a>

<a href="https://www.zimbra.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55322014-f14c0d00-542f-11e9-9355-3dd766ea56e6.png" height="68" width="160"></a>

<a href="https://www.atlassian.com/software/confluence">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55322216-97981280-5430-11e9-9ea3-b3682cb67ff1.png" height="68" width="130"></a>

<a href="https://www.atlassian.com/software/jira">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55322296-cb733800-5430-11e9-93e7-0f57e1a1ae89.png" height="78" width="90"></a>

<a href="javascript:void(0)">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55322626-b34fe880-5431-11e9-8574-74cc9ab9643f.png" height="78" width="120"></a>

<a href="https://www.crashplan.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55322944-7f28f780-5432-11e9-8dd0-e999f0b6e03e.png" height="78" width="120"></a>

<a href="https://www.docusign.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55340232-1c972200-5459-11e9-87d2-53e1eccef8b8.png" height="78" width="150"></a>

<a href="https://www.safaribooksonline.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55340518-b65ecf00-5459-11e9-9941-3b0e80ceaf73.png" height="78" width="110"></a>

<a href="https://www.tophat.com/">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55340927-89f78280-545a-11e9-81fb-baf0413bcbfa.png" height="48" width="140"></a>
 
<a href="javascript:void(0)">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55434382-58f77a80-554c-11e9-8494-1c6a830b532c.png" height="48" width="140"></a>

<a href="https://www.qualtrics.com">
<img alt="Service provider logo" src="https://user-images.githubusercontent.com/1205228/55434613-d3c09580-554c-11e9-83ef-6b5805d7424e.png" height="48" width="140"></a>

</div>

<div class="alert alert-info">:information_source: <strong>Configure Once, Run Everywhere</strong>
<p>If you have developed a recipe for integrating
with a SAML service provider, consider contributing that recipe to the project so its configuration
can be automated once and for all to use. Let the change become a feature of the project, 
rather than something you alone have to maintain.</p></div>

## Configuration

Allow CAS to register and enable a number of built-in SAML service provider integrations.

<div class="alert alert-warning">:warning: <strong>Remember</strong><p>SAML2 service provider integrations listed 
here attempt to automate CAS configuration based on known and documented integration 
guidelines and recipes provided by the service provider owned by the vendor. These 
recipes can change and break CAS over time.</p></div>

The settings defined for each service provider attempt to automate the creation of
SAML service definition and nothing more. If you find the applicable settings lack in certain areas, it
is best to fall back onto the native configuration strategy for registering
SAML service providers with CAS which would depend on your service registry of choice.

The SAML2 service provider supports the following settings:

| Name                 | Description                                                                                          |
|----------------------|------------------------------------------------------------------------------------------------------|
| `metadata`           | Location of metadata for the service provider (i.e URL, path, etc)                                   |
| `name`               | The name of the service provider registered in the service registry.                                 |
| `description`        | The description of the service provider registered in the service registry.                          |
| `name-id-attribute`  | Attribute to use when generating name ids for this service provider.                                 |
| `name-id-format`     | The forced NameID Format identifier (i.e. `urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress`). |
| `attributes`         | Attributes to release to the service provider, which may virtually be mapped and renamed.            |
| `signature-location` | Signature location to verify metadata.                                                               |
| `entity-ids`         | List of entity ids allowed for this service provider.                                                |
| `sign-responses`     | Indicate whether responses should be signed. Default is `true`.                                      |
| `sign-assertions`    | Indicate whether assertions should be signed. Default is `false`.                                    |

The only required setting that would activate the automatic configuration for a
service provider is the presence and definition of metadata. All other settings are optional.
     
{% include_cached casproperties.html properties="cas.saml-sp" %}

**Note**: For InCommon and other metadata aggregates, multiple entity ids can be specified to
filter the InCommon metadata. EntityIds can be regular expression patterns and are mapped to
CAS' `serviceId` field in the registry. The signature location MUST BE the public key used to sign the metadata.
