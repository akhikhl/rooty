# Rooty [![Maintainer Status](http://stillmaintained.com/akhikhl/rooty.png)](http://stillmaintained.com/akhikhl/rooty) [![Build Status](https://travis-ci.org/akhikhl/rooty.png?branch=master)](https://travis-ci.org/akhikhl/rooty) [![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/akhikhl/rooty/trend.png)](https://bitdeli.com/free "Bitdeli Badge") [![endorse](https://api.coderwall.com/akhikhl/endorsecount.png)](https://coderwall.com/akhikhl)

This is gradle plugin for automating idiomatic part of multi-project gradle builds.

All versions of Rooty are available in maven central under the group 'org.akhikhl.rooty'.

**Content**

1. [Why rooty?](#why-rooty)
2. [Gradle plugin](#gradle-plugin)
3. [Multi-project structure](#multi-project-structure)
4. [Root project tasks](#root-project-tasks)
5. [Library project tasks](#library-project-tasks)
6. [Injected properties](#injected-properties)
7. [Injected dependencies](#injected-dependencies)
8. [Copyright and License](#copyright-and-license)

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

![multi-project structure diagram](https://raw.github.com/akhikhl/rooty/master/doc/multi_project_structure.png "Multi-project structure")

"buildSrc" is a standard build script folder, described in 
[official gradle documentation](http://www.gradle.org/docs/current/userguide/organizing_build_logic.html#sec:build_sources).

"libs" contains java/groovy/scala libraries listed in "settings.gradle" of the root project.
Rooty ensures that these libraries are installed into local maven repository ($HOME/.m2/repository)
upon successful compilation.

"examples" contains example java/groovy/scala programs listed in "examples/settings.gradle".
Rooty ensures that examples are compiled only after successful compilation and installation of "libs".

"apps" contains java/groovy/scala applications listed in "apps/settings.gradle".
Rooty ensures that apps are compiled only after successful compilation and installation of "libs".

## Root project tasks

Rooty adds the following tasks to the root project:

![task diagram](https://raw.github.com/akhikhl/rooty/master/doc/rootproject_tasks_diagram.png "Root project tasks")

### build

Does nothing by default. Finalized by buildApps and buildExamples tasks.

### buildApps

Runs "build" task against "apps" folder (if such folder exists).

### buildExamples

Runs "build" task against "examples" folder (if such folder exists).

### clean

Deletes "build" folder of the root project (if such folder exists). 
Depends on "cleanApps", "cleanExamples" and "buildSrc_clean" tasks.

### cleanApps

Runs "clean" task against "apps" folder (if such folder exists).
"clean" task of the root project depends on "cleanApps".

### cleanExamples

Runs "clean" task against "examples" folder (if such folder exists).
"clean" task of the root project depends on "cleanExamples".

### buildSrc_clean

Runs "clean" task against "buildSrc" folder (if such folder exists).
"clean" task of the root project depends on "buildSrc_clean".

### buildSrc_uploadArchives

Runs "uploadArchives" task against "buildSrc" folder (if such folder exists).
"uploadArchives" task of the root project depends on "buildSrc_uploadArchives".

## Library project tasks

Rooty adds the following tasks to every project within "libs":

![task diagram](https://raw.github.com/akhikhl/rooty/master/doc/libproject_tasks_diagram.png "Library project tasks")

### sourcesJar

Creates sources jar from the source code of the given library.
"assemble" task of library project depends on "sourcesJar".

### javadocJar

Creates javadoc jar from the source code of the given library.
"assemble" task of library project depends on "javadocJar".

## Injected properties

Rooty injects the following properties into root project:

```groovy
ext {
  generateSources = true
  generateJavadoc = true
  groovy_version = '2.2.2'
  junit_version = '4.11'
  spock_version = '0.7-groovy-2.0'
  logback_version = '1.1.1'
  slf4j_version = '1.7.6'
  logback = "ch.qos.logback:logback-classic:$logback_version"
  slf4j_api = "org.slf4j:slf4j-api:$slf4j_version"
}
```

By default all library projects inherit these properties.

You can override any of these properties either by redefining them in the root project
or in any (or every) library project, for example, like this:

```groovy
ext {
  generateSources = false
}
```

## Injected dependencies

Rooty injects the following dependencies into library projects:

```groovy
dependencies {
  // injected if project has groovy nature
  compile "org.codehaus.groovy:groovy-all:${project.groovy_version}"
  // the following 3 dependencies are injected if project has "test" task
  testCompile "junit:junit:${project.junit_version}"
  testCompile "org.codehaus.groovy:groovy-all:${project.groovy_version}"
  testCompile "org.spockframework:spock-core:${project.spock_version}"
}
```
Please note that neither logback nor slf4j-api are injected. However, the relevant
injected properties somewhat simplify corresponding declarations:

```groovy
dependencies {
  compile logback
  compile slf4j_api
}
```

## Copyright and licence

Copyright 2014 (c) Andrey Hihlovskiy

All versions, present and past, of rooty are licensed under [MIT license](license.txt).


