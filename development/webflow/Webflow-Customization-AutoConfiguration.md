# Webflow Auto Configuration

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-AutoConfiguration.html

Most CAS modules, when declared as a dependency, attempt to autoconfigure the CAS webflow to suit their needs. This practically means that the CAS adopter would no longer have to manually massage the CAS webflow configuration, and the module automatically takes care of all required changes. While this is the default behavior, it is possible that you may want to manually handle all such changes. For doing so, you will need to disable the CAS auto-configuration of the webflow.

The following settings and properties are available from the CAS configuration catalog:

- `cas.webflow.auto-configuration.enabled` (Optional; type `Boolean`; default `true`): Whether webflow auto-configuration should be enabled.
- `cas.webflow.auto-configuration.order` (Optional; type `Integer`; default `0`): The order in which the webflow is configured.

> **Note**
>
> Only attempt to modify the Spring webflow configuration files by hand when/if absolutely necessary and the change is rather minimal or decorative. Extensive modifications of the webflow, if not done carefully may severely complicate your deployment and future upgrades. If reasonable, consider contributing or suggesting the change to the project and have it be maintained directly.
