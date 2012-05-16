NASA's Mission Control Technologies (MCT) project

Building MCT
============

1. MCT is built using Maven, so start by downloading [maven 2.2.1](http://maven.apache.org/download.html). In addition to the normal maven setup, you may need to add or enhance your `MAVEN_OPTS` environment variable with `-Xmx1024m -XX:MaxPermSize=128m`.
2. Run `mvn clean install -Dmaven.test.skip=true -Ddistribution` from the platform-assembly directory
3. The platform distribution can be found in the target directory 