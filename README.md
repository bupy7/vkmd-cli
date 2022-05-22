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
    --vk-cookies <arg>   Path to the file contains VK.com cookies.
    --vk-uid <arg>       VK.com user ID.
```

## Run

First of all, create `~/vkmd-cli` directory and [copy VK.com cookies from site](#how-to-copy-vkcom-cookies-correctly) into file `~/vkmd-cli/vk-cookies.txt`.

**Using JRE:**

Download compiled application:

```bash
wget -O ./vkmd-cli.jar @TODO
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
git checkout 1.0.0
```

Build Docker image:

```bash
cd vkmd-cli
docker build -t bupy7/vkmd-cli .
```

Run Docker image:

```bash
docker run --rm -it -v ~/vkmd-cli:/vkmd-cli bupy7/vkmd-cli https://vk.com/retrowavetouch \
  --vk-cookies=~/vkmd-cli/vk-cookies.txt \
  --vk-uid=100200300 \
  --save-dir=~/vkmd-cli
```

## How to copy VK.com cookies correctly

@TODO

## License

vkmd-cli is released under the BSD-3-Clause License. See the bundled LICENSE.txt for details.
