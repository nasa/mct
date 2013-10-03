Mission Control Technologies
--
The [MCT](https://sites.google.com/site/openmct/) project was developed at the NASA Ames Research Center for use in spaceflight mission operations, but is equally applicable to any other data monitoring and control application.

Getting Started
--
1. MCT is built using Maven (Java SE6), so start by downloading [maven 2.2.1](http://maven.apache.org/download.html)
2. Run mvn -N install from the superpom directory
3. Run `mvn clean install -Dmaven.test.skip=true -Ddistribution` from the platform-assembly directory
   1. If Maven complains about missing dependencies org.eclipse:equinox-osgi:jar:3.5.1 or org.eclipse:equinox-osgi-services:jar:3.2.0, download the JARs for the two plugins from http://archive.eclipse.org/equinox/drops/R-3.5.1-200909170800/index.php.  Then follow the instructions Maven provides for installing the JARs.
4. The platform distribution can be found in the target directory 

Working on MCT
--
[Work on MCT in Eclipse](https://github.com/nasa/mct/wiki/How-to-build-and-run-MCT-in-Eclipse)

[Building a MySQL database](https://github.com/nasa/mct/wiki/Creating-a-MySQL-database-for-MCT)

[Using a Derby database](https://github.com/nasa/mct/wiki/Using-Derby-in-MCT)

[Using a Derby database](https://github.com/nasa/mct/wiki/Using-Derby-in-MCT)

[Contributing to MCT](https://github.com/nasa/mct/wiki/Contributing-to-MCT)
