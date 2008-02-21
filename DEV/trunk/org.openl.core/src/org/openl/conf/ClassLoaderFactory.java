/*
 * Created on Jul 19, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.conf;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openl.OpenL;
import org.openl.util.ASelector;
import org.openl.util.FileTreeIterator;
import org.openl.util.ISelector;
import org.openl.util.Log;
import org.openl.util.RuntimeExceptionWrapper;

/**
 * @author snshor
 * 
 */
public class ClassLoaderFactory
{

    // public ClassLoaderFactory()
    // {
    // // userClassLoaders.put("org.openl.core", getOpenlCoreLoader());
    // }

    public static ClassLoader getOpenlCoreLoader(ClassLoader ucl)
    {
	try
	{
	    Class<?> c = ucl.loadClass(OpenL.class.getName());
	    if (c != null)
		return ucl;

	} catch (Exception e)
	{
	}

	return OpenL.class.getClassLoader();
    }

    public static synchronized HashMap<Key, ClassLoader> reset()
    {
	HashMap<Key, ClassLoader> oldLoaders = userClassLoaders;

	userClassLoaders = new HashMap<Key, ClassLoader>();

	return oldLoaders;
    }

    static final class Key
    {
	String name;
	String classpath;
	ClassLoader parent;
	IUserContext cxt;

	Key(String name, String classpath, ClassLoader parent, IUserContext cxt)
	{
	    this.name = name;
	    this.classpath = classpath;
	    this.parent = parent;
	    this.cxt = cxt;
	}

	public boolean equals(Object obj)
	{
	    if (!(obj instanceof Key))
		return false;
	    Key k = (Key) obj;

	    return new EqualsBuilder()
	    // .append(name, k.name)
		    .append(classpath, k.classpath).append(cxt, k.cxt).append(
			    parent, k.parent).isEquals();
	}

	public int hashCode()
	{
	    return new HashCodeBuilder()
	    // .append(name)
		    .append(parent).append(cxt).append(classpath).toHashCode();
	}

    }

    public static synchronized ClassLoader createUserClassloader(String name,
	    String classpath, ClassLoader parent, IUserContext ucxt)
	    throws Exception
    {

	Log.debug("name=" + name + " cp=" + classpath + " " + ucxt + " cl="
		+ parent);

	Key key = new Key(name, classpath, parent, ucxt);
	ClassLoader loader = userClassLoaders.get(key);

	Log.debug(loader == null ? "New" : "Old");

	if (loader == null)
	{
	    loader = createClassLoader(classpath, parent, ucxt);
	    // TODO fix it
	    userClassLoaders.put(key, loader);
	}

	return loader;
    }

    static HashMap<Key, ClassLoader> userClassLoaders = new HashMap<Key, ClassLoader>();

    static public ClassLoader createClassLoader(String classpath,
	    ClassLoader parent, IUserContext ucxt) throws Exception
    {

	return createClassLoader(splitClassPath(classpath), parent, ucxt);
    }

    static public ClassLoader createClassLoader(String[] classpath,
	    ClassLoader parent, IUserContext ucxt) throws Exception
    {
	List<URL> urls = new ArrayList<URL>();
	for (int i = 0; i < classpath.length; i++)
	{

	    if (classpath[i].endsWith("*"))
		makeWildcardPath(makeFile(ucxt.getUserHome(), classpath[i]
			.substring(0, classpath[i].length() - 1)), urls);
	    else
	    {

		File f = makeFile(ucxt.getUserHome(), classpath[i]);

		if (!f.exists())
		{
		    throw new IOException("File " + f.getPath()
			    + " does not exist");
		}

		urls.add(makeFile(ucxt.getUserHome(), classpath[i]).toURL());
	    }

	    // System.out.println(urls[i].toExternalForm());
	}

	URL[] uurl = urls.toArray(new URL[urls.size()]);
	return new URLClassLoader(uurl, parent);
    }

    /**
     * @param string
     * @param string2
     * @param v
     */
    public static void makeWildcardPath(File root, List<URL> urls)
    {

	ISelector<File> sel = new ASelector<File>()
	{
	    public boolean select(File f)
	    {
		String apath = f.getAbsolutePath();
		boolean res = apath.endsWith(".jar") || apath.endsWith(".zip");
		return res;
	    }

	};

	Iterator<File> iter = new FileTreeIterator(root, 0).select(sel);

	for (; iter.hasNext();)
	{
	    File f = iter.next();
	    try
	    {
		urls.add(f.toURL());
	    } catch (MalformedURLException e)
	    {
		throw RuntimeExceptionWrapper.wrap(e);
	    }
	}

    }

    static File makeFile(String root, String name) throws Exception
    {
	File f = new File(name);

	if (f.isAbsolute() || name.startsWith("/"))
	    return f.getCanonicalFile();

	return new File(root, name).getCanonicalFile();

    }

    static protected String[] splitClassPath(String classpath)
    {
	StringTokenizer st = new StringTokenizer(classpath, File.pathSeparator);

	String[] res = new String[st.countTokens()];
	for (int i = 0; i < res.length; i++)
	{
	    res[i] = st.nextToken();
	}
	return res;
    }

    // static class CurrentFactory extends ThreadLocal
    // {
    // }
    //
    // static CurrentFactory _currentFactory = new CurrentFactory();
    //
    // public static ClassLoaderFactory getCurrentFactory()
    // {
    // return (ClassLoaderFactory) _currentFactory.get();
    // }
    //
    // public static void setCurrentFactory(ClassLoaderFactory m)
    // {
    // _currentFactory.set(m);
    // }

}
