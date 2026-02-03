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
4. **Define the sprites inside the .json file**. The structure of the .json file should look like this:
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
   preferred Name
    - `location`: The path to your atlas image file inside the jar. Make sure to include the leading slash `/`.
    - `inJar`: _**IGNORE! Always set to `true`! To be implemented in future versions.**_
    - `sprites`: An array containing the definitions of each sprite in the atlas.


5. Define your sprites inside the "sprites" array. Each sprite should have the following structure:
    ```json
    {
      "spriteId": "namespace:sprite_name",
      "x": 0,
      "y": 0,
      "width": 64,
      "height": 64
    }
    ```
    - `spriteId`: The unique identifier for the sprite. Replace `namespace` with your preferred namespace and `sprite_name` with your preferred name for the sprite.
    - `x`: Where the sprite starts on the X-axis in the atlas image. (left to right)
    - `y`: Where the sprite starts on the Y-axis in the atlas image. (top to bottom)
    - `width`: The width of the sprite.
    - `height`: The height of the sprite.

6. Start the game and pray to the modding gods that you did everything right. If you did, your new sign background should be
available in the sign background selection menu!

**Note:** You need to share the same .jar of the mod with anyone who wants to use your custom background, as the
backgrounds are loaded from the mod's resources. If the person does not have the same .jar, they will simply see a
white background.