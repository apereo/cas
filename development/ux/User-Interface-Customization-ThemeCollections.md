# Theme Collections - User Interface Customization

Source: https://apereo.github.io/cas/development/ux/User-Interface-Customization-ThemeCollections.html

CAS provides a module that presents a number of ready-made themes. The intention for each themes to account for common and provide for common use cases when it comes to user interface modifications and samples, and attempt to automate much of the configuration.

Support is enabled by including the following module in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-themes-collection"
}
```

The following themes are provided by this module and can be assigned to service definitions:

| Theme | Description |
| --- | --- |
| `example` | A reference example theme that combines customized CSS, JavaScript and views |
| `twbs` | A basic theme utilizing [Bootstrap](http://getbootstrap.com/) for CSS and JavaScript |

The collection of themes above can also serve as reference examples of how to define a theme with custom CSS, JavaScript and associated views and fragments.

## Bootstrap Themes

If you intend to build your own themes based on Bootstrap, you will need to include the following module in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-themes-bootstrap"
}
```
