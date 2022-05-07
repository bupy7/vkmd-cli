package ru.mihaly4.vkmdcli;

import java.lang.reflect.Array;

/**
 * @see <a href="https://stackoverflow.com/a/42259988">Java implementationf of the Array.prototype.splice()</a>
 */
public final class JsArrayUtil {
    public static <T>T[] splice(final T[] array, int start) {
        if (start < 0) {
            start += array.length;
        }

        return splice(array, start, array.length - start);
    }

    @SuppressWarnings("unchecked")
    public static <T>T[] splice(final T[] array, int start, final int deleteCount) {
        if (start < 0) {
            start += array.length;
        }

        final T[] spliced = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - deleteCount);
        if (start != 0) {
            System.arraycopy(array, 0, spliced, 0, start);
        }

        if (start + deleteCount != array.length) {
            System.arraycopy(array, start + deleteCount, spliced, start, array.length - start - deleteCount);
        }

        return spliced;
    }

    @SuppressWarnings("unchecked")
    public static <T>T[] splice(final T[] array, int start, final int deleteCount, final T ... items) {
        if (start < 0)
            start += array.length;

        final T[] spliced = (T[]) Array.newInstance(
                array.getClass().getComponentType(),
                array.length - deleteCount + items.length
        );
        if (start != 0) {
            System.arraycopy(array, 0, spliced, 0, start);
        }

        if (items.length > 0) {
            System.arraycopy(items, 0, spliced, start, items.length);
        }

        if (start + deleteCount != array.length) {
            System.arraycopy(
                    array,
                    start + deleteCount,
                    spliced,
                    start + items.length,
                    array.length - start - deleteCount
            );
        }

        return spliced;
    }
}
