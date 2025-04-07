# Migrating from [imglib/imglib2-ij](https://github.com/imglib/imglib2-ij)

Unfortunately, `imglib/imglib2-ij` cannot be used with Java 11+, because of the package conflicts with `imglib/imglib2`. If you try, you'll see build errors like:
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.8.1:compile (default-compile) on project scijava-legacy: Compilation failure: Compilation failure:
[ERROR] error: the unnamed module reads package net.imglib2.img from both net.imglib2 and net.imglib2.img
[ERROR] error: the unnamed module reads package net.imglib2.display.projector from both net.imglib2 and net.imglib2.img
[ERROR] error: module net.imglib2 reads package net.imglib2.img from both net.imglib2 and net.imglib2.img
[ERROR] error: module net.imglib2 reads package net.imglib2.display.projector from both net.imglib2 and net.imglib2.img
[ERROR] error: module org.slf4j reads package net.imglib2.img from both net.imglib2 and net.imglib2.img
[ERROR] error: module org.slf4j reads package net.imglib2.display.projector from both net.imglib2 and net.imglib2.img
```

To resolve these errors (and for organization) this library changed many names. Below you'll find commonly-used functions from `imglib/imglib2-ij`, as well as their drop-in replacements from `imglib/imglib2-imagej`:

**`net.imglib2.img.display.imagej.ImgToVirtualStack.wrap*` have been renamed to `net.imglib2.imagej.RAIToImagePlus.wrapVirtualStack*`**.
**`net.imglib2.img.VirtualStackAdapter.wrap*` have been renamed to `net.imglib2.imagej.ImagePlusToImg.wrapCached*`**.
**`net.imglib2.img.ImagePlusAdapter.wrap*` have been renamed to `net.imglib2.imagej.ImagePlusToImg.wrapDirect*`**.
**`net.imglib2.img.ImageJFunctions.wrap*(ImagePlus)` have been renamed to `net.imglib2.imagej.ImagePlusToImg.wrap*`**.
**`net.imglib2.img.display.imagej.ImageJFunctions.wrap*(RandomAccessibleInterval, ...)` have been renamed to `net.imglib2.imagej.RAIToImagePlus.wrap*`**.

If you require additional functionality that is not yet in this library, please file an issue!
