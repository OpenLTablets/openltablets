package org.openl.rules.webstudio.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This filter is intended to reload a filter when it's configurations is
 * changed. For example DelegatingFilterProxy has a bug:
 * https://jira.springsource.org/browse/SPR-6228 That's why we should recreate
 * it when we are reloading spring context.
 * 
 * In web.xml you should configure filter parameter "delegateClass" and add all
 * parameters for delegate filter. For example:
 * 
 * <pre>
 * {@code
 *   <filter>
 *       <filter-name>SecurityFilter</filter-name>
 *       <filter-class>org.openl.rules.webstudio.filter.ReloadableDelegatingFilter</filter-class>
 *       <init-param>
 *           <description>Delegate filter class name</description>
 *           <param-name>delegateClass</param-name>
 *           <param-value>org.openl.rules.webstudio.filter.SecurityFilter</param-value>
 *       </init-param>
 *       <init-param>
 *           <description>SecurityFilter parameter</description>
 *           <param-name>targetBeanName</param-name>
 *           <param-value>filterChainProxy</param-value>
 *       </init-param>
 *   </filter>
 * }</pre>
 * 
 * @author NSamatov
 * 
 */
public class ReloadableDelegatingFilter implements Filter {
    private static final String DELEGATE_CLASS = "delegateClass";
    private static final String DELEGATE_FILTER_NAME = "delegateFilterName";

    private static ThreadLocal<ConfigurationReloader> reloaderHolder = new ThreadLocal<ConfigurationReloader>();

    private final Log log = LogFactory.getLog(ReloadableDelegatingFilter.class);

    private final ReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock read = rwl.readLock();
    private final Lock write = rwl.writeLock();

    private FilterConfig filterConfig;
    private String delegateClass;
    private String delegateFilterName;

    private Filter delegate;

    /**
     * Schedule configuration reloader to perform reload of configuration 
     * 
     * @param reloader for example spring context reloading task
     */
    public static void reload(ConfigurationReloader reloader) {
        reloaderHolder.set(reloader);
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        write.lock();
        try {
            this.filterConfig = filterConfig;
            delegateClass = filterConfig.getInitParameter(DELEGATE_CLASS);
            delegateFilterName = filterConfig.getInitParameter(DELEGATE_FILTER_NAME);

            if (delegateFilterName == null) {
                delegateFilterName = filterConfig.getFilterName() + "Delegate";
            }

            delegate = createFilter();
        } finally {
            write.unlock();
        }
    }

    @Override
    public void destroy() {
        write.lock();
        try {
            if (delegate != null) {
                delegate.destroy();
                delegate = null;
            }
        } finally {
            write.unlock();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
            ServletException {
        read.lock();
        try {
            if (delegate != null) {
                delegate.doFilter(request, response, filterChain);
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Delegate proxy is not configured");
                }
                filterChain.doFilter(request, response);
            }
        } catch (ServletException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            throw e;
        } finally {
            read.unlock();
        }

        reloadConfigurationIfNeeded();
    }

    private void reloadConfigurationIfNeeded() throws ServletException {
        ConfigurationReloader reloader = reloaderHolder.get();
        if (reloader != null) {
            Filter oldDelegate;

            write.lock();
            try {
                try {
                    reloader.reload();
                } catch (RuntimeException e) {
                    log.error(e.getMessage(), e);
                }

                oldDelegate = delegate;
                delegate = createFilter();
            } finally {
                write.unlock();
                reloaderHolder.remove();
            }

            oldDelegate.destroy();
        }
    }

    private Filter createFilter() throws ServletException {
        Filter filter = null;

        if (delegateClass != null) {
            try {
                filter = (Filter) Class.forName(delegateClass).newInstance();
                filter.init(new FilterConfig() {

                    @Override
                    public ServletContext getServletContext() {
                        return filterConfig.getServletContext();
                    }

                    @Override
                    public Enumeration<String> getInitParameterNames() {
                        return filterConfig.getInitParameterNames();
                    }

                    @Override
                    public String getInitParameter(String param) {
                        return filterConfig.getInitParameter(param);
                    }

                    @Override
                    public String getFilterName() {
                        return delegateFilterName;
                    }
                });
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }

        return filter;
    }

    /**
     * Configuration reloader. For example:
     * 
     * <pre>
     * 
     * new ConfigurationReloader() {
     * 
     *     public void reload() {
     *         XmlWebApplicationContext context = (XmlWebApplicationContext) WebApplicationContextUtils
     *                 .getWebApplicationContext(servletContext);
     *         context.refresh();
     *     }
     * }
     * 
     * </pre>
     * 
     * @author NSamatov
     * 
     */
    public static interface ConfigurationReloader {
        void reload();
    }
}
