# OpenRewrite Upgrade Recipes

Source: https://apereo.github.io/cas/development/installation/OpenRewrite-Upgrade-Recipes.html

[OpenRewrite](https://docs.openrewrite.org/) is a tool and platform used by the CAS allows the project to upgrade installations in place from one version to the next. It works by making changes to the project structure representing your [CAS Overlay build](https://apereo.github.io/cas/development/installation/WAR-Overlay-Installation.html) and printing the modified files back. Modification instructions are packaged together in form of upgrade scripts called *Recipes* that are produced by the CAS project, and then referenced and discovered in the CAS overlay.

Support is enabled by including the following dependencies in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-openrewrite"
}
```

> **YAGNI**
>
> You do not need to explicitly include this component in your configuration and overlays. This is just to teach you that it exists, should you need it.

OpenRewrite recipes produced by CAS are essentially YAML files that make minimally invasive changes to your [CAS build](https://apereo.github.io/cas/development/installation/WAR-Overlay-Installation.html) allowing you to upgrade from one version to the next with minimal effort. The recipe contains **almost everything** that is required for a CAS build system to navigate from one version to other and automated tedious aspects of the upgrade such as finding the correct versions of CAS, relevant libraries and plugins as well as any possible structural changes to one’s CAS build.

Please refer to the instructions produced in your CAS overlay project to learn more about how to apply upgrade recipes. In more modern and recent versions of CAS, the instructions are typically found in the `README` file of your CAS overlay project.

> **Usage Warning!**
>
> While upgrades recipes will continue to get smarter and better over time, you should know that their technical prowess and ability can only go so far. There may be certain aspects of the upgrade that are simply not possible to automate, specially if you have made significant modifications to your CAS build. Fewer changes to your CAS build often lead to smoother, cheaper and less painful future upgrades so think twice before stepping into the rabbit hole.
