package org.yajul.jboss.ejb;

import org.jboss.logging.Logger;

import javax.ejb.Local;
import javax.ejb.Stateless;

/**
 * Very simple SLSB for testing.
 * <br>
 * User: josh
 * Date: 6/16/11
 * Time: 10:31 AM
 */
@Local(HelloWorld.class)
@Stateless
public class HelloWorldBean implements HelloWorld
{
    private static final Logger log = Logger.getLogger(HelloWorldBean.class,"EJB");

    public void sayHello()
    {
        log.info("sayHello() : Hello world!");
    }
}
