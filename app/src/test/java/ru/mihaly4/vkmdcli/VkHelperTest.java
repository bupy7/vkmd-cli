package ru.mihaly4.vkmdcli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class VkHelperTest {
    @Test
    public void compileIdsToReloadAudio() {
        String audioDataJson = "[456239180,520969309,\"\",\"&quot;Сотрудник банка&quot; с табельным номером\",\"Александр\",121,0,0,\"\",0,2,\"my:my_audios\",\"[]\",\"e3f5c888f81141e614\\/c393a969ec445ea9e3\\/8479df88e3ca4a26be\\/8fd54cbf0be3f60f93\\/\\/324bd8a29b879da5a6\\/3981b41c6de1a0925f\",\"\",{\"duration\":121,\"content_id\":\"520969309_456239180\",\"puid22\":11,\"account_age_type\":2,\"_SITEID\":276,\"vk_id\":520969309,\"ver\":251116},\"\",\"\",\"\",false,\"9d00c5212bkZhFVA1dbjzwqwWpZydpzWYYSKqkwHtam--W8_qfiN6zWrOEzjsr7PDqA3n3J2nJ9l454rCyXXDwub_TTROvnqv-M7g4Q-az48oOiQmGdxCMjmPihqJIZPCI1JF6nvr7vFJI3hgIM\",0,0,true,\"\",false]";
        String audioReloadId = VkHelper.compileAudioId(audioDataJson);
        assertEquals("520969309_456239180_8479df88e3ca4a26be_324bd8a29b879da5a6", audioReloadId);

        audioDataJson = "[456244298,2000425578,\"\",\"Неважно\",\"Буерак\",151,2,0,\"\",0,2,\"group_wall:-72000629_60275\",\"[]\",\"10e5f20b7a76ee993e9\\/\\/96e97f49db4f4ca1c9\\/\\/\\/ea409540f1e8613342\\/\",\"\",{\"duration\":151,\"content_id\":\"2000425578_456244298\",\"puid22\":11,\"account_age_type\":3,\"_SITEID\":276,\"vk_id\":520969309,\"ver\":251116},\"\",\"\",\"\",false,\"f817267bcCSv9RefMi-a_u6JtapGq5MXwp6ri3CoNhzcIYt08kr_3-A83k613ojuA\",0,0,true,\"4353c4a0c05971c4a06\",false,\"\"]";
        audioReloadId = VkHelper.compileAudioId(audioDataJson);
        assertEquals("2000425578_456244298_96e97f49db4f4ca1c9_ea409540f1e8613342", audioReloadId);
    }

    @Test
    public void parseTarget() throws VkHelper.TargetException {
        VkHelper.Target target = VkHelper.parseTarget("https://vk.com/feed");
        assertNotNull(target);
        assertEquals("feed", target.getValue());
        assertEquals(VkHelper.Target.Type.WALL, target.getType());

        target = VkHelper.parseTarget("https://vk.com/id1");
        assertNotNull(target);
        assertEquals("id1", target.getValue());
        assertEquals(VkHelper.Target.Type.WALL, target.getType());

        target = VkHelper.parseTarget("https://vk.com/audios876");
        assertNotNull(target);
        assertEquals("876", target.getValue());
        assertEquals(VkHelper.Target.Type.AUDIO, target.getType());

        target = VkHelper.parseTarget("https://vk.com/audios-876");
        assertNotNull(target);
        assertEquals("-876", target.getValue());
        assertEquals(VkHelper.Target.Type.AUDIO, target.getType());

        assertThrowsExactly(VkHelper.TargetException.class, () -> VkHelper.parseTarget("https://vk.ru/invalid"));
    }

    @Test
    public void decodeHtmlSpecialChars() {
        assertEquals("&&\"\"''<<>>", VkHelper.decodeHtmlSpecialChars("&&amp;\"&quot;'&#039;<&lt;>&gt;"));
        assertEquals(
                "abc&&\"\"''<<>>123",
                VkHelper.decodeHtmlSpecialChars("abc&&amp;\"&quot;'&#039;<&lt;>&gt;123")
        );
    }

    @Test
    public void sanitizeFileName() {
        assertEquals(
                "DEAD BLONDE_ GSPD - Первая дискотека",
                VkHelper.sanitizeFileName("DEAD BLONDE, GSPD - Первая дискотека")
        );
        assertEquals(
                "Эдуард Артемьев - Музыка из к_ф _Курьер_",
                VkHelper.sanitizeFileName("Эдуард Артемьев - Музыка из к/ф \"Курьер\"")
        );
        assertEquals(
                "Ария - Тореро _Remastered 2012_",
                VkHelper.sanitizeFileName("Ария - Тореро (Remastered 2012)")
        );
        assertEquals(
                "_1234567890-_________________________________",
                VkHelper.sanitizeFileName("§1234567890-=±!@#$%^&*()_+][}{'\"\\/;:<>~`№₽?.,")
        );
    }
}
