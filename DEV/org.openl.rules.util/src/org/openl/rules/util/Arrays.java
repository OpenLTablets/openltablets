package org.openl.rules.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A set of util methods to work with arrays.
 *
 * Note: For OpenL rules only! Don't use it in Java code.
 *
 * @author Yury Molchan
 */
public final class Arrays {

    private Arrays() {
        // Utility class
    }

    /**
     * Checks if an array is empty or null.
     *
     * @param array the array to test
     * @return true if the array is empty or null
     */
    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if an array is NOT empty or null.
     *
     * @param array the array to test
     * @return true if the array is NOT empty or null
     */
    public static <T> boolean isNotEmpty(T[] array) {
        return !isEmpty(array);
    }

    public static <T> int length(T[] array) {
        return array == null ? 0 : array.length;
    }

    // ADD

    /**
     * Just concatenates the given elements to the array.
     */
    public static <T> T[] add(T... elements) {
        return elements;
    }

    /**
     * <p>
     * Copies the given array and adds the given elements at the end of the new array.
     * </p>
     * <p/>
     * <p>
     * The new array contains the same elements of the input array plus the given elements in the last position. The
     * component type of the new array is the same as that of the input array.
     * </p>
     * <p/>
     * <p>
     * If the input array is <code>null</code>, a new one element array is returned whose component type is the same as
     * the element, unless the element itself is null, in which case the return type is Object[]
     * </p>
     * <p/>
     *
     * <pre>
     * Arrays.add(null, null)      = [null]
     * Arrays.add(null, "a")       = ["a"]
     * Arrays.add(["a"], null)     = ["a", null]
     * Arrays.add(["a"], "b")      = ["a", "b"]
     * Arrays.add(["a", "b"], "c") = ["a", "b", "c"]
     * </pre>
     *
     * @param array the array to "add" the element to, may be <code>null</code>
     * @param elements the objects to add
     * @return A new array containing the existing elements plus the new elements. The returned array type will be that
     *         of the input array (unless null), in which case it will have the same type as the element.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] add(T[] array, T... elements) {
        return add(array, length(array), elements);
    }

    /**
     * <p>
     * Adds all the elements of the given arrays into a new array.
     * </p>
     * <p>
     * The new array contains all of the element of <code>arrays</code>. When an array is returned, it is always a new
     * array.
     * </p>
     * <p/>
     *
     * <pre>
     * Arrays.add(null, null)     = null
     * Arrays.add(array1, null)   = cloned copy of array1
     * Arrays.add(null, array2)   = cloned copy of array2
     * Arrays.add([], [])         = []
     * Arrays.add([null], [null]) = [null, null]
     * Arrays.add(["a", "b", "c"], ["1", "2", "3"]) = ["a", "b", "c", "1", "2", "3"]
     * </pre>
     *
     * @param arrays the arrays whose elements are added to the new array, may be <code>null</code>
     * @return The new array, <code>null</code> if both arrays are <code>null</code>. The type of the new array is the
     *         same type of the arrays.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] add(T[]... arrays) {
        if (arrays == null) {
            return null;
        }
        Class<?> componentType = arrays.getClass().getComponentType().getComponentType();
        return (T[]) java.util.Arrays.stream(arrays)
            .filter(Objects::nonNull)
            .flatMap(java.util.Arrays::stream)
            .map(componentType::cast)
            .toArray();
    }

    public static <T> T[] addAll(T[] a1, T[] a2) {
        return add(a1, a2);
    }

    public static <T> T[] add(T[] array, T element) {
        return Arrays.<T> add(array, length(array), element);
    }

    /**
     * <p>
     * Inserts the specified element at the specified position in the array. Shifts the element currently at that
     * position (if any) and any subsequent elements to the right (adds one to their indices).
     * </p>
     * <p/>
     * <p>
     * This method returns a new array with the same elements of the input array plus the given element on the specified
     * position. The component type of the returned array is always the same as that of the input array.
     * </p>
     * <p/>
     * <p>
     * If the input array is <code>null</code>, a new one element array is returned whose component type is the same as
     * the element.
     * </p>
     * <p/>
     *
     * <pre>
     * Arrays.add(null, 0, null)      = [null]
     * Arrays.add(null, 0, "a")       = ["a"]
     * Arrays.add(["a"], 1, null)     = ["a", null]
     * Arrays.add(["a"], 1, "b")      = ["a", "b"]
     * Arrays.add(["a", "b"], 3, "c") = ["a", "b", "c"]
     * </pre>
     *
     * @param array the array to add the element to, may be <code>null</code>
     * @param index the position of the new object
     * @param elements the objects to add
     * @return A new array containing the existing elements and the new element
     * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index > array.length).
     */
    public static <T> T[] add(T[] array, int index, T... elements) {
        if (elements == null) {
            return array;
        }
        if (array == null) {
            return elements;
        }
        Class<?> componentType = array.getClass().getComponentType();
        Class<?> commonType = elements.getClass().getComponentType() == componentType ? componentType : Object.class;
        Object result = Array.newInstance(commonType, array.length + elements.length);
        System.arraycopy(array, 0, result, 0, index);
        System.arraycopy(elements, 0, result, index, elements.length);
        System.arraycopy(array, index, result, index + elements.length, array.length - index);

        return (T[]) result;
    }

