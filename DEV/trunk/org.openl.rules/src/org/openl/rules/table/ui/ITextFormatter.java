/**
 * Created Feb 28, 2007
 */
package org.openl.rules.table.ui;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openl.util.Log;

/**
 * @author snshor
 *
 */
public interface ITextFormatter
{
	public String format(Object obj);
	public Object parse(String value);
	
	static public class NumberTextFormatter implements ITextFormatter
	{
		DecimalFormat format;
		String fmtStr;
		
		public NumberTextFormatter(String fmt)
		{
			this.format = new DecimalFormat(fmt);
		}

		public NumberTextFormatter(DecimalFormat fmt, String fmtStr)
		{
			this.format = fmt;
			this.fmtStr = fmtStr;
		}
		
		public String format(Object obj)
		{
			return format.format(obj);
		}

		public Object parse(String value)
		{
			try
			{
				return format.parse(value);
			} catch (ParseException e)
			{
				Log.warn("Could not parse number: " + value, e);
			}
			
			try
			{
				return DateFormat.getDateInstance().parse(value);
			}
			catch(ParseException pe)
			{
				return value;
			}
			
		}
	}
	

	static public class DateTextFormatter implements ITextFormatter
	{
		SimpleDateFormat format;
		
		public DateTextFormatter(String fmt)
		{
			this.format = new SimpleDateFormat(fmt);
		}

		public String format(Object obj)
		{
			return format.format(obj);
		}
		public Object parse(String value)
		{
			try
			{
				return format.parse(value);
			} catch (ParseException e)
			{
				Log.warn("Could not parse number: " + value, e);
				return value;
			}
		}
	}
	
	
	
	static public class ConstTextFormatter implements ITextFormatter 
	{
		String format;

		public ConstTextFormatter(String format)
		{
			this.format = format;
		}

		public String format(Object obj)
		{
			return format;
		}

		public Object parse(String value)
		{
			return value;
		}
	}
}
