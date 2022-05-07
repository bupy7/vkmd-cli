package ru.mihaly4.vkmdcli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public final class App {
    @Nonnull
    private final static String APP_NAME = "vkmd-cli";

    CliOutput output = new CliOutput();

    public App(String[] args) throws IOException {
        String newline = System.lineSeparator();
        if (!newline.equals("\n") && !newline.equals("\r")) {
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
        } catch (ParseException e) {
            output.error(e.getMessage());
            System.exit(1);
        }
        if (appArgs.getUrl().isEmpty()) {
            output.error("Missing required argument: URL");
            System.exit(1);
        }

        VkRepository vkRepository = new VkRepository(
                new VkClient(appArgs.getVkCookies()),
                new VkAudioLinkDecoder(appArgs.getVkUid()),
                output
        );

        VkHelper.Target urlTarget = null;
        try {
            urlTarget = VkHelper.parseTarget(appArgs.getUrl());
        } catch (VkHelper.TargetException e) {
            output.error(e.getMessage());
            System.exit(1);
        }
        Map <String, String[]> links = null;
        if (urlTarget.getType() == VkHelper.Target.Type.AUDIO) {
            int ownerId = Integer.parseInt(urlTarget.getValue());
            output.println(String.format("Grabbing from \"%d\"", ownerId));
            links = vkRepository.findAllByAudio(ownerId, ownerId == appArgs.getVkUid());
        } else if (urlTarget.getType() == VkHelper.Target.Type.WALL) {
            output.println(String.format("Grabbing from \"%s\"", urlTarget.getValue()));
            links = vkRepository.findAllByWall(urlTarget.getValue());
        } else {
            output.error("Unsupported VK.com URL.");
            System.exit(1);
        }

        output.println("RESULT:");
        links.forEach((url, meta) -> output.println(meta[0] + "\n" + meta[1] + "\n" + url + "\n"));
    }

    public static void main(String[] args) throws Exception {
        new App(args);
    }

    private final static class AppArgs {
        @Nullable
        private CommandLine cli;
        @Nonnull
        private Map<String, String> vkCookies = new HashMap<>();
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
        public Map<String, String> getVkCookies() {
            return vkCookies;
        }

        public void load() throws ParseException, IOException {
            cli = new DefaultParser().parse(createOptions(), args);
            loadVkCookies(cli.getOptionValue("vk-cookies") != null ? cli.getOptionValue("vk-cookies") : "");
        }

        public void printHelp(@Nonnull CliOutput output) {
            HelpFormatter help = new HelpFormatter();
            PrintWriter pw = new PrintWriter(output.getStream());
            help.printHelp(
                    pw,
                    help.getWidth(),
                    APP_NAME + " [OPTIONS] <URL>",
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

        private void loadVkCookies(@Nonnull String cookieFile) throws IOException {
            //noinspection IOStreamConstructor
            BufferedReader cookieBufferedReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(cookieFile.replaceFirst("^~", System.getProperty("user.home")))
            ));
            StringBuilder cookieBuilder = new StringBuilder();
            while (cookieBufferedReader.ready()) {
                cookieBuilder.append(cookieBufferedReader.readLine());
            }
            cookieBufferedReader.close();

            String[] rawCookies = cookieBuilder.toString().split(";\\s*");
            vkCookies = new HashMap<>();
            for (int i = 0; i != rawCookies.length; i++) {
                String[] cookie = rawCookies[i].split("=", 2);
                if (cookie.length < 2) {
                    continue;
                }
                vkCookies.put(cookie[0].trim(), cookie[1].trim());
            }
        }

        @Nonnull
        private Options createOptions() {
            Options options = new Options();

            options.addRequiredOption(
                    null,
                    "vk-cookies",
                    true,
                    "Path to the file contains VK.com cookies."
            );
            options.addRequiredOption(null, "vk-uid", true, "VK.com user ID.");

            options.addOption("h", "help", false, "Show help of application.");

            return options;
        }
    }
}
