# Server Preferences

When the mod is installed on a server, the server owner can configure various preferences that affect the gameplay experience of the mod.

## Available Settings

| Setting name                 | Value Type                          | Default Value | Real Value | Description                                                                                        |
|------------------------------|-------------------------------------|---------------|------------|----------------------------------------------------------------------------------------------------|
| `maximumImageUploadSize`     | Size in Bytes (Integer)             | 5 242 880     | 5 MiB      | Sets the maximum size of an image the client can upload per image. Exists to avoid exploitation.   |
| `maximumThumbnailUploadSize` | Size in Bytes (Integer)             | 524 288       | 512 KiB    | Sets the maximum size of a thumbnail the client can upload per image. Exists to avoid exploitation |
| `maximumMetadataUploadSize`  | Size in Bytes (Integer)             | 102 400       | 100 KiB    | Sets the maximum size of metadata the client can upload. Exists to avoid exploitation.             |
| `isPlayerUploadEnabled`      | Boolean                             | true          | Yes/No     | Enables or disables the ability for clients to upload images.                                      |
| `maximumUploadsPerPlayer`    | Number of Images per User (Integer) | No Limit      | N/A        | Sets the maximum number of images a single user can upload. Exists to avoid exploitation.          |

## FAQ
### My config file doesn't exist!?
In order to  change settings, you must have a config file, which is located in the `config` folder of your Minecraft server under
"myworld_traffic_addition". The file should be called `server_config.json`.

If the file does not exist, please check if you have already run the server for the first time. This is crucial
because MyWorld Traffic Addition generates all the config files on the first run OR if they don't exist in the specified locations.

If you have run the server one time already and the config file still isn't there, please check if you have installed the mod correctly
and if you let the server start up fully without interrupting the start process.

If you have checked all of the above, please manually create a file called `server_config.json` in `<server_dir>/config/myworld_traffic_addition/`

### My config file isn't working!
Please check if you have used correct JSON Syntax in your config file and if the setting names match the ones listed here

### How do I write valid JSON?
Please refer to any JSON Guide online. I recommend using the tutorialspoint's Guide on JSON since it's simple and easy to understand: [tutorialpoints Guide on JSON](https://www.tutorialspoint.com/json/json_quick_guide.htm)

Here's a simple example of a valid JSON file:

```json
{
    "setting_name_1": true,
    "setting_name_2": 10,
    "setting_name_3": "example text"
}
```

You still have to change up the values and the settings though!