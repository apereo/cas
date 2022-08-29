---
layout: default
title: CAS - Configuring Commandline Shell
category: Installation
---
{% include variables.html %}


# Command-line Shell

The CAS command-line shell provides the ability to query the CAS server for help on available settings/modules and
various other utility functions. 

To invoke and work with the utility, execute:

```bash
java -jar /path/to/cas-server-support-shell-$casVersion.jar
```

...where `$casVersion` needless to say is the CAS version that is deployed.

The interface that is next presented will guide you through with available parameters and methods of querying.
You will learn how to launch into the interactive shell and query the CAS engine dynamically.

<div class="alert alert-info"><strong>JCE Requirement</strong><p>Make sure you have the proper JCE bundle installed in your 
Java environment that is used by CAS, specially if you need to use specific signing/encryption algorithms and methods. 
Be sure to pick the right version of the JCE for your Java version. Java versions can be detected via the <code>java -version</code> command.</p></div>

Note that the [WAR Overlay deployment strategy](WAR-Overlay-Installation.html) should already be equipped with this 
functionality. You should not have to do anything special and extra to interact with the shell. See the relevant 
overlay documentation for more info on how to invoke and work with the shell.

## Shell Commands

The following commands are available and exposed by the CAS command-line shell.

<div>

{% assign allCommands = site.data[siteDataVersion]["shell"] %}
{% for cfg in allCommands %}
    {% assign configBlock = cfg[1] %}
    {% for config in configBlock %}
    	{% for cmdBlock in config %}
			{% assign cmd = cmdBlock[1] %}
			<h3>{{ cmd.name }}</h3>
			<p>{{ cmd.description }}</p>
			<table class="cas-datatable paginated-table">
			    <thead>
			        <tr>
			          <th>Name</th>
			          <th>Description</th>
			          <th>Default</th>
			        </tr>
			    </thead>
			    <tbody>
			        {% for param in cmd.parameters %}
			        	<tr>
			                <td>
			                    <code>{{ param.name }}</code>
			                </td>
			                <td>
			                    <code>{{ param.help }}</code>
			                </td>
			                <td>
			                    <code>{{ param.defaultValue }}</code>
			                </td>
			            </tr>
			        {% endfor %}
			    </tbody>
			</table>

    	{% endfor %}
    {% endfor %}
{% endfor %}

</div>
