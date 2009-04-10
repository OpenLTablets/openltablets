package org.openl.util;

import java.lang.reflect.Array;

import java.util.*;

public class ArrayTool
{
  static public final Object[] ZERO_OBJECT = {};
  static public final Class<?>[] ZERO_CLASS = {};
  static public final String[] ZERO_STRING = {};
  static public final int[] ZERO_int = {};
  static public final char[] ZERO_char = {};

  public static void swap(Object array, int i1, int i2)
  {
    Object value1 = Array.get(array, i1);
    Object value2 = Array.get(array, i2);

    Array.set(array, i1, value2);
    Array.set(array, i2, value1);
  }

  public static void move(Object array, int index, int delta)
  {
    Object value = Array.get(array, index);
    if(delta>0)
    {
      for(int i=0;i<delta;i++)
      {
        Object newValue = Array.get(array, index+i+1);
        Array.set(array, index+i, newValue);
      }
    }
    else if(delta<0)
    {
      for(int i=0;i>delta;i--)
      {
        Object newValue = Array.get(array, index+i-1);
        Array.set(array, index+i, newValue);
      }
    }
    Array.set(array, index+delta, value);
  }

  public static void swap(Object array, Object value1, Object value2)
  {
    int i1 = findFirstElementIndex(array, value1);
    int i2 = findFirstElementIndex(array, value2);

    swap(array, i1, i2);
  }

  public static Object insertValue(int i, Object oldArray, Object value)
  {
    int oldSize = Array.getLength(oldArray);
    Object newArray = Array.newInstance(oldArray.getClass().getComponentType(), oldSize+1);

    if (i > 0)
      System.arraycopy(oldArray,0,  newArray,0, i);

    Array.set(newArray, i, value);

    if(i < oldSize)
      System.arraycopy(oldArray, i, newArray, i + 1, oldSize - i);

    return newArray;
  }

  public interface ArrayModel
  {
    public Object getObject(int i);
  }

  public static Object insertValues(int[] indexes, Object oldArray, ArrayModel value)
  {
    if(indexes.length==0)
      return oldArray;
    if(indexes.length==1)
      return insertValue(indexes[0], oldArray, value.getObject(indexes[0]));

    Arrays.sort(indexes);

    int oldSize = Array.getLength(oldArray);

    int validIndexesSize=0;
    for(int i=0;i<indexes.length;i++)
    {
      if(indexes[i]<=oldSize)
        validIndexesSize=i+1;
    }
    Object newArray = oldArray;
    if(validIndexesSize==1)
    {
      newArray = insertValue(indexes[0], oldArray, value.getObject(indexes[0]));
    }
    else if(validIndexesSize>1)
    {
      Class<?> componentType = oldArray.getClass().getComponentType();
      newArray = Array.newInstance(componentType, oldSize+validIndexesSize);

      for(int i=0;i<validIndexesSize;i++)
      {
        if(i==0)
        {
          if (indexes[i] > 0)
            System.arraycopy(oldArray,0,  newArray,0, indexes[i]);
        }
        else if(i==validIndexesSize-1)
        {
          int prev_i1 = indexes[i-1];
          if(prev_i1 < indexes[i])
            System.arraycopy(oldArray, prev_i1, newArray, prev_i1 + i, indexes[i] - prev_i1);

          if(indexes[i] < oldSize)
            System.arraycopy(oldArray, indexes[i], newArray, indexes[i] + i + 1, oldSize - indexes[i]);
        }
        else
        {
          int prev_i1 = indexes[i-1];
          if(prev_i1 < indexes[i])
            System.arraycopy(oldArray, prev_i1, newArray, prev_i1 + i, indexes[i] - prev_i1);
        }
        Array.set(newArray, indexes[i]+i, value.getObject(indexes[i]));
      }
    }
    return newArray;
  }

  public static Object add(Object array, Object value)
  {
    return insertValue(Array.getLength(array), array, value);
  }

  public static Object merge(Object array1, Object array2)
  {
    return 
     merge(new Object[] {array1, array2});
  }

  public static Object merge(Object[] arrays)
  {
    if(arrays==null || arrays.length==0)
      return new Object[0];
    if(arrays.length==1)
      return arrays[0];

    int newSize = 0;
    for(int i=0;i<arrays.length;i++)
      newSize+=Array.getLength(arrays[i]);

    if(newSize==Array.getLength(arrays[0]))
      return arrays[0];

    Object newArray = Array.newInstance(arrays[0].getClass().getComponentType(), newSize);
    int pos = 0;
    for(int i=0;i<arrays.length;i++)
    {
      int sz = Array.getLength(arrays[i]);
      System.arraycopy(arrays[i], 0, newArray, pos, sz);
      pos += Array.getLength(arrays[i]);
    }
    return newArray;
  }

  public static Object removeValue(int i, Object oldArray)
  {
    int oldSize = Array.getLength(oldArray);

    Object newArray = Array.newInstance(oldArray.getClass().getComponentType(), oldSize-1);

    if(i > 0)
      System.arraycopy(oldArray,0,  newArray,0, i);

    int i1 = i + 1;

    if(i1 < oldSize)
      System.arraycopy(oldArray, i1, newArray, i, oldSize - i1);

    return newArray;
  }

