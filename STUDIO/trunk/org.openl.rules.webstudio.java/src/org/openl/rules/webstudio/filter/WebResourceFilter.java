package org.openl.rules.webstudio.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Servlet filter to load web resources (images, html, etc). First, attempt is
 * made to load resource from classpath and then from web application root.
 *
 * @author Andrey Naumenko
 */
public class WebResourceFilter implements Filter {
    private static final String WEBRESOURCE_PREFIX = "/webresource";

    private FilterConfig filterConfig;

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String path = httpRequest.getRequestURI();

            if (StringUtils.contains(path, WEBRESOURCE_PREFIX)) {
                // When "webresource/**" is requested from html page which was
                // loaded via "webresource/**" itself
                // the path will contain 2 "webresource" strings.
                // "lastIndexOf" cuts off all prefixes at once.
                path = path.substring(path.lastIndexOf(WEBRESOURCE_PREFIX) + WEBRESOURCE_PREFIX.length());
                InputStream stream = WebResourceFilter.class.getResourceAsStream(path);
                if (stream == null) {
                    stream = new FileInputStream(new File(filterConfig.getServletContext().getRealPath(path)));
                }
                OutputStream out = response.getOutputStream();
                IOUtils.copy(stream, out);
                stream.close();
            } else {
                chain.doFilter(request, response);
            }
        } finally {
        }
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }
}
