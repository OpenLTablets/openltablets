package org.openl.rules.webstudio.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openl.IOpenSourceCodeModule;
import org.openl.rules.ui.studio.WebStudio;
import org.openl.syntax.impl.FileSourceCodeModule;

public class DownloadServlet extends HttpServlet {
    private static final long serialVersionUID = -5102656998760586960L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String filename = request.getParameter("filename");
        if (filename != null) {
            File file = new File(filename);
            if (file.isFile() && checkFile(request, file)) {
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment;filename=\"" + file.getName() + "\"");

                ServletOutputStream outputStream = response.getOutputStream();
                dumpFile(file, outputStream);
                outputStream.flush();
                outputStream.close();
            }
        }
    }

    /**
     * Performs a check on a file, to prevent downloading  ANY file on the computer. Checks that <code>file</code>
     * is in the same directory with currently opened project in webstudio. 
     *
     * @param request current request
     * @param file file to check
     * @return if downloading the file is allowed
     */
    private static boolean checkFile(HttpServletRequest request, File file) {
        WebStudio webStudio = getWebStudio(request);
        if (webStudio == null) return false;

        IOpenSourceCodeModule module = webStudio.getModel().getXlsModuleNode().getModule();
        if (module instanceof FileSourceCodeModule) {
            FileSourceCodeModule fileSourceCodeModule = (FileSourceCodeModule) module;
            try {
                return file.getParentFile().equals(fileSourceCodeModule.getFile().getParentFile().getCanonicalFile());
            } catch (IOException e) {}
        }
        return false;
    }

    private static WebStudio getWebStudio(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return (WebStudio) (session == null ? null : session.getAttribute("studio"));
    }

    private static void dumpFile(File file, OutputStream out) throws IOException {
        byte bytes[] = new byte[1 << 15];
        FileInputStream fis = new FileInputStream(file);
        try {
            int len;
            while ((len = fis.read(bytes)) != -1) out.write(bytes, 0, len);
        } finally {
            fis.close();
        }
    }

}
