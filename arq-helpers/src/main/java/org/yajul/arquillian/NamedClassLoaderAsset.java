package org.yajul.arquillian;

import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.jboss.shrinkwrap.impl.base.asset.ClassLoaderAsset;

import java.io.InputStream;

/**
 * A named asset that comes from the current class loader.
 * <br>
 * User: josh
 * Date: 6/16/11
 * Time: 2:02 PM
 */
public class NamedClassLoaderAsset implements NamedAsset
{
    private final String name;
    private ClassLoaderAsset delegate;

    public NamedClassLoaderAsset(String name, String resourceName)
    {
        this.name = name;
        this.delegate = new ClassLoaderAsset(resourceName,
                                     Thread.currentThread().getContextClassLoader());
    }

    public String getName()
    {
        return name;
    }

    public InputStream openStream()
    {

        return delegate.openStream();
    }
}
