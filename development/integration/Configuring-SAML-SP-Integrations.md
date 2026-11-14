# SAML SP Integrations

Source: https://apereo.github.io/cas/development/integration/Configuring-SAML-SP-Integrations.html

CAS provides built-in integration support for a number of SAML2 service providers. Configuring these service providers is about declaring the relevant properties in the CAS configuration as well as the configuration module below. Each integration, when configured appropriately, will register the service provider with the CAS service registry as a SAML SP and will follow a recipe (that is documented by the SP publicly) to configure attribute release policies, name ids and entity IDs. If you need to, you can review the registration record inside the CAS service registry to adjust options.

**NOTE:** In the event that special attributes and/or name ids are required for the integration, you are required to ensure all such [attributes are properly resolved](https://apereo.github.io/cas/development/integration/Attribute-Resolution.html) and are available to the CAS principal.

> **Remember**
>
> SAML2 service provider integrations listed here attempt to automate CAS configuration based on known and documented integration guidelines and recipes provided by the service provider owned by the vendor. These recipes can change and break CAS over time and needless to say, they need to be properly and thoroughly tested as the project itself does not have a subscription to each application to test for correctness. YMMV. If you find an issue with an automated integration strategy here, please **speak up**.

Support is enabled by including the following module in the WAR Overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-saml-sp-integrations"
}
```

The following SAML SP integrations, as samples, are provided by CAS:

![Service provider logo](https://user-images.githubusercontent.com/1205228/233796715-5bc5c1de-0307-4ea7-984a-0e7776a3eec1.png) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233796726-3035dbe1-2717-40b0-aab5-a2c41f57cde6.png)](https://zoom.us/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233796752-9e53b09e-2d10-4ef9-aef5-db42681932d5.png)](https://www.dropbox.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233796766-53e570b8-a233-4e65-ad71-618462591911.png)](https://samanage.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233796783-cb802464-f540-438a-9962-ec0d4d310455.png)](https://help.salesforce.com/HTViewHelpDoc?id=sso_saml.htm) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233796794-96b90d2a-92e7-494d-83e5-7875c6ee7096.png)](https://box.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233796813-51c1e6e2-fd2a-498c-ab1c-11456720237d.png)](http://servicenow.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233796838-fd211896-06cd-4378-a2b7-a46ab3407948.png)](http://www.workday.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233796858-7399f83a-059c-499f-a577-06e5ab4d1708.png)](https://help.webex.com/en-us/article/g5ey83/Configure-Single-Sign-On-for-Cisco-Webex-Site) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233796867-579269a0-9117-42c9-aa12-728bcf378ac6.png)](https://powerfaids.collegeboard.org/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233796891-6ac3488d-5ae3-4e59-a3be-ce2593026032.png)](https://msdn.microsoft.com/en-us/library/azure/dn641269.aspx) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233796907-c6e9df3b-c51e-4da4-a7b3-56ffdbd6711c.png)](https://asana.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233796919-1943f738-f192-4e23-83b8-027e2e341220.png)](https://onlinehelp.tableau.com/current/server/en-us/saml_requ.htm) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233796964-51945275-2f35-4826-a8af-1c01417cdc8e.png)](https://evernote.com/) ![Service provider logo](https://user-images.githubusercontent.com/1205228/233796972-548fdc7f-983b-4cd2-a538-7181c75e1a88.png) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233796988-85683cf4-4128-4d38-84bd-8da3adb4dac1.png)](https://docs.openathens.net/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233797015-9bc9a9e1-d3e7-4d70-8d1c-2ea59479bada.png)](http://server.arcgis.com/en/portal/latest/administer/linux/configuring-a-saml-compliant-identity-provider-with-your-portal.htm) ![Service provider logo](https://user-images.githubusercontent.com/1205228/233797023-8d3dd298-d35e-474f-9692-e3a268e83957.png) ![Service provider logo](https://user-images.githubusercontent.com/1205228/233797059-4dd4895b-870e-4b8f-ab16-7712fc52ae08.png) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233797078-f6a107e8-7efa-4265-a2c1-b1739cbfefc7.png)](https://sc.edu/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233797093-200aebac-3c48-4563-9962-8ca591b44cdd.png)](https://get.slack.help/hc/en-us/articles/205168057) [![Service provider logo](https://user-images.githubusercontent.com/1205228/233797108-625c0f96-9da1-4335-85a3-50241b5a811b.png)](https://zendesk.com/) ![Service provider logo](https://user-images.githubusercontent.com/1205228/233797125-6bf0d02d-0d32-4661-a05d-bfd26383d6f0.png) [![Service provider logo](https://user-images.githubusercontent.com/1205228/30205883-84174ebc-949f-11e7-9afc-a66c2ab19f59.png)](https://www.cherwell.com/) ![Service provider logo](https://user-images.githubusercontent.com/1205228/30205852-69921a5e-949f-11e7-8326-ba4c00fceba4.png) [![Service provider logo](https://user-images.githubusercontent.com/1205228/30205910-a560ec90-949f-11e7-8485-e3a833f8109b.png)](https://www.everbridge.com/) ![Service provider logo](https://user-images.githubusercontent.com/1205228/30221936-5e90af04-94da-11e7-8046-483fc26a1c01.png) [![Service provider logo](https://user-images.githubusercontent.com/1205228/30247067-541cef96-9620-11e7-88d7-c3749ba55ecf.png)](https://newrelic.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/30247063-218f3962-9620-11e7-9ba4-54f7112fee13.png)](https://www.egnyte.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/30271142-6dd0b58c-9704-11e7-9138-0b86d5799403.png)](https://www.yuja.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/30271318-192b48fc-9705-11e7-9c18-3be401a39e84.png)](https://www.symplicity.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/30735450-f9a12792-9f8b-11e7-941d-7ab4ac7628b9.png)](http://www.accruent.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/33747990-4d8da06e-db83-11e7-9551-f52630f7d4f0.png)](https://docs.gitlab.com/ee/administration/auth/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/33800340-3c2c072e-dcfb-11e7-9f10-7a7b2488c9b2.png)](https://www.appdynamics.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/34523704-d33a2054-f0ad-11e7-9fd7-444c30772a2a.png)](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_providers_enable-console-saml.html) [![Service provider logo](https://user-images.githubusercontent.com/1205228/36302734-ca3327ae-12c6-11e8-836f-e9a1550253d2.png)](https://www.concursolutions.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/36302795-026f57d2-12c7-11e8-931e-0f8700df1864.png)](https://www.polleverywhere.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/39860502-c72d1614-5452-11e8-956d-28a4b3a7a757.png)](https://www.blackbaud.com/) ![Service provider logo](https://user-images.githubusercontent.com/1205228/39876988-eb009f5c-5489-11e8-8b74-940f75997f41.png) [![Service provider logo](https://user-images.githubusercontent.com/1205228/39861404-b73f4bca-5455-11e8-9035-e0ecf1f8dcd0.png)](https://www.warpwire.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/39877039-1128c7ae-548a-11e8-8735-1abf90883df6.png)](https://rocket.chat/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/55328877-35e0a400-5442-11e9-8848-cda5a9efe1c5.png)](https://armssoftware.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/55275480-e5334480-52a3-11e9-982a-1fad518258c5.png)](https://www.ahpcare.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/55275543-f3359500-52a4-11e9-9407-02ba4fc21a80.png)](https://www.neogov.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/55275622-ff6e2200-52a5-11e9-833d-f48518e58d0e.png)](https://www.conexed.com/) ![Service provider logo](https://user-images.githubusercontent.com/1205228/55275721-62ac8400-52a7-11e9-89c2-121f2c494920.png) [![Service provider logo](https://user-images.githubusercontent.com/1205228/55322014-f14c0d00-542f-11e9-9355-3dd766ea56e6.png)](https://www.zimbra.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/55322216-97981280-5430-11e9-9ea3-b3682cb67ff1.png)](https://www.atlassian.com/software/confluence) [![Service provider logo](https://user-images.githubusercontent.com/1205228/55322296-cb733800-5430-11e9-93e7-0f57e1a1ae89.png)](https://www.atlassian.com/software/jira) ![Service provider logo](https://user-images.githubusercontent.com/1205228/55322626-b34fe880-5431-11e9-8574-74cc9ab9643f.png) [![Service provider logo](https://user-images.githubusercontent.com/1205228/55322944-7f28f780-5432-11e9-8dd0-e999f0b6e03e.png)](https://www.crashplan.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/55340232-1c972200-5459-11e9-87d2-53e1eccef8b8.png)](https://www.docusign.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/55340518-b65ecf00-5459-11e9-9941-3b0e80ceaf73.png)](https://www.safaribooksonline.com/) [![Service provider logo](https://user-images.githubusercontent.com/1205228/55340927-89f78280-545a-11e9-81fb-baf0413bcbfa.png)](https://www.tophat.com/) ![Service provider logo](https://user-images.githubusercontent.com/1205228/55434382-58f77a80-554c-11e9-8494-1c6a830b532c.png) [![Service provider logo](https://user-images.githubusercontent.com/1205228/55434613-d3c09580-554c-11e9-83ef-6b5805d7424e.png)](https://www.qualtrics.com/)

> **Configure Once, Run Everywhere**
>
> If you have developed a recipe for integrating with a SAML service provider, consider contributing that recipe to the project so its configuration can be automated once and for all to use. Let the change become a feature of the project, rather than something you alone have to maintain.

## Configuration

Allow CAS to register and enable a number of built-in SAML service provider integrations.

> **Remember**
>
> SAML2 service provider integrations listed here attempt to automate CAS configuration based on known and documented integration guidelines and recipes provided by the service provider owned by the vendor. These recipes can change and break CAS over time.

The settings defined for each service provider attempt to automate the creation of SAML service definition and nothing more. If you find the applicable settings lack in certain areas, it is best to fall back onto the native configuration strategy for registering SAML service providers with CAS which would depend on your service registry of choice.

The SAML2 service provider supports the following settings:

| Name | Description |
| --- | --- |
| `metadata` | Location of metadata for the service provider (i.e URL, path, etc) |
| `name` | The name of the service provider registered in the service registry. |
| `description` | The description of the service provider registered in the service registry. |
| `name-id-attribute` | Attribute to use when generating name ids for this service provider. |
| `name-id-format` | The forced NameID Format identifier (i.e. `urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress`). |
| `attributes` | Attributes to release to the service provider, which may virtually be mapped and renamed. |
| `signature-location` | Signature location to verify metadata. |
| `entity-ids` | List of entity ids allowed for this service provider. |
| `sign-responses` | Indicate whether responses should be signed. Default is `true`. |
| `sign-assertions` | Indicate whether assertions should be signed. Default is `false`. |

The only required setting that would activate the automatic configuration for a service provider is the presence and definition of metadata. All other settings are optional.

The following settings and properties are available from the CAS configuration catalog:

- `cas.saml-sp.academic-health-plans.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.adobe-cloud.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.amazon.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.app-dynamics.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.arc-g-i-s.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.arms-software.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.asana.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.benefit-focus.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.black-baud.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.box.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.bynder.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.cccco.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.cher-well.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.concur-solutions.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.confluence.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.cranium-cafe.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.crash-plan.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.docu-sign.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.dropbox.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.easy-iep.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.egnyte.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.emma.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.ever-bridge.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.evernote.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.famis.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.gartner.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.gitlab.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.give-campus.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.in-common.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.infinite-campus.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.jira.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.neo-gov.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.net-partner.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.new-relic.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.office365.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.open-athens.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.pager-duty.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.poll-everywhere.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.qualtrics.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.rocket-chat.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.sa-manage.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.safari-online.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.salesforce.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.sans-sth.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.service-now.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.slack.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.sserca.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.symplicity.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.tableau.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.top-hat.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.warp-wire.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.web-advisor.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.webex.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.workday.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.yuja.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.zendesk.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.zimbra.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.zoom.metadata` (Required; type `String`): The location of the metadata for this service provider. Can be a URL or another form of resource.
- `cas.saml-sp.academic-health-plans.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.academic-health-plans.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.academic-health-plans.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.academic-health-plans.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.academic-health-plans.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.academic-health-plans.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.academic-health-plans.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.academic-health-plans.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.academic-health-plans.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.adobe-cloud.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.adobe-cloud.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.adobe-cloud.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.adobe-cloud.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.adobe-cloud.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.adobe-cloud.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.adobe-cloud.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.adobe-cloud.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.adobe-cloud.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.amazon.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.amazon.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.amazon.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.amazon.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.amazon.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.amazon.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.amazon.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.amazon.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.amazon.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.app-dynamics.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.app-dynamics.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.app-dynamics.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.app-dynamics.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.app-dynamics.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.app-dynamics.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.app-dynamics.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.app-dynamics.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.app-dynamics.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.arc-g-i-s.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.arc-g-i-s.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.arc-g-i-s.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.arc-g-i-s.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.arc-g-i-s.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.arc-g-i-s.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.arc-g-i-s.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.arc-g-i-s.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.arc-g-i-s.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.arms-software.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.arms-software.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.arms-software.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.arms-software.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.arms-software.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.arms-software.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.arms-software.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.arms-software.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.arms-software.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.asana.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.asana.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.asana.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.asana.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.asana.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.asana.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.asana.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.asana.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.asana.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.benefit-focus.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.benefit-focus.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.benefit-focus.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.benefit-focus.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.benefit-focus.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.benefit-focus.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.benefit-focus.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.benefit-focus.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.benefit-focus.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.black-baud.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.black-baud.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.black-baud.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.black-baud.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.black-baud.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.black-baud.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.black-baud.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.black-baud.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.black-baud.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.box.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.box.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.box.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.box.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.box.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.box.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.box.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.box.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.box.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.bynder.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.bynder.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.bynder.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.bynder.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.bynder.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.bynder.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.bynder.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.bynder.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.bynder.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.cccco.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.cccco.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.cccco.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.cccco.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.cccco.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.cccco.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.cccco.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.cccco.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.cccco.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.cher-well.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.cher-well.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.cher-well.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.cher-well.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.cher-well.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.cher-well.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.cher-well.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.cher-well.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.cher-well.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.concur-solutions.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.concur-solutions.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.concur-solutions.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.concur-solutions.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.concur-solutions.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.concur-solutions.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.concur-solutions.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.concur-solutions.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.concur-solutions.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.confluence.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.confluence.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.confluence.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.confluence.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.confluence.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.confluence.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.confluence.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.confluence.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.confluence.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.cranium-cafe.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.cranium-cafe.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.cranium-cafe.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.cranium-cafe.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.cranium-cafe.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.cranium-cafe.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.cranium-cafe.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.cranium-cafe.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.cranium-cafe.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.crash-plan.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.crash-plan.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.crash-plan.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.crash-plan.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.crash-plan.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.crash-plan.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.crash-plan.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.crash-plan.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.crash-plan.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.docu-sign.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.docu-sign.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.docu-sign.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.docu-sign.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.docu-sign.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.docu-sign.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.docu-sign.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.docu-sign.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.docu-sign.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.dropbox.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.dropbox.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.dropbox.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.dropbox.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.dropbox.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.dropbox.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.dropbox.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.dropbox.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.dropbox.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.easy-iep.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.easy-iep.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.easy-iep.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.easy-iep.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.easy-iep.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.easy-iep.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.easy-iep.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.easy-iep.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.easy-iep.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.egnyte.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.egnyte.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.egnyte.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.egnyte.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.egnyte.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.egnyte.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.egnyte.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.egnyte.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.egnyte.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.emma.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.emma.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.emma.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.emma.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.emma.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.emma.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.emma.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.emma.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.emma.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.ever-bridge.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.ever-bridge.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.ever-bridge.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.ever-bridge.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.ever-bridge.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.ever-bridge.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.ever-bridge.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.ever-bridge.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.ever-bridge.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.evernote.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.evernote.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.evernote.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.evernote.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.evernote.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.evernote.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.evernote.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.evernote.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.evernote.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.famis.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.famis.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.famis.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.famis.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.famis.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.famis.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.famis.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.famis.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.famis.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.gartner.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.gartner.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.gartner.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.gartner.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.gartner.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.gartner.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.gartner.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.gartner.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.gartner.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.gitlab.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.gitlab.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.gitlab.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.gitlab.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.gitlab.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.gitlab.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.gitlab.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.gitlab.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.gitlab.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.give-campus.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.give-campus.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.give-campus.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.give-campus.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.give-campus.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.give-campus.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.give-campus.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.give-campus.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.give-campus.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.in-common.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.in-common.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.in-common.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.in-common.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.in-common.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.in-common.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.in-common.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.in-common.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.in-common.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.infinite-campus.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.infinite-campus.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.infinite-campus.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.infinite-campus.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.infinite-campus.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.infinite-campus.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.infinite-campus.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.infinite-campus.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.infinite-campus.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.jira.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.jira.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.jira.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.jira.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.jira.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.jira.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.jira.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.jira.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.jira.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.neo-gov.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.neo-gov.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.neo-gov.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.neo-gov.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.neo-gov.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.neo-gov.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.neo-gov.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.neo-gov.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.neo-gov.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.net-partner.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.net-partner.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.net-partner.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.net-partner.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.net-partner.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.net-partner.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.net-partner.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.net-partner.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.net-partner.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.new-relic.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.new-relic.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.new-relic.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.new-relic.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.new-relic.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.new-relic.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.new-relic.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.new-relic.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.new-relic.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.office365.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.office365.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.office365.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.office365.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.office365.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.office365.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.office365.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.office365.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.office365.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.open-athens.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.open-athens.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.open-athens.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.open-athens.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.open-athens.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.open-athens.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.open-athens.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.open-athens.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.open-athens.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.pager-duty.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.pager-duty.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.pager-duty.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.pager-duty.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.pager-duty.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.pager-duty.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.pager-duty.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.pager-duty.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.pager-duty.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.poll-everywhere.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.poll-everywhere.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.poll-everywhere.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.poll-everywhere.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.poll-everywhere.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.poll-everywhere.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.poll-everywhere.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.poll-everywhere.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.poll-everywhere.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.qualtrics.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.qualtrics.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.qualtrics.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.qualtrics.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.qualtrics.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.qualtrics.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.qualtrics.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.qualtrics.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.qualtrics.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.rocket-chat.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.rocket-chat.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.rocket-chat.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.rocket-chat.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.rocket-chat.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.rocket-chat.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.rocket-chat.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.rocket-chat.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.rocket-chat.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.sa-manage.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.sa-manage.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.sa-manage.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.sa-manage.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.sa-manage.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.sa-manage.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.sa-manage.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.sa-manage.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.sa-manage.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.safari-online.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.safari-online.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.safari-online.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.safari-online.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.safari-online.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.safari-online.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.safari-online.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.safari-online.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.safari-online.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.salesforce.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.salesforce.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.salesforce.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.salesforce.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.salesforce.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.salesforce.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.salesforce.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.salesforce.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.salesforce.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.sans-sth.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.sans-sth.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.sans-sth.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.sans-sth.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.sans-sth.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.sans-sth.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.sans-sth.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.sans-sth.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.sans-sth.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.service-now.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.service-now.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.service-now.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.service-now.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.service-now.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.service-now.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.service-now.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.service-now.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.service-now.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.slack.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.slack.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.slack.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.slack.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.slack.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.slack.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.slack.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.slack.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.slack.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.sserca.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.sserca.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.sserca.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.sserca.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.sserca.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.sserca.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.sserca.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.sserca.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.sserca.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.symplicity.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.symplicity.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.symplicity.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.symplicity.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.symplicity.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.symplicity.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.symplicity.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.symplicity.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.symplicity.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.tableau.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.tableau.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.tableau.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.tableau.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.tableau.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.tableau.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.tableau.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.tableau.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.tableau.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.top-hat.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.top-hat.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.top-hat.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.top-hat.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.top-hat.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.top-hat.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.top-hat.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.top-hat.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.top-hat.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.warp-wire.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.warp-wire.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.warp-wire.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.warp-wire.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.warp-wire.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.warp-wire.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.warp-wire.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.warp-wire.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.warp-wire.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.web-advisor.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.web-advisor.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.web-advisor.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.web-advisor.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.web-advisor.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.web-advisor.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.web-advisor.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.web-advisor.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.web-advisor.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.webex.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.webex.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.webex.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.webex.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.webex.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.webex.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.webex.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.webex.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.webex.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.workday.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.workday.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.workday.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.workday.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.workday.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.workday.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.workday.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.workday.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.workday.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.yuja.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.yuja.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.yuja.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.yuja.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.yuja.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.yuja.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.yuja.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.yuja.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.yuja.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.zendesk.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.zendesk.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.zendesk.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.zendesk.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.zendesk.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.zendesk.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.zendesk.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.zendesk.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.zendesk.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.zimbra.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.zimbra.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.zimbra.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.zimbra.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.zimbra.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.zimbra.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.zimbra.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.zimbra.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.zimbra.signature-location` (Optional; type `String`): Signature location used to verify metadata.
- `cas.saml-sp.zoom.attributes` (Optional; type `List<String>`): Set up the attribute release policy for this service. Allow attributes that are to be released to this SP. Attributes should be separated by commas and can be virtually mapped and renamed.
- `cas.saml-sp.zoom.description` (Optional; type `String`; default ` SAML SP Integration`): Description of this service provider as it's stored in the registry.
- `cas.saml-sp.zoom.entity-ids` (Optional; type `List<String>`): List of entityIds allowed for this service provider. Multiple ids can be specified in the event that the metadata is an aggregate.
- `cas.saml-sp.zoom.name` (Optional; type `String`): Name of this service provider.
- `cas.saml-sp.zoom.name-id-attribute` (Optional; type `String`): Attribute to use when generating nameids for this SP.
- `cas.saml-sp.zoom.name-id-format` (Optional; type `String`): The forced nameId format to use when generating a response.
- `cas.saml-sp.zoom.sign-assertions` (Optional; type `TriStateBoolean`; default `false`): Indicate whether assertions should be signed. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.saml-sp.zoom.sign-responses` (Optional; type `Boolean`; default `true`): Indicate whether responses should be signed.
- `cas.saml-sp.zoom.signature-location` (Optional; type `String`): Signature location used to verify metadata.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

**Note**: For InCommon and other metadata aggregates, multiple entity ids can be specified to filter the InCommon metadata. EntityIds can be regular expression patterns and are mapped to CAS’ `serviceId` field in the registry. The signature location MUST BE the public key used to sign the metadata.