    // SLICE
    public static <T> T[] slice(T[] values, int startIndexInclusive) {
        return slice(values, startIndexInclusive, Integer.MAX_VALUE);
    }

    public static <T> T[] slice(T[] values, int startIndexInclusive, int endIndexExclusive) {
        @SuppressWarnings("unchecked") // OK, because array is of type T
        T[] subarray = (T[]) subarray(values, startIndexInclusive, endIndexExclusive);
        return subarray;
    }

    /**
     * Removes the first occurrence of the specified element from the specified array. All subsequent elements are
     * shifted to the left (substracts one from their indices). If the array does not contains such an element, no
     * elements are removed from the array. <br />
     * <br />
     * <p/>
     * This method returns a new array with the same elements of the input array except the first occurrence of the
     * specified element. The component type of the returned array is always the same as that of the input array. <br />
     * <br />
     * <p/>
     * <code>
     * ArrayUtils.removeElement(null, "a")            = null        <br />
     * ArrayUtils.removeElement([], "a")              = []          <br />
     * ArrayUtils.removeElement(["a"], "b")           = ["a"]       <br />
     * ArrayUtils.removeElement(["a", "b"], "a")      = ["b"]       <br />
     * ArrayUtils.removeElement(["a", "b", "a"], "a") = ["b", "a"]  <br />
     * </code>
     *
     * @param array the array to remove the element from, may be null
     * @param elements
     * @return the element to be removed
     */
    public static <T, E extends T> T[] removeElement(T[] array, E... elements) {
        if (array == null) {
            return null;
        }
        if (elements == null) {
            return array;
        }
        ArrayList<T> result = new ArrayList<>(java.util.Arrays.asList(array));
        java.util.Arrays.stream(elements).forEach(result::remove);
        return (T[]) result.toArray();
    }

    private static Object subarray(Object array, int beginIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        // handle negatives
        int size = Array.getLength(array);
        if (size == 0) {
            return array;
        }
        if (endIndex < 0) {
            endIndex = size + endIndex; // remember end is negative
        }
        if (beginIndex < 0) {
            beginIndex = size + beginIndex; // remember start is negative

        }

        // check length next
        if (endIndex > size) {
            endIndex = size;
        }

        if (beginIndex < 0) {
            beginIndex = 0;
        }
        if (endIndex < 0) {
            endIndex = 0;
        }

        final int newSize = endIndex - beginIndex;
        if (newSize == size && beginIndex == 0) {
            return array;
        }
        final Class<?> type = array.getClass().getComponentType();
        if (newSize <= 0) {
            return Array.newInstance(type, 0);
        }
        final Object subarray = Array.newInstance(type, newSize);
        System.arraycopy(array, beginIndex, subarray, 0, newSize);
        return subarray;
    }
}
