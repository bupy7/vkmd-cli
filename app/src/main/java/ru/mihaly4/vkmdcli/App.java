package ru.mihaly4.vkmdcli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class App {
    @Nonnull
    CliOutput output = new CliOutput();
    @Nonnull
    private Map<String, String> vkCookies = new HashMap<>();
    @Nonnull
    private String ffmpeg = "";
    @Nonnull
    private String saveDir = "";

    public App(String[] args) {
        run(args);
    }

    public static void main(String[] args) {
        new App(args);
    }

    private void run(String[] args) {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            output.error("This app only works on Unix compatibility systems.");
            System.exit(1);
        }

        AppArgs appArgs = new AppArgs(args);
        if (appArgs.isHelp()) {
            appArgs.printHelp(output);
            System.exit(0);
        }
        try {
            appArgs.load();
            loadVkCookies(appArgs.getVkCookies());
            detectFfmpeg(appArgs.getFfmpeg());
            detectSaveDir(appArgs.getSaveDir());
        } catch (AppException e) {
            output.error(e.getMessage());
            System.exit(1);
        }

        VkRepository vkRepository = new VkRepository(
                new VkClient(vkCookies),
                new VkAudioLinkDecoder(appArgs.getVkUid()),
                output
        );

        // grabbing
        VkHelper.Target urlTarget = null;
        try {
            urlTarget = VkHelper.parseTarget(appArgs.getUrl());
        } catch (VkHelper.TargetException e) {
            output.error(e.getMessage());
            System.exit(1);
        }
        Map<String, String[]> links = null;
        if (urlTarget.getType() == VkHelper.Target.Type.AUDIO) {
            int ownerId = Integer.parseInt(urlTarget.getValue());
            output.println(String.format("> Grabbing from \"%d\"...", ownerId));
            links = vkRepository.findAllByAudio(ownerId, ownerId == appArgs.getVkUid());
        } else if (urlTarget.getType() == VkHelper.Target.Type.WALL) {
            output.println(String.format("> Grabbing from \"%s\"...", urlTarget.getValue()));
            links = vkRepository.findAllByWall(urlTarget.getValue());
        } else {
            output.error("Unsupported VK.com URL.");
            System.exit(1);
        }
        output.println("Result:");
        links.forEach((url, meta) -> output.println(meta[0] + " - " + meta[1] + "\n" + url + "\n"));

        // downloading
        output.println("");
        if (links.size() > 0) {
            output.println("> Downloading...");
            try {
                downloadAudios(links);
            } catch (AppException e) {
                output.error(e.getMessage());
                System.exit(1);
            }
        } else {
            output.println("Nothing to download.");
        }

        output.println("Done.");
    }

    private void loadVkCookies(@Nonnull String cookieFile) throws AppException {
        StringBuilder cookieBuilder;
        try {
            //noinspection IOStreamConstructor
            BufferedReader cookieBufferedReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(cookieFile)
            ));
            cookieBuilder = new StringBuilder();
            while (cookieBufferedReader.ready()) {
                cookieBuilder.append(cookieBufferedReader.readLine());
            }
            cookieBufferedReader.close();
        } catch (IOException e) {
            throw new AppException(e.getMessage());
        }

        String[] rawCookies = cookieBuilder.toString().split(";\\s*");
        if (rawCookies.length == 0) {
            throw new AppException("VK.com Cookies is empty.");
        }

        vkCookies = new HashMap<>();
        for (int i = 0; i != rawCookies.length; i++) {
            String[] cookie = rawCookies[i].split("=", 2);
            if (cookie.length < 2) {
                continue;
            }
            vkCookies.put(cookie[0].trim(), cookie[1].trim());
        }
    }

    private void downloadAudios(@Nonnull Map<String, String[]> links) throws AppException {
        output.println(String.format("FFmpeg: %s", ffmpeg));

        final int totalLinks = links.size();
        int currentLink = 0;
        for (Map.Entry<String, String[]> link : links.entrySet()) {
            String audioName = link.getValue()[0] + " - " + link.getValue()[1];

            output.print(String.format("%d/%d. %s", ++currentLink, totalLinks, audioName + "..."));

            try {
                // M3U8 to MP3
                // ffmpeg -y -http_persistent false -i link.m3u8 -c copy audio.mp3
                Process process = Runtime.getRuntime().exec(new String[]{
                        ffmpeg,
                        "-y",
                        "-http_persistent",
                        "false",
                        "-i",
                        link.getKey(),
                        "-c",
                        "copy",
                        String.format("%s/%s.mp3", saveDir, audioName.replaceAll("[^a-zA-Z0-9._\\-]+", "_"))
                });
                if (process.waitFor() != 0) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String error = reader.lines().collect(Collectors.joining());
                    output.println("ERROR");
                    if (error.isEmpty()) {
                        output.error("Unknown error has occurred");
                    } else {
                        output.error(error);
                    }
                }
                output.println("OK");
            } catch (IOException | InterruptedException e) {
                throw new AppException(e.getMessage());
            }
        }
    }

    private void detectFfmpeg(@Nonnull String ffmpeg) throws AppException {
        if (!ffmpeg.isEmpty()) {
            if (!ffmpeg.startsWith("/")) {
                throw new AppException("Invalid path to the FFmpeg bin.");
            }
            this.ffmpeg = ffmpeg;
            return;
        }

        Process process;
        int exitCode;
        try {
            process = Runtime.getRuntime().exec(new String[]{"which", "ffmpeg"});
            exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new AppException(e.getMessage());
        }

        if (exitCode != 0) {
            throw new AppException("Cannot detect FFmpeg automatically.");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String whichResult;
        try {
            whichResult = reader.readLine();
            reader.close();
        } catch (IOException e) {
            throw new AppException(e.getMessage());
        }

        if (!whichResult.startsWith("/")) {
            throw new AppException("Cannot detect FFmpeg automatically.");
        }

        this.ffmpeg = whichResult;
    }

    private void detectSaveDir(@Nonnull String saveDir) throws AppException {
        if (!saveDir.startsWith("/")) {
            throw new AppException("Invalid path to the saving directory.");
        }

        File saveDirFile = new File(saveDir);
        if (!saveDirFile.exists()) {
            if (!saveDirFile.mkdirs()) {
                throw new AppException("Cannot create saving directory.");
            }
        } else {
            if (!saveDirFile.isDirectory()) {
                throw new AppException("The saving directory is not directory.");
            }
            if (!saveDirFile.canWrite()) {
                throw new AppException("The saving directory is not writable.");
            }
        }

        this.saveDir = saveDir;
    }

    private final static class AppArgs {
        @Nullable
        private CommandLine cli;
        @Nonnull
        private final String[] args;

        public AppArgs(@Nonnull String[] args) {
            this.args = args;
        }

        public int getVkUid() {
            if (cli == null) {
                return 0;
            }
            return Integer.parseInt(cli.getOptionValue("vk-uid"));
        }

        @Nonnull
        public String getUrl() {
            if (cli == null || cli.getArgs().length < 1) {
                return "";
            }
            return cli.getArgs()[0];
        }

        public boolean isHelp() {
            return args.length == 0 || (args.length == 1 && (args[0].equals("--help") || args[0].equals("-h")));
        }

        @Nonnull
        public String getVkCookies() {
            if (cli == null || cli.getArgs().length < 1) {
                return "";
            }
            return cli.getOptionValue("vk-cookies") != null
                    ? normalizePath(cli.getOptionValue("vk-cookies"))
                    : "";
        }

        @Nonnull
        public String getFfmpeg() {
            if (cli == null || cli.getArgs().length < 1) {
                return "";
            }
            return cli.getOptionValue("ffmpeg") != null
                    ? normalizePath(cli.getOptionValue("ffmpeg"))
                    : "";
        }

        @Nonnull
        public String getSaveDir() {
            if (cli == null || cli.getArgs().length < 1) {
                return "";
            }
            return cli.getOptionValue("save-dir") != null
                    ? normalizePath(cli.getOptionValue("save-dir"))
                    : "";
        }

        public void load() throws AppException {
            try {
                cli = new DefaultParser().parse(createOptions(), args);
            } catch (ParseException e) {
                throw new AppException(e.getMessage());
            }
        }

        public void printHelp(@Nonnull CliOutput output) {
            HelpFormatter help = new HelpFormatter();
            PrintWriter pw = new PrintWriter(output.getStream());
            help.printHelp(
                    pw,
                    help.getWidth(),
                    "[OPTIONS] <URL>",
                    help.getNewLine()
                            + "A lightweight CLI music downloader for VK.com."
                            + help.getNewLine()
                            + help.getNewLine(),
                    createOptions(),
                    help.getLeftPadding(),
                    help.getDescPadding(),
                    help.getNewLine(),
                    false
            );
            pw.flush();
        }

        @Nonnull
        private Options createOptions() {
            Options options = new Options();

            options.addRequiredOption(
                    null,
                    "vk-cookies",
                    true,
                    "Path to the file contains VK.com cookies."
                            + " Format file is: cookie_key1=cookie_value1; cookie_key2=cookie_value2;"
                            + " cookie_key3=cookie_value3 ... (and so on)."
            );
            options.addRequiredOption(null, "vk-uid", true, "VK.com user ID.");
            options.addRequiredOption(
                    null,
                    "save-dir",
                    true,
                    "Path to the saving directory where we will put downloaded audio files.");

            options.addOption("h", "help", false, "Show help of application.");
            options.addOption(
                    null,
                    "ffmpeg",
                    true,
                    "Path to the FFmpeg bin. By default will search automatically."
            );

            return options;
        }

        @Nonnull
        private String normalizePath(@Nonnull String path) {
            return path.replaceFirst("^~", System.getProperty("user.home"));
        }
    }
}
