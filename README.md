# OpenEasterEggsLib
Encrypted data/assets to prevent users from finding out secrets. 
This mod was made to hide recipes, items, etc. from the user, allowing mod devs to make truely hidden easter eggs.

Comes with a data-gen library. I would recommend making a datagen sourceset and gitignoring it.

# Current Features
  - encrypted recipes
    - crafting
    - smithing
    - stonecutting
  - encrypted textures
  - encrypted lang entries
  - api for adding your own features

# Commonly Asked Questions
**Can't you just dump all the recipes at runtime?**: No, the encryption key is derived from the inputs of the recipe, so to know the output of the recipe, you must know the input items. The way the recipe is validated is through the validation key, which is a seperate hash of the inputs of the recipe. This diagram should hopefully clear it up:
![img](https://github.com/Foundations-of-Alchemy/OpenEasterEggsLib/blob/master/reade/img.png?raw=true)

**Why?**: It's in the name, for easter eggs, but more importantly it's part of [foa](https://gist.github.com/Devan-Kerman/4e7a5e6a44e08281d8bfef8e56dab61f).


**This was NOT made for paranoid artists/developers**\
If you're using this mod for DRM, the licence allows you to but, fuck you.
