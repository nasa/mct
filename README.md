Mission Control Technologies
--
The [MCT](https://sites.google.com/site/openmct/) project was developed at the NASA Ames Research Center for use in spaceflight mission operations, but is equally applicable to any other data monitoring and control application.

Getting Started
--
1. MCT is built using Maven (Java SE6), so start by downloading [maven 2.2.1](http://maven.apache.org/download.html)
2. Clone the git repository `git clone https://github.com/nasa/mct.git` into a local folder (referred to as `MCT_HOME`).
3. Run `mvn -N install` from the `MCT_HOME/superpom` directory.
4. Run `mvn clean install -Dmaven.test.skip=true -Ddistribution` from the `MCT_HOME/platform-assembly` directory.
   1. If Maven complains about missing dependencies org.eclipse:equinox-osgi:jar:3.5.1 or org.eclipse:equinox-osgi-services:jar:3.2.0, download the JARs for the two plugins from http://archive.eclipse.org/equinox/drops/R-3.5.1-200909170800/index.php.  Then follow the instructions Maven provides for installing the JARs.
5. The platform distribution archive can be found in the `MCT_HOME/platform-assembly/target` directory.
6. Extract the distribution archive, i.e. `mct-platform-1.8b4-dist.tar.gz` to the directory you wish to install MCT.
   The subdirectory `mct-platform-1.8b4` will be created from the archive (referred to as `MCT_DIST`).
7. Run `mct.sh` from the `MCT_DIST/bin` directory to start MCT.

Working on MCT
--
[Work on MCT in Eclipse](https://github.com/nasa/mct/wiki/How-to-build-and-run-MCT-in-Eclipse)

[Building a MySQL database](https://github.com/nasa/mct/wiki/Creating-a-MySQL-database-for-MCT)

[Using a Derby database](https://github.com/nasa/mct/wiki/Using-Derby-in-MCT)

[Contributing to MCT](https://github.com/nasa/mct/wiki/Contributing-to-MCT)
