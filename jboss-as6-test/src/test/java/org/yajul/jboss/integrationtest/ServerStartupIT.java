package org.yajul.jboss.integrationtest;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.asset.ClassLoaderAsset;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.yajul.arquillian.NamedClassLoaderAsset;
import org.yajul.jboss.ejb.HelloWorld;
import org.yajul.jboss.ejb.HelloWorldBean;

import javax.ejb.EJB;
import java.io.File;
import java.io.InputStream;

/**
 * Test starting up the patched JBoss AS server.
 * <br/>
 * Created by IntelliJ IDEA.
 * User: josh
 * Date: 6/16/11
 * Time: 12:00 AM
 */
@RunWith(Arquillian.class)
public class ServerStartupIT
{
    private static final Logger log = LoggerFactory.getLogger(ServerStartupIT.class);

    @EJB
    private HelloWorld helloWorld;

    @Deployment(testable = true)
    public static JavaArchive createDeployment()
    {
        return ShrinkWrap.create(JavaArchive.class, "server-startup.jar")
                .add(new NamedClassLoaderAsset("jboss-logging.xml", "test-jboss-logging.xml"))
                .addClasses(HelloWorld.class, HelloWorldBean.class);
    }

    @Test
    public void sayHello()
    {
        log.info("calling sayHello()...");
        helloWorld.sayHello();
    }

    @Test
    @RunAsClient
    public void checkLogFiles()
    {
        log.info("Looking for log files...");
        File jbossHome = requireEnvVarDir("JBOSS_HOME");
        File serverLog = new File(jbossHome,"server/default/log/server.log");
        Assert.assertTrue(serverLog.exists() && serverLog.isFile());
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
