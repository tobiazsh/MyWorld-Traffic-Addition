# Server Preferences

When the mod is installed on a server, the server owner can configure various preferences that affect the gameplay experience of the mod.

## Available Settings

| Setting Category                   | Setting name             | Value Type                          | Default Value | Real Value  | Description                                                                                                                 |
|------------------------------------|--------------------------|-------------------------------------|---------------|-------------|-----------------------------------------------------------------------------------------------------------------------------|
| `customizable_signs.online_images` | `max_size`               | Size in Bytes (Long)                | 5 242 880     | 5 MiB       | Sets the maximum size of an image the client can upload per image. Exists to avoid exploitation.                            |
| `customizable_signs.online_images` | `max_thumnbnail_size`    | Size in Bytes (Long)                | 524 288       | 512 KiB     | Sets the maximum size of a thumbnail the client can upload per image. Exists to avoid exploitation                          |
| `customizable_signs.online_images` | `max_metadata_size`      | Size in Bytes (Long)                | 102 400       | 100 KiB     | Sets the maximum size of metadata the client can upload. Exists to avoid exploitation.                                      |
| `customizable_signs.online_images` | `upload_enabled`         | Boolean                             | true          | Yes/No      | Enables or disables the ability for clients to upload images.                                                               |
| `customizable_signs.online_images` | `has_limit`              | Boolean                             | true          | Yes/No      | Whether uploading images has a limit. `max_uploads_per_player` will be ignored if this setting is `false`.                  |
| `customizable_signs.online_images` | `max_uploads_per_player` | Number of Images per User (Integer) | No Limit      | N/A         | Sets the maximum number of images a single user can upload. Exists to avoid exploitation.                                   |
| `customizable_signs.online_images` | `download_timeout`       | Time in milliseconds                | 15 0000       | 15 Seconds  | Sets the duration the clients can download a custom image from the server without timeout. Useful if server has big images. |
| `customizable_signs.general`       | `max_width`              | Size in blocks (short)              | 60            | 60 Blocks   | The maximum width a customizable sign can be initialized with.                                                              |
| `customizable_signs.general`       | `max_height`             | Size in blocks (short)              | 60            | 60 Blocks   | The maximum height a customizable sign can be initialized with.                                                             |
| `customizable_signs.general`       | `max_elements`           | Size in elements (short)            | 30            | 30 Elements | The maximum amount of elements to be allowed inside a customizable sign.                                                    |


## FAQ
### My config file doesn't exist!?
In order to change settings, you must have a config file, which is located in the `config` folder of your Minecraft server under
"myworld_traffic_addition". The file should be called `server_preferences.toml`.

If the file does not exist, please check if you have already run the server for the first time. This is crucial
because MyWorld Traffic Addition generates all the config files on the first run OR if they don't exist in the specified locations.

If you have run the server one time already and the config file still isn't there, please check if you have installed the mod correctly
and if you let the server start up fully without interrupting the start process.

If you have checked all of the above, please manually create a file called `server_preferences.toml` in `<server_dir>/config/myworld_traffic_addition/`.

### My config file isn't working!
Please check whether you have used correct TOML syntax in your config file and if the setting names match the ones listed here.

### How do I write valid TOML?
Please refer to any TOML Guide online. I recommend using the official guide on TOML: [TOML Introduction](https://toml.io/en/v1.1.0)

Here's a simple example of a valid TOML file:

```toml
[category]
setting_name_1 = true
setting_name_2 = 10

[category.subcategory]
setting_name_3 = "example text"
```

You still have to change up the values and the settings though!