# vkmd-cli

A lightweight CLI music downloader for VK.com.

## Requirements

- Java >= 8.0;
- Linux or macOS;

## Usage

First of all, copy VK.com cookies from site and put into some file, example "~/vkcom-cookies.txt".

Run:

```bash
java -jar vkmd-cli-1.0.0.jar --vk-cookies="~/vkcom-cookies.txt" --vk-uid=100200300 https://vk.com/retrowavetouch
```

- **--vk-cookies** - Path to the file of VK.com cookies;
- **--vk-uid** - VK.com user ID;
- **https://vk.com/retrowavetouch** - URL of page with audios.

## M3U8 to MP3

```
ffmpeg -http_persistent false -i link.m3u8 -c copy audio.mp3
```

## License

vkmd-cli is released under the BSD-3-Clause License. See the bundled LICENSE.txt for details.
