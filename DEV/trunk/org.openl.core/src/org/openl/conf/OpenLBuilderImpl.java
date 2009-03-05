package org.openl.conf;

import org.openl.OpenConfigurationException;
import org.openl.OpenL;
import org.openl.conf.AOpenLBuilder;
import org.openl.conf.JavaImportTypeConfiguration;
import org.openl.conf.JavaLibraryConfiguration;
import org.openl.conf.LibraryFactoryConfiguration;
import org.openl.conf.NameSpacedLibraryConfiguration;
import org.openl.conf.NameSpacedTypeConfiguration;
import org.openl.conf.NoAntOpenLTask;
import org.openl.conf.TypeFactoryConfiguration;
import org.openl.syntax.impl.ISyntaxConstants;

public class OpenLBuilderImpl extends AOpenLBuilder {

    String extendsCategory = "org.openl.j";

    String category;

    String imports;

    String libName;

    public OpenL build(String category) throws OpenConfigurationException {
        OpenL.getInstance(extendsCategory, getUserContext());
        return super.build(category);
    }

    public NoAntOpenLTask getNoAntOpenLTask() {
        NoAntOpenLTask op = new NoAntOpenLTask();

        op.setExtendsCategory(extendsCategory);
        op.setCategory(category);

        if (libName != null) {
            LibraryFactoryConfiguration libraries = op.createLibraries();
            NameSpacedLibraryConfiguration library = new NameSpacedLibraryConfiguration();
            library.setNamespace(ISyntaxConstants.THIS_NAMESPACE);
            JavaLibraryConfiguration javalib = new JavaLibraryConfiguration();
            javalib.setClassName(libName);
            library.addJavalib(javalib);
            libraries.addConfiguredLibrary(library);
        }

        /**
         * <libraries>
         * 
         * <library namespace="org.openl.this"> <javalib
         * classname="org.openl.rules.helpers.Util"/> </library> </libraries>
         */

        if (imports != null) {
            TypeFactoryConfiguration types = op.createTypes();
            NameSpacedTypeConfiguration typelibrary = new NameSpacedTypeConfiguration();
            typelibrary.setNamespace(ISyntaxConstants.THIS_NAMESPACE);
            JavaImportTypeConfiguration javaimport = new JavaImportTypeConfiguration();
            // javaimport.setAll("com.exigen.ipb.rm.uk");
            //		
            // typelibrary.addJavaImport(javaimport);

            javaimport = new JavaImportTypeConfiguration();
            javaimport.setAll(imports);

            typelibrary.addJavaImport(javaimport);

            types.addConfiguredTypeLibrary(typelibrary);
        }

        /*
         * 
         * <types> <typelibrary namespace="org.openl.this"> <javaimport
         * all="${org.openl.rules.java.project.imports}"/> <javaimport
         * all="org.openl.rules.helpers"/> </typelibrary> </types>
         * 
         */

        return op;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getExtendsCategory() {
        return extendsCategory;
    }

    public void setExtendsCategory(String extendsCategory) {
        this.extendsCategory = extendsCategory;
    }

    public String getImports() {
        return imports;
    }

    public void setImports(String imports) {
        this.imports = imports;
    }

    public String getLibName() {
        return libName;
    }

    public void setLibName(String libName) {
        this.libName = libName;
    }

}
