# Hazelcast Ticket Registry - Azure Auto Discovery

Source: https://apereo.github.io/cas/development/ticketing/Hazelcast-Ticket-Registry-AutoDiscovery-Azure.html

Hazelcast support in CAS may handle auto-discovery automatically via Microsoft Azure. The discovery strategy will provide all Hazelcast instances by returning VMs within your Azure resource group that are tagged with a specified value. You will need to setup Microsoft Entra ID Service Principal credentials for your Azure Subscription for this plugin to work. With every Hazelcast Virtual Machine you deploy in your resource group, you need to ensure that each VM is tagged with the value of `clusterId` defined in the CAS Hazelcast configuration. The only requirement is that every VM can access each other either by private or public IP address.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-hazelcast-discovery-azure"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.hazelcast.cluster.discovery.azure.client-id` (Required; type `String`): The Azure Active Directory Service Principal client ID.
- `cas.ticket.registry.hazelcast.cluster.discovery.azure.client-secret` (Required; type `String`): The Azure Active Directory Service Principal client secret.
- `cas.ticket.registry.hazelcast.cluster.discovery.azure.cluster-id` (Required; type `String`): The name of the tag on the hazelcast vm resources. With every Hazelcast Virtual Machine you deploy in your resource group, you need to ensure that each VM is tagged with the value of cluster-id defined in your Hazelcast configuration. The only requirement is that every VM can access each other either by private or public IP address.
- `cas.ticket.registry.hazelcast.cluster.discovery.azure.group-name` (Required; type `String`): The Azure resource group name of the cluster. You can find this in the Azure portal or CLI.
- `cas.ticket.registry.hazelcast.cluster.discovery.azure.subscription-id` (Required; type `String`): The Azure subscription ID.
- `cas.ticket.registry.hazelcast.cluster.discovery.azure.tenant-id` (Required; type `String`): The Azure Active Directory tenant ID.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.
