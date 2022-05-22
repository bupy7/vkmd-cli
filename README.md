# vkmd-cli

A lightweight CLI music downloader for VK.com.

## Requirements

- Java >= 8.0;
- Linux or macOS;

## Usage

```
vkmd-cli [OPTIONS] <URL>

    --ffmpeg <arg>       Path to the FFmpeg bin. By default will search
                         automatically.
 -h,--help               Show help of application.
    --save-dir <arg>     Path to the saving directory where we will put
                         downloaded audio files.
    --vk-cookies <arg>   Path to the file contains VK.com cookies.
    --vk-uid <arg>       VK.com user ID.
```

## Run

First of all, copy VK.com cookies from site and put into some file, example "~/vkcom-cookies.txt".

**Using JRE:**

```bash
java -jar vkmd-cli-1.0.0.jar https://vk.com/retrowavetouch \
  --vk-cookies="~/vkcom-cookies.txt" \
  --vk-uid=100200300 \
  --save-dir=~/vkmd-cli-downloads
```

- **--vk-cookies** - Path to the file of VK.com cookies;
- **--vk-uid** - VK.com user ID;
- **--save-dir** - Path to save downloaded audios;
- **https://vk.com/retrowavetouch** - URL of page with audios.

**Using Docker:**

TODO

## License

vkmd-cli is released under the BSD-3-Clause License. See the bundled LICENSE.txt for details.
