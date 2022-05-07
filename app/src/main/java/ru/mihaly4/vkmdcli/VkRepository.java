package ru.mihaly4.vkmdcli;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class VkRepository {
    private static final int AUDIO_LIMIT = 50;
    private static final int WALL_LIMIT = 5;
    private static final long DDOS_DELAY = 1000;
    private static final int AUDIO_IDS_CHUNK_SIZE = 3;

    @Nonnull
    private final VkClient client;
    @Nonnull
    private final VkAudioLinkDecoder linkDecoder;
    @Nonnull
    private final ILogger logger;

    public VkRepository(
            @Nonnull VkClient client,
            @Nonnull VkAudioLinkDecoder linkDecoder,
            @Nonnull ILogger logger
    ) {
        this.client = client;
        this.linkDecoder = linkDecoder;
        this.logger = logger;
    }

    @Nonnull
    public Map<String, String[]> findAllByWall(@Nonnull String wallId) {
        return findAll(page -> client.fromWall(wallId, WALL_LIMIT * page), ".wi_body .audio_item");
    }

    @Nonnull
    public Map<String, String[]> findAllByAudio(int ownerId, boolean my) {
        return findAll(page -> client.fromAudio(ownerId, AUDIO_LIMIT * page, my), ".AudioPlaylistRoot.AudioBlock__content .audio_item");
    }

    @Nonnull
    private Map<String, String[]> findAll(@Nonnull IFetcher fetcher, @Nonnull String selector) {
        final Map<String, String[]> links = new HashMap<>();
        int oldLinkSize = 0;
        int page = 0;

        do {
            logger.println(String.format("Page: %d, Total links: %d", page + 1, links.size()));

            oldLinkSize = links.size();

            Document doc = Jsoup.parse(fetcher.fetch(page++));

            Elements tracks = doc.select(selector);

            final ArrayList<String> audioIds = new ArrayList<>();

            for (int i = 0; i != tracks.size(); i++) {
                Element track = tracks.get(i);
                String audioData = track.dataset().getOrDefault("audio", "[]");

                String audioId = VkHelper.compileAudioId(audioData);

                if (!audioId.isEmpty()) {
                    audioIds.add(audioId);
                }
            }

            List<List<String>> partedAudioIds = partitionList(audioIds, AUDIO_IDS_CHUNK_SIZE);

            for (int j = 0; j != partedAudioIds.size(); j++) {
                String reloadAudioResponse = client.reloadAudio(partedAudioIds.get(j).toArray(new String[]{}));

                try {
                    JSONObject reloadAudioData = new JSONObject(reloadAudioResponse);
                    JSONArray audioUrls = reloadAudioData.getJSONArray("data").getJSONArray(0);
                    for (int i = 0; i != audioUrls.length(); i++) {
                        JSONArray audioUrl = audioUrls.optJSONArray(i);

                        String author = audioUrl.optString(4);
                        String title = audioUrl.optString(3);
                        String link = audioUrl.optString(2);

                        // decode api_unavailable
                        link = linkDecoder.decode(link);

                        links.put(link, new String[]{author, title});
                    }
                } catch (JSONException e) {
                    logger.error("INVALID JSON: " + e.getMessage());
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(DDOS_DELAY);
                } catch (InterruptedException e) {
                    logger.error("DDOS DELAY: " + e.getMessage());
                }
            }

            try {
                TimeUnit.MILLISECONDS.sleep(DDOS_DELAY);
            } catch (InterruptedException e) {
                logger.error("DDOS DELAY: " + e.getMessage());
            }
        } while (oldLinkSize != links.size());

        logger.println(String.format("Total links: %d", links.size()));

        return links;
    }

    @SuppressWarnings("SameParameterValue")
    @Nonnull
    private <T> List<List<T>> partitionList(@Nonnull List<T> list, int chunkSize) {
        List<List<T>> partitionList = new ArrayList<>();
        List<T> chunkList = new ArrayList<>();
        int current = 0;
        for (T value : list) {
            chunkList.add(value);
            if (++current >= chunkSize) {
                partitionList.add(new ArrayList<>(chunkList));
                chunkList.clear();
                current = 0;
            }
        }
        if (current != 0) {
            partitionList.add(new ArrayList<>(chunkList));
        }
        return partitionList;
    }

    private interface IFetcher {
        @Nonnull
        String fetch(int page);
    }
}
