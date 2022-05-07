package ru.mihaly4.vkmdcli;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final public class VkClient {
    private static final String BASE_AUDIO_URL = "https://m.vk.com/audios";
    private static final String BASE_RELOAD_AUDIO_URL = "https://m.vk.com/audio";
    private static final String BASE_WALL_URL = "https://m.vk.com/";
    private static final String USER_AGENT = "Mozilla/5.0"
        + " (iPhone; CPU iPhone OS 12_1 like Mac OS X)"
        + " AppleWebKit/605.1.15"
        + " (KHTML, like Gecko)"
        + " Version/12.0 Mobile/15E148 Safari/604.1";
    private static final int MAX_RELOAD_AUDIO_COUNT = 3;

    @Nonnull
    private final OkHttpClient httpClient;

    public VkClient(@Nonnull Map<String, String> vkCookies) {
        httpClient = createHttpClient(vkCookies);
    }

    /**
     * @param audioIds Maximum size of items is 3.
     * @return JSON-string
     */
    @Nonnull
    public String reloadAudio(@Nonnull String[] audioIds) {
        FormBody.Builder requestBodyBuilder = new FormBody.Builder()
                .add("act", "reload_audio")
                .add("ids", Arrays.stream(audioIds)
                        .limit(MAX_RELOAD_AUDIO_COUNT)
                        .collect(Collectors.joining(","))
                );
        String json = "";
        Request request;
        try {
            request = new Request.Builder()
                    .url(BASE_RELOAD_AUDIO_URL)
                    .post(requestBodyBuilder.build())
                    .build();
        } catch (IllegalArgumentException e) {
            return json;
        }

        Response response;
        try {
            response = httpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                json = response.body().string();
            }
        } catch (IOException | NullPointerException e) {
            // nothing
        }

        return json;
    }

    @Nonnull
    public String fromAudio(int ownerId, int offset, boolean my) {
        Request request = new Request.Builder()
                .url(BASE_AUDIO_URL + ownerId + "?offset=" + offset + (my ? "&section=my" : ""))
                .get()
                .build();
        Response response;
        String html = "";
        try {
            response = httpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                html = response.body().string();
            }
        } catch (IOException | NullPointerException e) {
            // nothing
        }

        return html;
    }

    @Nonnull
    public String fromWall(@Nonnull String wallId, int offset) {
        Request request = new Request.Builder()
                .url(BASE_WALL_URL + wallId + "?offset=" + offset)
                .get()
                .build();
        Response response;
        String html = "";
        try {
            response = httpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                html = response.body().string();
            }
        } catch (IOException | NullPointerException e) {
            // nothing
        }

        return html;
    }

    private OkHttpClient createHttpClient(final Map<String, String> vkCookies) {
        return new OkHttpClient()
                .newBuilder()
                .addNetworkInterceptor(chain -> {
                    Request originalRequest = chain.request();
                    Request requestWithUserAgent = originalRequest.newBuilder()
                            .header("User-Agent", USER_AGENT)
                            .build();
                    return chain.proceed(requestWithUserAgent);
                })
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(@Nonnull HttpUrl url, @Nonnull List<Cookie> cookies) {
                        // does nothing
                    }

                    @Nonnull
                    @Override
                    public List<Cookie> loadForRequest(@Nonnull HttpUrl url) {
                        List<Cookie> result = new ArrayList<>();
                        vkCookies.forEach((name, value) -> result.add(new Cookie.Builder()
                                .name(name)
                                .domain("vk.com")
                                .value(value)
                                .build()
                        ));
                        return result;
                    }
                })
                .build();
    }
}