  public static Object removeValues(int[] indexes, Object oldArray)
  {
    if(indexes.length==0)
      return oldArray;

    int oldSize = Array.getLength(oldArray);
    Arrays.sort(indexes);
    int validIndexesSize=0;
    for(int i=0;i<indexes.length;i++)
    {
      if(indexes[i]<oldSize)
        validIndexesSize=i+1;
    }

    if(validIndexesSize==0)
      return oldArray;
    if(validIndexesSize==1)
      return removeValue(indexes[0], oldArray);

    Object newArray = Array.newInstance(oldArray.getClass().getComponentType(), oldSize-validIndexesSize);

    for(int i=0;i<validIndexesSize;i++)
    {
      if(i==0)
      {
        if(indexes[i] > 0)
          System.arraycopy(oldArray,0,  newArray,0, indexes[i]);
      }
      else if(i==validIndexesSize-1)
      {
        int prev_i1 = indexes[i-1]+1;
        if(prev_i1 < indexes[i])
          System.arraycopy(oldArray, prev_i1, newArray, prev_i1-i, indexes[i] - prev_i1);

        int i1 = indexes[i] + 1;
        if(i1 < oldSize)
          System.arraycopy(oldArray, i1, newArray, i1-i-1, oldSize - i1);
      }
      else
      {
        int prev_i1 = indexes[i-1]+1;
        if(prev_i1 < indexes[i])
          System.arraycopy(oldArray, prev_i1, newArray, prev_i1-i, indexes[i] - prev_i1);
      }
    }
    return newArray;
  }

  public static Object zeroArray(Class<?> c)
  {
    if(c==String.class)
      return ZERO_STRING;
    return Array.newInstance(c, 0);
  }

  public static Object copy(Object oldArray)
  {
    int size = Array.getLength(oldArray);
    Object newArray = Array.newInstance(oldArray.getClass().getComponentType(),
      size);
    System.arraycopy(oldArray,0,  newArray,0, size);
    return newArray;
  }

  public static Object ensureSize(Object oldArray, int newSize)
  {
    int oldSize = Array.getLength(oldArray);
    if (oldSize >= newSize)
      return oldArray;
    Class<?> componentType = oldArray.getClass().getComponentType();
    Object newArray = Array.newInstance(componentType, newSize);
    System.arraycopy(oldArray,0,  newArray,0, oldSize);
    return newArray;
  }

  public static Object resize(Object oldArray, int newSize)
  {
    int oldSize = Array.getLength(oldArray);
    if (oldSize == newSize)
      return oldArray;
    Class<?> componentType = oldArray.getClass().getComponentType();
    Object newArray = Array.newInstance(componentType, newSize);
    System.arraycopy(oldArray,0,  newArray,0, Math.min(oldSize, newSize));
    return newArray;
  }

  public static int findFirstElementIndex(Object array, Object element)
  {
    return findFirstIndex(array, ASelector.selectObject(element));
  }

  public static Object remove(Object array, Object element)
  {
    int idx = findFirstElementIndex(array, element);
    if (idx == -1)
      return array;
    return removeValue(idx, array);
  }



  public static Object findFirstElement(Object array, ISelector s)
  {
    int index = findFirstIndex(array, s);
    if (index < 0)
      return null;

    return Array.get(array, index);
  }

  public static int findFirstIndex(Object array, ISelector sel)
  {
    int size = Array.getLength(array);
    for(int i = 0; i < size; ++i)
    {
      if (sel.select(Array.get(array, i)))
        return i;
    }

    return -1;
  }

  public static boolean contains(Object array, Object test )
  {
    return findFirstElementIndex(array, test) >= 0;
  }

  /**
   * Returns true if array container contains all the elements of array testArray
   */

  public static <T> boolean containsAll(T[] container, T[] testArray )
  {
     Iterator<T> it = iterator(testArray);
     while(it.hasNext())
       if(!contains(container, it.next()))
         return false;
     return  true;
  }

  /**
   * Returns true if both array1 and array2 have at least one the same element
   */

  public static <T> boolean intersects(T[] array1, T[] array2)
  {
     Iterator<T> it = iterator(array1);
     while(it.hasNext())
       if(contains(array2, it.next()))
         return true;
     return false;
  }

  /**
   * Returns true if containsAll(array1, array2) && containsAll(array2, array1)
   */

  public static <T> boolean haveSameElements(T[] array1, T[] array2)
  {
    return containsAll(array1, array2) && containsAll(array2, array1);
  }


  /**
   * @return array's size
   */

  public static int size(Object ary)
  {
    return Array.getLength(ary);
  }

//  public static int size(Object[] ary)
//  {
//    return ary.length;
//  }
//

  @SuppressWarnings("unchecked")
public static <DST, SRC> DST[] collect(SRC[] src, Class<DST> dstType, IConvertor<SRC, DST> c)
  {
    int size = src.length;
    DST[] dstArray = (DST[])Array.newInstance(dstType, size);
    for(int i = 0; i < size; ++i)
    {
      Array.set(dstArray, i, c.convert(src[i]));
    }

    return dstArray;
  }

