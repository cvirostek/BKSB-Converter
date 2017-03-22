# BKSB Converter

**What is a .bksb?**

The original Temple Run for iOS was made with a custom-built game engine, which uses its own unique binary format for 3D models (.bksb).
Note that the Android and Windows Phone versions of Temple Run were rebuilt on the Unity engine, so they do not use .bksb files.


**What does this program do?**

It converts .bksb models to .obj format, and vice versa. This allows you to put your own models in Temple Run.

**Characteristics of .bksb**

- They use vertex animation rather than skeletal animation, which means that each frame of animation
is actually an entire new model. If you compare the iOS version to the Unity versions, you'll notice that the former has much choppier
animations as a result.
  - When converting an animated .bksb, one .obj file is created for each frame of the animation.
- Some .bksb models have two UV maps: one for the regular texture, and one for a lightmap texture that superimposes shadows over the regular
texture.
  - The only models in Temple Run without a lightmap are the coins, characters, and enemies.
  - Since .obj files can only have one UV map, there is an option to generate a second .obj with the lightmap UV. To convert back to .bksb
  with the lightmap intact, you'll need to have both versions of the model.
