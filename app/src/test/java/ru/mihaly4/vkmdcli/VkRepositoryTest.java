package ru.mihaly4.vkmdcli;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public final class VkRepositoryTest {
    @SuppressWarnings("unchecked")
    @Test
    public void partitionList() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        VkRepository vkRepository = new VkRepository(
                new VkClient(new HashMap<>()),
                new VkAudioLinkDecoder(0),
                new CliOutput()
        );

        Method method = vkRepository.getClass().getDeclaredMethod("partitionList", List.class, int.class);
        method.setAccessible(true);

        List<String> list = new ArrayList<>();
        List<List<String>> actual = (List<List<String>>) method.invoke(vkRepository, list, 3);
        assertEquals(0, actual.size());

        //noinspection ArraysAsListWithZeroOrOneArgument
        list = Arrays.asList("0");
        actual = (List<List<String>>) method.invoke(vkRepository, list, 3);
        assertEquals(1, actual.size());
        assertEquals("0", actual.get(0).get(0));

        list = Arrays.asList("0", "1");
        actual = (List<List<String>>) method.invoke(vkRepository, list, 3);
        assertEquals(1, actual.size());
        assertArrayEquals(new String[]{"0", "1"}, actual.get(0).toArray(new String[]{}));

        list = Arrays.asList("0", "1", "2");
        actual = (List<List<String>>) method.invoke(vkRepository, list, 3);
        assertEquals(1, actual.size());
        assertArrayEquals(new String[]{"0", "1", "2"}, actual.get(0).toArray(new String[]{}));

        list = Arrays.asList("0", "1", "2", "3");
        actual = (List<List<String>>) method.invoke(vkRepository, list, 3);
        assertEquals(2, actual.size());
        assertArrayEquals(new String[]{"0", "1", "2"}, actual.get(0).toArray(new String[]{}));
        assertArrayEquals(new String[]{"3"}, actual.get(1).toArray(new String[]{}));

        list = Arrays.asList("0", "1", "2", "3", "4", "5");
        actual = (List<List<String>>) method.invoke(vkRepository, list, 3);
        assertEquals(2, actual.size());
        assertArrayEquals(new String[]{"0", "1", "2"}, actual.get(0).toArray(new String[]{}));
        assertArrayEquals(new String[]{"3", "4", "5"}, actual.get(1).toArray(new String[]{}));

        list = Arrays.asList("0", "1", "2", "3", "4", "5", "6");
        actual = (List<List<String>>) method.invoke(vkRepository, list, 3);
        assertEquals(3, actual.size());
        assertArrayEquals(new String[]{"0", "1", "2"}, actual.get(0).toArray(new String[]{}));
        assertArrayEquals(new String[]{"3", "4", "5"}, actual.get(1).toArray(new String[]{}));
        assertArrayEquals(new String[]{"6"}, actual.get(2).toArray(new String[]{}));

        list = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
        actual = (List<List<String>>) method.invoke(vkRepository, list, 3);
        assertEquals(4, actual.size());
        assertArrayEquals(new String[]{"0", "1", "2"}, actual.get(0).toArray(new String[]{}));
        assertArrayEquals(new String[]{"3", "4", "5"}, actual.get(1).toArray(new String[]{}));
        assertArrayEquals(new String[]{"6", "7", "8"}, actual.get(2).toArray(new String[]{}));
        assertArrayEquals(new String[]{"9"}, actual.get(3).toArray(new String[]{}));
    }
}
