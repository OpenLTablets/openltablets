/*
 * Created on May 30, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.conf.ant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Stack;

import org.openl.OpenConfigurationException;
import org.openl.OpenL;
import org.openl.binding.impl.Binder;
import org.openl.conf.IConfigurableResourceContext;
import org.openl.conf.IOpenLBuilder;
import org.openl.conf.IOpenLConfiguration;
import org.openl.conf.IUserContext;
import org.openl.impl.DefaultCompileContext;
import org.openl.syntax.impl.Parser;
import org.openl.util.Log;
import org.openl.util.RuntimeExceptionWrapper;
import org.openl.vm.SimpleVM;

/**
 * @author snshor
 *
 */
public class AntOpenLBuilder implements IOpenLBuilder {

    static class UserContextStack extends ThreadLocal<Stack<IUserContext>> {

        /**
         *
         */

        @Override
        protected Stack<IUserContext> initialValue() {
            return new Stack<IUserContext>();
        }

        public IUserContext pop() {
            return stack().pop();
        }

        public void push(IUserContext ucxt) {
            stack().push(ucxt);
        }

        protected Stack<IUserContext> stack() {
            return get();
        }

        public IUserContext top() {
            return (IUserContext) stack().peek();
        }

    }

    static public UserContextStack userCxt = new UserContextStack();

    IConfigurableResourceContext configurableResourceContext;

    IUserContext ucxt;

    public static String getAntProjectConfigurationVariable(String openl) {
        return openl + ".configuration";
    }

    public static String getAntTarget(String openl) {
        return "build." + openl;
    }

    public AntOpenLBuilder() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.conf.IOpenLBuilder#build(java.lang.String)
     */
    public OpenL build(String openl) throws OpenConfigurationException {
        OpenL op = new OpenL();
        boolean changedClassLoader = false;
        ClassLoader oldClassLoader = null;

        try {
            userCxt.push(ucxt);

            ClassLoader myClassLoader = getClass().getClassLoader();

            oldClassLoader = Thread.currentThread().getContextClassLoader();

            if (oldClassLoader != myClassLoader) {
                Thread.currentThread().setContextClassLoader(myClassLoader);
                changedClassLoader = true;
            }

            new AntHelper(getAntFile(openl), getAntTarget(openl), getProperties(openl));

            // OpenLConfiguration conf =
            // (OpenLConfiguration)helper.getConfigurationObject(
            // getAntProjectConfigurationVariable(openl));

            IOpenLConfiguration conf = AntOpenLTask.retrieveConfiguration();

            op.setParser(new Parser(conf));

            op.setBinder(new Binder(conf, conf, conf, conf, conf, op));
            op.setVm(new SimpleVM());
            op.setCompileContext(new DefaultCompileContext());
        } catch (Exception ex) {
            throw RuntimeExceptionWrapper.wrap(ex);
        } finally {
            if (changedClassLoader) {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
            userCxt.pop();
        }
        return op;
    }

    protected String getAntFile(String openl) {

        String name = configurableResourceContext.findProperty(openl + ".ant.build.file");
        if (name != null) {
            File f = configurableResourceContext.findFileSystemResource(name);
            if (f != null) {
                return f.getAbsolutePath();
            }
            throw new OpenConfigurationException("File " + name + " is not found ", null, null);
        }

        name = openl + ".build.xml";
        if (name != null) {
            File f = configurableResourceContext.findFileSystemResource(name);
            if (f != null) {
                return f.getAbsolutePath();
            }
        }

        name = configurableResourceContext.findProperty("org.openl.default.ant.build.file");
        if (name != null) {
            File f = configurableResourceContext.findFileSystemResource(name);
            if (f != null) {
                return f.getAbsolutePath();
            }
            throw new OpenConfigurationException("File " + name + " is not found ", null, null);
        }

        throw new OpenConfigurationException("Can not find Ant configuration file for " + openl, null, null);
    }

    /**
     * @param openl
     */
    protected Properties getProperties(String openl) {
        URL url = configurableResourceContext.findClassPathResource(openl.replace('.', '/') + '/' + openl
                + ".ant.properties");
        if (url == null) {
            return null;
        }
        InputStream is = null;
        try {
            is = url.openStream();
            Properties p = new Properties();
            p.load(is);
            return p;
        } catch (IOException e) {
            throw RuntimeExceptionWrapper.wrap(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Throwable t) {
                Log.error("Error closing stream", t);
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.openl.conf.IOpenLBuilder#setConfigurableResourceContext(org.openl.conf.IConfigurableResourceContext)
     */
    public void setConfigurableResourceContext(IConfigurableResourceContext cxt, IUserContext ucxt) {
        configurableResourceContext = cxt;
        this.ucxt = ucxt;
    }

}