  public static <T> Iterator<T> iterator(T[] array)
  {
    return new ArrayIterator<T>(array);
  }

  public static Enumeration enumeration(Object array)
  {
    return new ArrayEnumeration(array);
  }


  static class ArrayEnumeration implements Enumeration<Object>
  {
    int _index = 0;
    int _size;
    Object _array;

    ArrayEnumeration(Object array)
    {
      _size = Array.getLength(array);
      _array = array;
    }

    public boolean hasMoreElements()
    {
      return _index < _size;
    }

    public Object nextElement()
    {
      return Array.get(_array, _index++);
    }
  }



  static class ArrayIterator<T> implements Iterator<T>
  {
    int _index = 0;
    int _size;
    T[] _array;

    ArrayIterator(T[] array)
    {
      _size = Array.getLength(array);
      _array = array;
    }

    public boolean hasNext()
    {
      return _index < _size;
    }

    public T next()
    {
      return _array[_index++];
    }

    public void remove()
    {
      throw new RuntimeException("Should not be called");
    }

  }



  static  public  <T> Object[] toArray(List<T> v, Class<?> c)
  {
    Object[] ary = (Object[])Array.newInstance(c, v.size());
    return v.toArray(ary);
  }

  static public Set toSet(Object[] ary)
  {
    Set set = new HashSet();
    for(int i=0;i<ary.length;i++)
      set.add(ary[i]);
    return set;
  }

  static public String asString(Object ary)
  {
    return asString(ary, 128);
  }

  static public String asString(Object ary, int maxLength)
  {
    StringBuffer buf = new StringBuffer(100);

    print(ary, buf, maxLength);

    if( buf.length() > maxLength)
    {
      String ellipses = "...";
      buf.delete(maxLength - ellipses.length(), buf.length()).append(ellipses);
    }

    return buf.toString();
  }


  static void print(Object obj, StringBuffer buf, int maxLength)
  {
    if (obj == null)
      buf.append("null");
    else if (obj instanceof String)
      buf.append('"').append(obj).append('"');
    else if (obj instanceof Class)
      buf.append( ((Class<?>)obj).getName() );
    else if (obj.getClass().isArray())
      printArray(obj,buf,maxLength);
    else
      buf.append(obj);
  }


  static void printArray(Object ary, StringBuffer buf, int maxLength)
  {
    int size = Array.getLength(ary);

    buf.append('[');
    for(int i = 0; i < size; ++i)
    {
      if (i > 0)
        buf.append(", ");

      print(Array.get(ary, i), buf, maxLength);

      if(buf.length() > maxLength)
        return;
    }
    buf.append(']');
  }

  public static Object subarray(Object srcArray, int beginIndex, int endIndex)
  {
    int count = Array.getLength(srcArray)-1;
    if (beginIndex < 0)
    {
      throw new ArrayIndexOutOfBoundsException(beginIndex);
    }
    if (endIndex > count)
    {
      throw new ArrayIndexOutOfBoundsException(endIndex);
    }
    int newLength = endIndex-beginIndex+1;
    if (newLength<=0)
    {
      throw new ArrayIndexOutOfBoundsException(endIndex - beginIndex);
    }
    if(beginIndex==0 && endIndex==count)
      return srcArray;

    Object newArray = Array.newInstance(srcArray.getClass().getComponentType(), newLength);
    System.arraycopy(srcArray, beginIndex, newArray, 0, newLength);
    return newArray;
  }


  /**
   * Returns a class for the array of the componentType with a given dimensions.
   */
  public static Class<?> getArrayClass(Class<?> componentType, int dimensions)
  {
    if(dimensions == 0)
      return componentType;

    int dims[] = new int[dimensions];
    return Array.newInstance(componentType, dims).getClass();
  }


  public static String[] tokens(String toparse, String delim)
  {
    StringTokenizer st = new StringTokenizer(toparse, delim);
    Vector v = new Vector(10);

    while(st.hasMoreTokens())
      v.add(st.nextToken());

    return (String[]) v.toArray(new String[v.size()]);

  }

  public static boolean equals(Object obj1, Object obj2)
  {
    if (obj1 == null || obj2 == null)
      return obj1 == obj2;

    Class<?> c1 = obj1.getClass();
    Class<?> c2 = obj2.getClass();
    if (c1 != c2)
      return false;

    if (c1.isArray())
    {
      int size1 = size(obj1);
      int size2 = size(obj2);

      if (size1 != size2)
        return false;
      for (int i = 0; i < size1; i++)
      {
        if (!equals(Array.get(obj1, i), Array.get(obj2, i)))
          return false;
      }

      return true;
    }
    return obj1.equals(obj2);
  }
  
  public static int dimensionOfArray(Object ary, Class<?> baseClass)
  {
  	Class<?> aryClass = ary.getClass();
  	
  	int dim = 0;
  	while( aryClass != baseClass && aryClass.isArray())
  	{
  		aryClass = aryClass.getComponentType();
  		++dim;
  	}
  	
  	return aryClass == baseClass ? dim : -1;
  }
  

}
