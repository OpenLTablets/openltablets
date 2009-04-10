/*
 * Created on Oct 11, 2005
 */
package org.openl.rules.helpers;

/**
 * @author snshor
 */
public class TablePrinter
{
    static public String LEFT = "LEFT", RIGHT = "RIGHT";

    String[] alignment;

    String separator;

    ITableAdaptor tableAdaptor;

    public TablePrinter(String[][] data, String[] alignment, String separator)
    {
        this.tableAdaptor = new StringArrayTableAdator(data);
        this.alignment = alignment;
        this.separator = separator;
    }

    public String print()
    {
        return print(0, tableAdaptor.height());
    }

    public String print(int from, int to)
    {
        StringBuffer sb = new StringBuffer();

        int[] width = calcWidth(tableAdaptor);

        for (int i = from; i < to; i++)
        {
            for (int j = 0; j < tableAdaptor.width(i); j++)
            {
                String alignRow = LEFT;
                if (alignment != null && alignment[j] != null)
                    alignRow = alignment[j];

                if (j != 0)
                    sb.append(separator);
                Object obj = tableAdaptor.get(j, i);
                String cell = "";
                if (obj != null)
                  cell = String.valueOf(obj);
                if (LEFT.equals(alignRow))
                {
                    sb.append(cell);
                    for (int k = 0; k < width[j] - cell.length(); k++)
                        sb.append(' ');

                }
                else if (RIGHT.equals(alignRow))
                {
                    for (int k = 0; k < width[j] - cell.length(); k++)
                        sb.append(' ');
                    sb.append(cell);

                }
            }
            sb.append("\n");
        }

        return sb.toString();

    }

    /**
     * @param data
     * @return
     */
    int[] calcWidth(ITableAdaptor ta)
    {
        int[] width = new int[ta.maxWidth()];

        for (int i = 0; i < ta.height(); i++)
        {
        	if (ta.width(i) <= 1)
        		continue;
        	
            for (int j = 0; j < ta.width(i); j++)
            {
            	Object x = ta.get(j, i);
              if (x != null)
                 width[j] = Math.max(width[j], String.valueOf(x).length());
            }
        }
        return width;

    }
    
    static class StringArrayTableAdator implements ITableAdaptor
    {
    	String[][] array;
    	StringArrayTableAdator(String[][] array)
    	{
    		this.array = array;
    	}
			public Object get(int col, int row)
			{
				String[] r = array[row];
				return  r == null || r.length <= col ? null : r[col];
			}
			
			public int width(int i)
			{
				return array[i].length;
			}
			
			public int height()
			{
				return array.length;
			}
			
			public int maxWidth()
			{
				int w = 0;
				for (int i = 0; i < array.length; i++)
				{
					if (array[i] != null)
						w = Math.max(w, array[i].length);
				}
				return w;
			}

    	
    	
    }

}
