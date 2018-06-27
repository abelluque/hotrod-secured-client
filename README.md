Spring-Boot hotrod-secured-client: Example Using Secured Remote Access to Cache via HotRod
==============================================
Author: Abel Luque
Level: Intermediate
Technologies: Infinispan, Hot Rod
Summary: The `hotrod-secured-clien` quickstart demonstrates how to use Infinispan cache remotely using the Hot Rod protocol and secured authentication/authorization.
Target Product: JDG
Product Versions: JDG 7.2


What is it?
-----------

Hot Rod is a binary TCP client-server protocol used in Red Hat JBoss Data Grid. The Hot Rod protocol facilitates faster client and server interactions in comparison to other text based protocols(Memcached, REST) and allows clients to make decisions about load balancing, failover and data location operations.

The `hotrod-secured` quickstart demonstrates how to connect securely to remote JBoss Data Grid (JDG) to store, retrieve, and remove data from cache using the Hot Rod protocol. 

System requirements
-------------------

All you need to build this project is Java 8.0 (Java SDK 1.8) or higher, Maven 3.0 or higher.
The application this project produces is designed to be run on JBoss Data Grid 7.2


Hot Rod authentication
----------------------
The Hot Rod protocol supports authentication since version 2.0 by leveraging the SASL mechanisms. The supported SASL mechanisms (usually shortened as mechs) are:

* PLAIN - This is the most insecure mech, since credentials are sent over the wire in plain-text format, however it is the simplest to get to work. In combination with encryption (i.e. SSL) it can be used safely
* DIGEST-MD5 - This mech hashes the credentials before sending them over the wire, so it is more secure than PLAIN
* GSSAPI - This mech uses Kerberos tickets, and therefore requires the presence of a properly configured Kerberos Domain Controller (such as Microsoft Active Directory)
* EXTERNAL - This mech obtains credentials from the underlying transport (i.e. from a X.509 client certificate) and therefore requires encryption using client-certificates to be enabled.

Configure JDG
-------------

1. Obtain JDG server distribution on Red Hat's Customer Portal at https://access.redhat.com/jbossnetwork/restricted/listSoftware.html

2. This Quickstart uses new HotRod Security features to configure the cache. To use it, it's necessary to change JDG configuration file (`JDG_HOME/standalone/configuration/standalone.xml`) to contain the following definitions:
* Security Realm configuration
  Security Realms are used by the server to provide authentication and authorization information for both the management and application interfaces

        <management>
            ...
            <security-realm name="ApplicationRealm">
                <authentication>
                    <properties path="application-users.properties" relative-to="jboss.server.config.dir"/>
                </authentication>
                <authorization>
                    <properties path="application-roles.properties" relative-to="jboss.server.config.dir"/>
                </authorization>
            </security-realm>
            ...
        </management>
   
* Enpoint subsystem definition:
  The following configuration enables authentication against ApplicationRealm, using the DIGEST-MD5 SASL mechanism: 

        <subsystem xmlns="urn:infinispan:server:endpoint:8.1">
            <hotrod-connector socket-binding="hotrod" cache-container="secured">
                <topology-state-transfer lazy-retrieval="false" lock-timeout="1000" replication-timeout="5000"/>
                <authentication security-realm="ApplicationRealm">
                    <sasl server-name="football" mechanisms="DIGEST-MD5" qop="auth">
                        <policy>
                            <no-anonymous value="true"/>
                        </policy>
                        <property name="com.sun.security.sasl.digest.utf8">true</property>
                    </sasl>
                </authentication>
            </hotrod-connector>
            ...
        </subsystem>
          
  Notice! The server-name attribute: it is the name that the server declares to incoming clients and therefore the client configuration must match.

* Infinispan subsystem definition:
  Server supports authorization with cache configuration defined below

        <subsystem xmlns="urn:infinispan:server:core:8.5">
            <cache-container name="local" default-cache="workshop">
                <security>
                    <authorization>
                        <identity-role-mapper/>
                        <role name="master" permissions="ALL"/>
                        <role name="guest" permissions="READ"/>
                    </authorization>
                </security>
                <local-cache name="workshop">
                    <security>
                        <authorization roles="master guest"/>
                    </security>
                </local-cache>
                ...
            </cache-container>
        </subsystem>

Start JDG
---------

1. Open a command line and navigate to the root of the JDG directory.
2. The following shows the command line to start the server with the web profile:

        For Linux:   $JDG_HOME/bin/standalone.sh
        For Windows: %JDG_HOME%\bin\standalone.bat

Add new users to ApplicationRealm
---------------------------------

        $JDG_HOME/bin/add-user.sh -a -u 'master'      -p 'redhat1!' -ro master
        $JDG_HOME/bin/add-user.sh -a -u 'guest'     -p 'redhat1!' -ro guest


