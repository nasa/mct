1. MCT is built using Maven, so start by downloading [maven 2.2.1](http://maven.apache.org/download.html). In addition to the normal maven setup, you may need to add or enhance your `MAVEN_OPTS` environment variable with `-Xmx1024m -XX:MaxPermSize=128m`.
2. Run `mvn clean install -Dmaven.test.skip=true -Ddistribution` from the platform-assembly directory
   1. If Maven complains about missing dependencies org.eclipse:equinox-osgi:jar:3.5.1 or org.eclipse:equinox-osgi-services:jar:3.2.0, download the JARs for the two plugins from http://archive.eclipse.org/equinox/drops/R-3.5.1-200909170800/index.php.  Then follow the instructions Maven provides for installing the JARs.
3. The platform distribution can be found in the target directory 
