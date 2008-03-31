package org.openl.j;

import org.openl.conf.AOpenLBuilder;
import org.openl.conf.ClassFactory;
import org.openl.conf.JavaImportTypeConfiguration;
import org.openl.conf.JavaLibraryConfiguration;
import org.openl.conf.JavaTypeConfiguration;
import org.openl.conf.LibraryFactoryConfiguration;
import org.openl.conf.NameSpacedLibraryConfiguration;
import org.openl.conf.NameSpacedTypeConfiguration;
import org.openl.conf.NoAntOpenLTask;
import org.openl.conf.NodeBinderFactoryConfiguration;
import org.openl.conf.OpenFactoryConfiguration;
import org.openl.conf.TypeCastFactory;
import org.openl.conf.TypeFactoryConfiguration;
import org.openl.conf.NodeBinderFactoryConfiguration.SingleBinderFactory;
import org.openl.grammars.bexgrammar.BExGrammar;
import org.openl.syntax.impl.ISyntaxConstants;

public class OpenLBuilder extends AOpenLBuilder
{



  
  public NoAntOpenLTask getNoAntOpenLTask()
  {
  		NoAntOpenLTask op = new NoAntOpenLTask();
  		
  		op.setCategory("org.openl.j");
  		op.setShared(false);
  		
  		ClassFactory cfg = op.createGrammar();
  		cfg.setClassName(BExGrammar.class.getName());
  		
  		NodeBinderFactoryConfiguration nbc = op.createBindings();
  		
  		
  		String[] binders =
  		{
  		"literal", org.openl.binding.impl.LiteralNodeBinder.class.getName()
  		,"literal.integer",org.openl.binding.impl.IntNodeBinder.class.getName()
  		,"literal.real", org.openl.binding.impl.DoubleNodeBinder.class.getName()
  		,"literal.percent", org.openl.binding.impl.PercentNodeBinder.class.getName()
  		,"literal.string",org.openl.binding.impl.StringNodeBinder.class.getName()
  		,"literal.char", org.openl.binding.impl.CharNodeBinder.class.getName()
  		,"array.init",org.openl.binding.impl.ArrayInitializationBinder.class.getName()

  		,"module.top",org.openl.binding.impl.module.ModuleNodeBinder.class.getName()
  		,"method.header",org.openl.binding.impl.MethodHeaderNodeBinder.class.getName()
  		,"method.parameters",org.openl.binding.impl.module.MethodParametersNodeBinder.class.getName()
  		,"method.declaration",org.openl.binding.impl.module.MethodDeclarationNodeBinder.class.getName()
  		,"var.declaration", org.openl.binding.impl.module.VarDeclarationNodeBinder.class.getName()
  		,"parameter.declaration", org.openl.binding.impl.module.ParameterDeclarationNodeBinder.class.getName()

  		,"block", org.openl.binding.impl.BlockBinder.class.getName()

  		
  		,"op.binary", org.openl.binding.impl.BinaryOperatorNodeBinder.class.getName()
  		,"op.binary.and", org.openl.binding.impl.BinaryOperatorAndNodeBinder.class.getName()
  		,"op.binary.or", org.openl.binding.impl.BinaryOperatorOrNodeBinder.class.getName()
  		,"op.unary", org.openl.binding.impl.UnaryOperatorNodeBinder.class.getName()
  		,"op.prefix", org.openl.binding.impl.PrefixOperatorNodeBinder.class.getName()
  		,"op.suffix", org.openl.binding.impl.SuffixOperatorNodeBinder.class.getName()
  		,"op.assign", org.openl.binding.impl.AssignOperatorNodeBinder.class.getName()
  		,"op.new.object",org.openl.binding.impl.NewNodeBinder.class.getName()
  		,"op.new.array", org.openl.binding.impl.NewArrayNodeBinder.class.getName()
  		,"op.index", org.openl.binding.impl.IndexNodeBinder.class.getName()
  		,"op.ternary.qmark", org.openl.binding.impl.QMarkNodeBinder.class.getName()
  		,"type.cast", org.openl.binding.impl.TypeCastBinder.class.getName()


  		,"local.var.declaration",org.openl.binding.impl.LocalVarBinder.class.getName()
  		,"type.declaration", org.openl.binding.impl.TypeBinder.class.getName()

  		,"function",org.openl.binding.impl.MethodNodeBinder.class.getName()
  		,"identifier",org.openl.binding.impl.IdentifierBinder.class.getName()
  		,"identifier.sequence",org.openl.binding.impl.IdentifierSequenceBinder.class.getName()
//  		,"chain","org.openl.binding.impl.ChainBinder"
  		,"chain", org.openl.binding.impl.BExChainBinder.class.getName()
  		,"chain.suffix",org.openl.binding.impl.BExChainSuffixBinder.class.getName()

  		, "where.expression",  org.openl.binding.impl.WhereExpressionNodeBinder.class.getName()
  		, "where.var.expalnation",  org.openl.binding.impl.WhereVarNodeBinder.class.getName()
  		, "list", org.openl.binding.impl.ListNodeBinder.class.getName()
  		
  		
  		,"control.for",org.openl.binding.impl.ForNodeBinder.class.getName()
  		,"control.if", org.openl.binding.impl.IfNodeBinder.class.getName()
  		,"control.while",org.openl.binding.impl.WhileNodeBinder.class.getName()
  		,"control.return",org.openl.binding.impl.ReturnNodeBinder.class.getName()
  		

  		
  		};

  		
  		for (int i = 0; i < binders.length/2; i++) 
  		{
    		SingleBinderFactory sbf = new SingleBinderFactory();
    		sbf.setNode(binders[2*i]);
    		sbf.setClassName(binders[2*i+1]);
    		nbc.addConfiguredBinder(sbf);
  			
			}

  		 LibraryFactoryConfiguration lfc = op.createLibraries();
  		 NameSpacedLibraryConfiguration nslc = new NameSpacedLibraryConfiguration();
  		 nslc.setNamespace("org.openl.operators");
  		 JavaLibraryConfiguration javalib = new JavaLibraryConfiguration();
  		 javalib.setClassName(org.openl.binding.impl.Operators.class.getName());
  		 nslc.addJavalib(javalib);
  		 lfc.addConfiguredLibrary(nslc);

  		 
  		 
  		 
/**  		
  	  <libraries>
      <library namespace="org.openl.operators">
        <javalib classname="org.openl.binding.impl.Operators"/>
      </library>
    </libraries>
**/
  		 
  		 OpenFactoryConfiguration of = new OpenFactoryConfiguration();
  		 of.setName("java.factory");
  		 of.setImplementingClass(org.openl.types.java.JavaOpenFactory.class.getName());
  		 op.addConfiguredTypeFactory(of);
  		 
/**  		 

    <typefactory name="java.factory" implementingClass="org.openl.types.java.JavaOpenFactory"/>
**/
  		 
  		 
  	TypeFactoryConfiguration types = op.createTypes();
  	NameSpacedTypeConfiguration nstc = new NameSpacedTypeConfiguration();
  	nstc.setNamespace(ISyntaxConstants.THIS_NAMESPACE);
  	JavaTypeConfiguration javatype = new JavaTypeConfiguration();
  	javatype.setClassName(org.openl.types.java.JavaPrimitiveTypeLibrary.class.getName());
  	nstc.addJavaType(javatype);
  	javatype = new JavaTypeConfiguration();
  	javatype.setClassName(org.openl.types.java.JavaLang.class.getName());
  	nstc.addJavaType(javatype);
  	JavaImportTypeConfiguration javaimport = new JavaImportTypeConfiguration();
  	JavaImportTypeConfiguration.StringHolder anImport = new JavaImportTypeConfiguration.StringHolder();
  	anImport.addText("java.util");
  	
  	javaimport.addConfiguredImport(anImport);
  	nstc.addJavaImport(javaimport);
  	
  	types.addConfiguredTypeLibrary(nstc);
  	
  	
  	
/**  		 
  		 
    <types>
      <typelibrary namespace="org.openl.this">
        <javatype classname="org.openl.types.java.JavaPrimitiveTypeLibrary"/>
        <javatype classname="org.openl.types.java.JavaLang"/>
        <javaimport>
           <import>java.util</import>
        </javaimport>
      </typelibrary>
    </types>
**/
  	
  	TypeCastFactory typecast = op.createTypecast();
  	TypeCastFactory.JavaCastComponent javacast = new TypeCastFactory.JavaCastComponent();
  	javacast.setLibraryClassName(org.openl.binding.impl.Operators.class.getName());
  	javacast.setClassName(org.openl.binding.impl.ACastFactory.class.getName());
  	
  	typecast.addJavaCast(javacast);
  	
/**  	
    <typecast>
       <javacast libraryclassname="org.openl.binding.impl.Operators" classname="org.openl.binding.impl.ACastFactory"/> 
    </typecast> 	
**/  		
  		
  		return op;
  	
  }

}
