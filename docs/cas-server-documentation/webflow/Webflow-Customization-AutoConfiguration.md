---
layout: default
title: CAS - Webflow Customization
category: Webflow Management
---

{% include variables.html %}

# Webflow Auto Configuration

Most CAS modules, when declared as a dependency, attempt to autoconfigure the CAS webflow to suit their needs.
This practically means that the CAS adopter would no longer have to manually massage the CAS webflow configuration,
and the module automatically takes care of all required changes. While this is the default behavior, it is possible that
you may want to manually handle all such changes. For doing so, you will need to disable the CAS auto-configuration
of the webflow.

{% include_cached casproperties.html properties="cas.webflow.auto-configuration." %}

<div class="alert alert-warning">:warning: <strong>Note</strong><p>Only attempt to 
modify the Spring webflow configuration files by hand when/if absolutely necessary and the
change is rather minimal or decorative. Extensive modifications of the webflow, if not done carefully
may severely complicate your deployment and future upgrades. If reasonable, consider contributing or
suggesting the change to the project and have it be maintained directly.</p></div>
