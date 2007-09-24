/*
 * Created on Nov 3, 2003
 *
 * Developed by Intelligent ChoicePoint Inc. 2003
 */

package org.openl.rules.data;

import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.openl.binding.IBindingContext;
import org.openl.syntax.impl.ISyntaxConstants;
import org.openl.types.IOpenClass;
import org.openl.util.RuntimeExceptionWrapper;

/**
 * @author snshor
 *
 */
public class String2DataConvertorFactory
{

	static HashMap convertors;

	public static IString2DataConvertor getConvertor(Class c) //throws Exception
	{
		IString2DataConvertor conv = (IString2DataConvertor) convertors.get(c);

		if (conv != null)
			return conv;



		try
		{
			Constructor ctr = c.getDeclaredConstructor(new Class[]{String.class});
			return new String2ConstructorConvertor(ctr);
		}
		catch (Throwable t)
		{
			throw new RuntimeException("Convertor or Public Constructor " + c.getName() + "(String s) does not exist");
//			return null;
		}

	}




	
	
	
	public static class String2ConstructorConvertor implements IString2DataConvertor
	{
		
		Constructor ctr;
		
		String2ConstructorConvertor(Constructor ctr)
		{
			this.ctr = ctr;
		}
		
		public Object parse(String data, String format, IBindingContext cxt)
		{
			
			try
			{
				return ctr.newInstance(new Object[]{data});
			}
			catch (Exception e)
			{
				throw RuntimeExceptionWrapper.wrap(e);
			}
		}
		/**
		 *
		 */

		public String format(Object data, String format)
		{
			return String.valueOf(data);
		}

	}
	



	//Initialize 

	static {
		convertors = new HashMap();

		convertors.put(int.class, new String2IntConvertor());
		convertors.put(double.class, new String2DoubleConvertor());
		convertors.put(char.class, new String2CharConvertor());
		convertors.put(boolean.class, new String2BooleanConvertor());
		convertors.put(long.class, new String2LongConvertor());


		convertors.put(Integer.class, new String2IntConvertor());
		convertors.put(Double.class, new String2DoubleConvertor());
		convertors.put(Character.class, new String2CharConvertor());
		convertors.put(Boolean.class, new String2BooleanConvertor());
		convertors.put(Long.class, new String2LongConvertor());



		convertors.put(String.class, new String2StringConvertor());
		convertors.put(Date.class, new String2DateConvertor());
		convertors.put(Class.class, new String2ClassConvertor());
		convertors.put(IOpenClass.class, new String2OpenClassConvertor());
	}

	public static class String2LongConvertor implements IString2DataConvertor
	{
		
		public Object parse(String data, String format, IBindingContext cxt)
		{
			if (format == null)
				return Long.valueOf(data);
			DecimalFormat df = new DecimalFormat(format);
			
			 Number n;
			try
			{
				n = df.parse(data);
			}
			catch (ParseException e)
			{
				throw RuntimeExceptionWrapper.wrap(e);
			}
			 
			 return new Long(n.longValue());	
		}
		/**
		 *
		 */

		public String format(Object data, String format)
		{
			if (format == null)
				return String.valueOf(data);
			DecimalFormat df = new DecimalFormat(format);
			return 	df.format(((Long)data).intValue());
		}

	}




	public static class String2IntConvertor implements IString2DataConvertor
	{
		
		public Object parse(String data, String format, IBindingContext cxt)
		{
			if (format == null)
				return Integer.valueOf(data);
			DecimalFormat df = new DecimalFormat(format);
			
			 Number n;
			try
			{
				n = df.parse(data);
			}
			catch (ParseException e)
			{
				throw RuntimeExceptionWrapper.wrap(e);
			}
			 
			 return new Integer(n.intValue());	
		}
		/**
		 *
		 */

		public String format(Object data, String format)
		{
			if (format == null)
				return String.valueOf(data);
			DecimalFormat df = new DecimalFormat(format);
			return 	df.format(((Integer)data).intValue());
		}

	}

	public static class String2DoubleConvertor implements IString2DataConvertor
	{
		
