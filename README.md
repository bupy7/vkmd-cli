# vkmd-cli

A lightweight CLI music downloader for VK.com.

## Requirements

- Java >= 8.0;
- Linux or macOS;

## M3U8 to MP3

```
ffmpeg -http_persistent false -i link.m3u8 -c copy audio.mp3
```

## License

vkmd-cli is released under the BSD-3-Clause License. See the bundled LICENSE.txt for details.
