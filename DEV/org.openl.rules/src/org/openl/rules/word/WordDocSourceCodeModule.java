package org.openl.rules.word;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.poi.hwpf.HWPFDocument;
import org.openl.IOpenSourceCodeModule;
import org.openl.rules.indexer.IDocumentType;
import org.openl.rules.indexer.IIndexElement;
import org.openl.syntax.impl.SourceCodeModuleDelegator;
import org.openl.util.Log;
import org.openl.util.RuntimeExceptionWrapper;

public class WordDocSourceCodeModule extends SourceCodeModuleDelegator implements IIndexElement
{
	
	

	HWPFDocument document;
	
	public WordDocSourceCodeModule(IOpenSourceCodeModule src, HWPFDocument document)
	{
		super(src);
		this.document = document;
	}

	public WordDocSourceCodeModule(IOpenSourceCodeModule src)
	{
		super(src);
		
		InputStream is = null;
		try
		{
			is = src.getByteStream();
	
			document = new HWPFDocument(is);
		}	
		catch(Throwable t)
		{
			throw RuntimeExceptionWrapper.wrap(t);
		}
		finally
		{
      try
      {
        if (is != null)
          is.close();

      } catch (Throwable e)
      {
        Log.error("Error trying close input stream:", e);
      }
		}
	}
	
	
	public String getUri()
	{
		return src.getUri(0);
	}

	public IIndexElement getParent()
	{
		return null;
	}


	public String getIndexedText()
	{
		return getDisplayName();
	}

	public String getDisplayName()
	{
		String uri = src.getUri(0);
		
		try
		{
			URL url = new URL(uri);
			String file = url.getFile();
			int index = file.lastIndexOf('/');
			
			return index < 0 ? file : file.substring(index);
			
		} catch (MalformedURLException e)
		{
			throw RuntimeExceptionWrapper.wrap(e);
		}
		
	}

	public String getType()
	{
		return IDocumentType.WORD_DOC.getType();
	}

	public String getCategory()
	{
		return IDocumentType.WORD_DOC.getCategory();
	}
	

	public HWPFDocument getDocument()
	{
		return document;
	}


}
