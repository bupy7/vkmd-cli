# vkmd-cli

A lightweight CLI music downloader for VK.com.

## Requirements

- Java >= 8.0;
- [FFmpeg](https://www.ffmpeg.org/);
- Linux or macOS;

> Docker image of this app has everything out of box and will works even on Windows OS.

## Usage

```
vkmd-cli [OPTIONS] <URL>

    --ffmpeg <arg>       Path to the FFmpeg bin. By default will search
                         automatically.
 -h,--help               Show help of application.
    --save-dir <arg>     Path to the saving directory where we will put
                         downloaded audio files.
    --vk-cookies <arg>   Path to the file contains VK.com cookies. Format
                         file is (without quotes): "cookie_key1=cookie_value1;
                         cookie_key2=cookie_value2; cookie_key3=cookie_value"
    --vk-uid <arg>       VK.com user ID.
```

## Run

First of all, create `~/vkmd-cli` directory and [copy VK.com cookies from site](#how-to-copy-vkcom-cookies-correctly) into file `~/vkmd-cli/vk-cookies.txt`.

**Using JRE:**

Download compiled application:

```bash
wget -O ./vkmd-cli.jar [@TODO URL of jar file]
```

```bash
java -jar vkmd-cli.jar https://vk.com/retrowavetouch \
  --vk-cookies=~/vkmd-cli/vk-cookies.txt \
  --vk-uid=100200300 \
  --save-dir=~/vkmd-cli
```

- **--vk-cookies** - Path to the file of VK.com cookies;
- **--vk-uid** - VK.com user ID;
- **--save-dir** - Path to save downloaded audios;
- **https://vk.com/retrowavetouch** - URL of page with audios.

**Using Docker:**

Clone repository:

```bash
git clone https://github.com/bupy7/vkmd-cli.git
cd vkmd-cli
git checkout 1.0.0
```

Build Docker image:

```bash
docker build -t bupy7/vkmd-cli .
```

Run Docker container:

```bash
docker run --rm -it -v ~/vkmd-cli:/vkmd-cli bupy7/vkmd-cli https://vk.com/retrowavetouch \
  --vk-cookies=~/vkmd-cli/vk-cookies.txt \
  --vk-uid=100200300 \
  --save-dir=~/vkmd-cli
```

## How to copy VK.com cookies correctly

You have to extract VK.com cookies from first request of any page. You may do that using Developer Tools in Chrome, Safari, Mozilla and other browsers.

> Notice! Extracting cookies using `document.cookie` is not correctly, due to there are non HTTP cookies only.

**Instruction for Chrome and any Chromium like browsers:**

1. Open https://vk.com and log in.
2. Open "Developer tools" (macOS: Command + Shift + I; PC: Ctrl + Shift + I).
3. Refresh current page.
4. Choose "Network" tab in "Developer tools" panel.
5. Filter results in the tab using "Doc" request type.
6. Choose first request and choose "Header" tab in the right section.
7. Go to "Request Headers", in the right section, and find "cookie:" header.
8. Copy all text between "cookie: " and next header name.
9. Put this text into `~/vkmd-cli/vk-cookies.txt` file and save.

## License

vkmd-cli is released under the BSD-3-Clause License. See the bundled LICENSE.txt for details.
