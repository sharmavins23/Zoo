# Zoo

![img](https://cdn.discordapp.com/attachments/714206938719715429/728761036332597278/Screenshot_20200703-195519.jpg)

All assets used were created by
**[Poly by Google](https://poly.google.com/user/4aEd8rQgKu2)**. These were
published under a Public/Remixable (CC-BY) license.

# Overview and Functionality

This project demonstrates ARCore's ability to load multiple assets on the screen
at once. Every single animal, once placed down in the world, will be rendered
properly and will stay in its location, anchored by one anchor node. Which animal is placed can be swapped at will utilizing two buttons.

The animal assets are loaded from a URI, much like the [URIAssetLoader](https://github.com/sharmavins23/URIAssetLoader) project. Unlike this project, however, these URL .gltf files are supplied to the application via a server [database](https://github.com/sharmavins23/Xora-Fileserver), which automatically loads and reloads the files.

The current project runs on ARCore, using Sceneform 1.15.0. This is one of the later stable versions of Sceneform.

# Coding Process

The application reused a lot of the code from my original URI Asset Loading file. Hits were scanned properly and reused, and nodes were added similarly. The old code could already handle placement of multiple assets in rapid succession; The challenge came in getting those models.

While setting up the file-server proved to be a simple task, writing the HTTP client code was extremely difficult in Kotlin. The code has three helper functions that offload the HTTP request from the main thread.

---

### Guides/References

-   [https://stackoverflow.com/a/61482596/13821979](https://stackoverflow.com/a/61482596/13821979)
    -   This is a Stack Overflow post describing how HTTP requests in Kotlin can be structured. Since then, I have been using this as a framework for all HTTP requests in Kotlin.
-   [https://stackoverflow.com/questions/26625555/failed-to-find-style-with-id-0x7f070001-in-current-theme-when-using-cardview-a](https://stackoverflow.com/questions/26625555/failed-to-find-style-with-id-0x7f070001-in-current-theme-when-using-cardview-a)
    -   One of the issues encountered was that the `activity_main.xml` layout wasn't registering style elements. The Gradle build doesn't always synchronize properly; This can be fixed by restarting the application.

# License TL;DR

This project is distributed under the MIT license. This is a paraphrasing of a
[short summary](https://tldrlegal.com/license/mit-license).

This license is a short, permissive software license. Basically, you can do
whatever you want with this software, as long as you include the original
copyright and license notice in any copy of this software/source.

## What you CAN do:

-   You may commercially use this project in any way, and profit off it or the
    code included in any way;
-   You may modify or make changes to this project in any way;
-   You may distribute this project, the compiled code, or its source in any
    way;
-   You may incorporate this work into something that has a more restrictive
    license in any way;
-   And you may use the work for private use.

## What you CANNOT do:

-   You may not hold me (the author) liable for anything that happens to this
    code as well as anything that this code accomplishes. The work is provided
    as-is.

## What you MUST do:

-   You must include the copyright notice in all copies or substantial uses of
    the work;
-   You must include the license notice in all copies or substantial uses of the
    work.

If you're feeling generous, give credit to me somewhere in your projects.
