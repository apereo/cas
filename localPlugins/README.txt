Custom Plugins for CAS 3.0
--------------------------

This directory is provided for you to create your own CAS plugins.
Follow the steps below to compile and install these plugins.

1. write your plugins, placing the source under src/ and any external
   dependencies that aren't already part of CAS 3.0 under lib/

2. run Ant to compile your plugins and build a .jar file:

   % ant jar

   This will build a file called localPlugins.jar and place it in
   target/

3. edit ../webapp/WEB-INF/deployerConfigContext.xml to configure
   CAS to use your new plugin.

4. run Ant to build a new CAS war:

   % ant war

   This copies your plugin to ../webapp/WEB-INF/lib, and packages up 
   everything under CAS 3.0's webapp/ directory into target/cas.war.

Additional Ant build targets:

   % ant

   The default target will compile your .java files into .class files
   under build/

   % ant clean

   This will delete the build/ directory (created by Step 1 above) and
   webapp/WEB-INF/lib/localPlugins.jar, and it will replace target/cas.war
   with the version that was backed-up in Step 3.

   % ant doc

   This will build any JavaDoc that you have written into your plugins.

--------------------------
Author: Drew Mazurek
Version: $Revision$ $Date$
Since: 3.0
