# Finger
Troll students by making them think they have been selected _but actually not_.

Finger is written in Java using [Slick2D](http://slick.ninjacave.com/) and
[LWJGL](http://lwjgl.org/), wrappers around the OpenGL and OpenAL libraries.

## Building
Finger! is distributed as a Maven project.

* To run the project, execute the Maven goal `compile exec:exec`.
* To create a single executable JAR file, execute the Maven goal
  `install -Djar`.  This will link the LWJGL native libraries using a
  [modified version](https://github.com/itdelatrisu/JarSplicePlus) of
  [JarSplice](http://ninjacave.com/jarsplice), which is included in the
  `tools` directory in both its original and modified forms.  The resulting
  file will be located in `target/finger-${version}-runnable.jar`.

## License
**This software is licensed under GNU GPL version 3.**
You can find the full text of the license [here](LICENSE).