		public Object parse(String xdata, String format, IBindingContext cxt)
		{
			
			if (format != null)
			{
				DecimalFormat df = new DecimalFormat(format);
				try
				{
					Number n = df.parse(xdata);
					
					return new Double(n.doubleValue());
				}
				catch (ParseException e)
				{
					throw RuntimeExceptionWrapper.wrap("", e);
				}
			}
			
			
			String data = numberStringWithoutModifier(xdata);
			
			double d = Double.parseDouble(data);
			
			return xdata == data ? new Double(d) : new Double(d * numberModifier(xdata));
		}
		/**
		 *
		 */

		public String format(Object data, String format)
		{
			if (format == null)
			{
				format = "#0.00";
			}
			
			DecimalFormat df = new DecimalFormat(format);
			
			return df.format(((Number)data).doubleValue());
		}

	}
	
	
	
	static String numberStringWithoutModifier(String s)
	{
		if (s.endsWith("%"))
		  return s.substring(0, s.length() - 1);
		  
		return s;  
	}
	
	static double numberModifier(String s)
	{
		if (s.endsWith("%"))
		  return 0.01;
		  
		return 1;  
	}
	
	
	

	public static class String2CharConvertor implements IString2DataConvertor
	{
		
		public Object parse(String data, String format, IBindingContext cxt)
		{
			if (data.length() != 1)
			  throw new IndexOutOfBoundsException("Character field must have only one symbol");
			
			return new Character(data.charAt(0));
		}

		public String format(Object data, String forma)
		{
			return new String(
			   new char[]{((Character)data).charValue()})
			;
		}


	}
	
	
	

	public static class String2BooleanConvertor implements IString2DataConvertor
	{
		
		public Object parse(String data, String format, IBindingContext cxt)
		{
			if (data == null || data.length() == 0)
			  return Boolean.FALSE;
			
			String lcase = data.toLowerCase().intern();
			
			return lcase == "true" || lcase == "yes" || lcase == "t" || lcase == "y" ? Boolean.TRUE : Boolean.FALSE;
		}
		/**
		 *
		 */

		public String format(Object data, String format)
		{
			return String.valueOf(data);
		}

	}
	
	
	public static class String2ClassConvertor implements IString2DataConvertor
	{
		
		public Object parse(String data, String format, IBindingContext cxt)
		{
			IOpenClass c =  cxt.findType(ISyntaxConstants.THIS_NAMESPACE, data);
			
			if (c == null)
				throw new RuntimeException("Type " + data + " is not found");

			return c.getInstanceClass();
		}
		/**
		 *
		 */

		public String format(Object data, String format)
		{
			return String.valueOf(data);
		}

	}
	
	public static class String2OpenClassConvertor implements IString2DataConvertor
	{
		
		public Object parse(String data, String format, IBindingContext cxt)
		{
			IOpenClass c =  cxt.findType(ISyntaxConstants.THIS_NAMESPACE, data);
			
			if (c == null)
				throw new RuntimeException("Type " + data + " is not found");

			return c;
		}

		/**
		 *
		 */

		public String format(Object data, String format)
		{
			return String.valueOf(data);
		}

	}	
	
	public static class String2StringConvertor implements IString2DataConvertor
	{
		
		public Object parse(String data, String format, IBindingContext cxt)
		{
			return data;
		}

		public String format(Object data, String format)
		{
			return String.valueOf(data);
		}

	}
	

	static public class String2DateConvertor implements IString2DataConvertor
	{
		
		
		
		public Object parse(String data, String format,  IBindingContext cxt)
		{
			return parseDate(data, format);
		}
		
		public Date parseDate(String data, String format)
		{
			
			DateFormat df = format == null ? DateFormat.getDateInstance(DateFormat.SHORT): new SimpleDateFormat(format);
			
			
			try
			{
				return df.parse(data);
			}
			catch (ParseException e)
			{
				try
				{
					int value = Integer.parseInt(data);
					Calendar cc = Calendar.getInstance();
					cc.set(1900, 0, 1);
					cc.add(Calendar.DATE, value - 1);
					return cc.getTime();
					
				}
				catch(Throwable t)
				{
				}
				throw RuntimeExceptionWrapper.wrap(e);
			}
		}
		/**
		 *
		 */

		public String format(Object data, String format)
		{
				DateFormat df = format == null ? DateFormat.getDateInstance(DateFormat.SHORT): new SimpleDateFormat(format);
				return df.format(data);
		}

	}
	
	
	
	
	
}
