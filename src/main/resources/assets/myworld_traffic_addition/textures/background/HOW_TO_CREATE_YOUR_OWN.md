# Implementing your own background

Creating your own sign background is actually fairly easy. To do that, you have to keep in mind that sign
currently only support edges, not corners. This means that you have to create 16 different textures to cover all the
possible combinations of edges. But do not fear! You will not have to make a new file for each of them,
instead, in modern versions of this mod, you can use a single sprite atlas to create all the necessary textures.

You also don't have to follow concrete dimensions, as you will have to create or own .json file to define the
sprites inside the atlas. This is also not hard.
1. First, **create a new sprite atlas image** using any image editing
software such as GIMP, Photoshop, Affinity, or even MS Paint (if you like pain).
2. **Place it somewhere accessible** within the mod's resource folder structure.
3. **Create a new .json file** with any name you want (preferably the same as the image file). You have to options on where
    to place it:
    - **`./autoload` folder: This folder is automatically scanned by the mod, and any .json file placed here will be
    automatically loaded. This is the recommended option for most users.**
    - anywhere else in the resource folder structure: If you choose this option, you will have to manually
    specify the path to the .json file in code at `client/java/at/tobiazsh/myworld_traffic_addition/texture/sign/BackgroundAtlases.java`
   
4. For the .json file, you need the following base-fields, no matter if you're doing the manual or automatic approach:

   | Field Name | Job                                                   | Usage                                                                            |
   |------------|-------------------------------------------------------|----------------------------------------------------------------------------------|
   | `atlasId`  | Differentiate between mutliple different atlases      | Use Minecraft's Identifier syntax: `namespace:name`                              |
   | `location` | Points to the location of the atlas sprite texture    | Use absolute path inside that jar (starting with `/`, indicating the Jar's Root) |
   | `inJar`    | Specifies if the all the data is inside the jar.      | Always set to true! Outside-atlas is not supported yet.                          |
   | `isAuto`   | Specifies if you're using the auto or manual approach | `true` or `false`, depending on what you're doing                                |
   
   For example:
   ```json
   {
       "atlasId": "namespace:atlas_name",
       "location": "/assets/myworld_traffic_addition/sprites/someatlas.png",
       "inJar": true,
       "isAuto": true,
       ...
   }
   ```

5. **Now you have two options:**

## Automatic Approach

**Define the sprite atlas inside the .json file**. Use these extra fields:
   
| Field Name          | Job                                                                                        | Type       |
|---------------------|--------------------------------------------------------------------------------------------|------------|
| `startX`            | Defines where to start scanning on the X axis (left to right) in px                        | int        |
| `startY`            | Defines where to start scanning on the Y axis (top to bottom) in px                        | int        |
| `baseWidth`         | Defines the width for each sprite in the atlas in px                                       | int        |
| `baseHeight`        | Defines the height for each sprite in the atlas in px                                      | int        |
| `spriteIdNamespace` | Defines the namespace for the id for each sprite in the atlas in px                        | String     |
| `spriteIdPaths`     | Defines the path for the id for each spirtein the atlas in px. For each row, use new array | String[][] |

   For example, here's the `slovenia_default.json` file:
   
   ```json
   {
     "atlasId": "slovenia:default",
     "location": "/assets/myworld_traffic_addition/textures/background/slovenia_default.png",
     "inJar": true,
     "isAuto": true,
     "startX": 0,
     "startY": 0,
     "baseWidth": 1024,
     "baseHeight": 1024,
     "spriteIdNamespace": "slovenia_default",
     "spriteIdPaths": [
       ["0000", "0001", "0010", "0011"],
       ["0100", "0101", "0110", "0111"],
       ["1000", "1001", "1010", "1011"],
       ["1100", "1101", "1110", "1111"]
     ]
   }
   ```
   
   The 0000's define the border style of the background:
    - First digit: Top edge (0 = no edge, 1 = edge)
    - Second digit: Right edge (0 = no edge, 1 = edge)
    - Third digit: Bottom edge (0 = no edge, 1 = edge)
    - Fourth digit: Left edge (0 = no edge, 1 = edge)

   ... and so should yours too!

## Manual Approach

1. **Define the sprite atlas inside the .json file**. The structure of the .json file should look like this:
    ```json
    {
      "atlasId": "namespace:atlas_name",
      "location": "/assets/myworld_traffic_addition/sprites/someatlas.png",
      "inJar": true,
      "sprites": [
        ... list of sprites ...
      ]
    }
    ```
    - `atlasId`: A unique identifier for your atlas. Replace `namespace` with your preferred namespace and `atlas_name`
   preferred Name. Internally, we use the country as the namespace and the type as the path (e.g. `austria:default`).
    - `location`: The path to your atlas image file inside the jar. Make sure to include the leading slash `/`.
    - `inJar`: _**IGNORE! Always set to `true`! To be implemented in future versions.**_
    - `sprites`: An array containing the definitions of each sprite in the atlas.


2. Define your sprites inside the "sprites" array. Each sprite should have the following structure:
    ```json
    {
      "spriteId": "namespace_atlas_name:0000",
      "x": 0,
      "y": 0,
      "width": 64,
      "height": 64
    }
    ```
    - `spriteId`: The unique identifier for the sprite. Replace `namespace_atlas_name` with your atlasId (e.g. when `"atlasId" = "austria:default"`, use `"spriteId" = "austria_default:xxxx"`, and `0000` with the type of background for the sign. Construct like so:
      - First digit: Top edge (0 = no edge, 1 = edge)
      - Second digit: Right edge (0 = no edge, 1 = edge)
      - Third digit: Bottom edge (0 = no edge, 1 = edge)
      - Fourth digit: Left edge (0 = no edge, 1 = edge)
      
      For example, a sprite with `spriteId` of `namespace:1010` would represent a sign background with edges on the top and bottom only.
      Internally, we use the whole country and name as the namespace (e.g. `austria_default:0000`).
    - `x`: Where the sprite starts on the X-axis in the atlas image. (left to right)
    - `y`: Where the sprite starts on the Y-axis in the atlas image. (top to bottom)
    - `width`: The width of the sprite.
    - `height`: The height of the sprite.

3. Start the game and pray to the modding gods that you did everything right. If you did, your new sign background should be
available in the sign background selection menu!

**Note:** You need to share the same .jar of the mod with anyone who wants to use your custom background, as the
backgrounds are loaded from the mod's resources. If the person does not have the same .jar, they will simply see a
white background.