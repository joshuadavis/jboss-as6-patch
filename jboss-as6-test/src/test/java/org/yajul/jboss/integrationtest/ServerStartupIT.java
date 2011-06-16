package org.yajul.jboss.integrationtest;

import org.jboss.jbossas.servermanager.Server;
import org.jboss.jbossas.servermanager.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ServerStartupIT {
    private static final Logger log = LoggerFactory.getLogger(ServerStartupIT.class);
    private ServerManager serverManager =  new ServerManager();

    @BeforeClass
    public void setup()
    {
        File javaHomeDir = requireEnvVarDir("JAVA_HOME");
        serverManager.setJavaHome(javaHomeDir.getAbsolutePath());
        File jbossHomeDir = requireEnvVarDir("JBOSS_HOME");
        log.info("Setting JBoss Home to " + javaHomeDir.getAbsolutePath());
        serverManager.setJbossHome(jbossHomeDir.getAbsolutePath());
    }

    public void testDefaultStartup() throws IOException {
        final String config = "default";

        String serverName = config + "-server";

        Server defaultServer = new Server();

        defaultServer.setName(serverName);
        defaultServer.setConfig(config);

        serverManager.addServer(defaultServer);

        log.info("Starting...");
        serverManager.startServer(serverName);

        log.info("Stopping...");
        serverManager.stopServer(serverName);
    }

    private File requireEnvVarDir(String envVarName)
    {
        String javaHome = requireEnvVar(envVarName);
        File javaHomeDir = new File(javaHome);
        if (!(javaHomeDir.exists() && javaHomeDir.isDirectory()))
            throw new RuntimeException(envVarName + " '" + javaHome + "' is not an existing directory!");
        return javaHomeDir;
    }

    private String requireEnvVar(String envVarName)
    {
        String value = System.getenv(envVarName);
        if (value == null || value.isEmpty())
            throw new RuntimeException(envVarName + " not specified!");
        return value;
    }
}
