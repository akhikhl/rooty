# Rooty

This is gradle plugin for automating idiomatic part of multi-project gradle builds.

All versions of Rooty are available in maven central under the group 'org.akhikhl.rooty'.

**Content**

1. [Why rooty?](#why-rooty)
2. [Gradle plugin](#gradle-plugin)
3. [Multi-project structure](#multi-project-structure)
4. [Gradle configuration](#gradle-configuration)

## Why rooty?

Rooty takes opinionated view on how to organize multi-project gradle configuration.
Purpose is to reduce the amount of boilerplate code and make things work "out of the box",
without programming or with minimal programming.

## Gradle plugin

Add the following to "build.gradle":

```groovy
apply from: 'https://raw.github.com/akhikhl/rooty/master/pluginScripts/rooty.plugin'
```

then do "gradle build" from command-line.

Alternatively, you can download the script from https://raw.github.com/akhikhl/rooty/master/pluginScripts/rooty.plugin 
to the project folder and include it like this:

```groovy
apply from: 'rooty.plugin'
```

or feel free copying (and modifying) the declarations from this script to your "build.gradle".

## Multi-project structure

Rooty assumes the following multi-project structure (all parts are optional):

```
rootProject
|
+-- apps
|   |
|   +-- app1
|   |
|   +-- app2
|   |
|   +-- settings.gradle
|
+-- buildSrc
|
+-- examples
|   |
|   +-- example1
|   |
|   +-- example2
|   |
|   +-- settings.gradle
|
+-- libs  
|   |
|   +-- libraryA
|   |
|   +-- libraryB
|
+-- settings.gradle
```

"buildSrc" is a standard build script folder, described in 
[official gradle documentation](http://www.gradle.org/docs/current/userguide/organizing_build_logic.html#sec:build_sources).

"libs" contains java/groovy/scala libraries listed in "settings.gradle" of the root project.
Rooty ensures that these libraries are installed into local maven repository ($HOME/.m2/repository)
upon successful compilation.

"examples" contains example java/groovy/scala programs listed in "examples/settings.gradle".
Rooty ensures that examples are compiled only after successful compilation and installation of "libs".

"apps" contains java/groovy/scala applications listed in "apps/settings.gradle".
Rooty ensures that apps are compiled only after successful compilation and installation of "libs".

## Gradle tasks

## Gradle configuration

