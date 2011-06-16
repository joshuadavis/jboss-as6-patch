package org.yajul.jboss;

import org.jboss.jbossas.servermanager.Server;
import org.jboss.jbossas.servermanager.ServerManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Test starting up the patched JBoss AS server.
 * <br/>
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 6/16/11
 * Time: 12:00 AM
 */
@Test
public class TestServerStartup {
    private ServerManager serverManager =  new ServerManager();

    @BeforeClass
    public void setup()
    {
        // In Maven / surefire, both of these will be set appropriately.
        File javaHome = getDirFromEnvVar("java.home.dir","JAVA_HOME");
        File jbossHome = new File(".","target/unpacked");
        Assert.assertTrue(jbossHome.isDirectory());
        serverManager.setJavaHome(javaHome.getAbsolutePath());
        serverManager.setJbossHome(jbossHome.getAbsolutePath());
    }

    public void testDefaultStartup() throws IOException {
        final String config = "default";

        String serverName = config + "-server";
        Server defaultServer = new Server();

        defaultServer.setName(serverName);
        defaultServer.setConfig(config);

        serverManager.addServer(defaultServer);

        serverManager.startServer(serverName);

        serverManager.stopServer(serverName);
    }

    private File getDirFromEnvVar(String sysProperty,String envVar) {
        String envValue = System.getProperty(sysProperty,System.getenv(envVar));
        if (envValue == null || envValue.isEmpty())
            throw new IllegalArgumentException("Environment variable " + envVar + " was not specified!");
        File javaHome =  new File(envValue);
        if (!javaHome.isDirectory())
            throw new IllegalArgumentException("Environment variable " + envVar + ", value "
                    + envValue + " is not a directory!");
        return javaHome;
    }
}
