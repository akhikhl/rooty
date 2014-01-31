/*
 * rooty
 *
 * Copyright (c) 2014  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gradle.rooty

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.*


/**
 * Gradle plugin for automating idiomatic part of multi-project gradle configuration
 */
class RootyPlugin implements Plugin<Project> {

  void apply(Project project) {

    project.apply plugin: 'base' // add "clean" task to the root project.

    def rootFile = project.file('root.gradle')
    if(rootFile.exists() && rootFile.isFile())
      project.apply from: rootFile

    project.ext {
      generateSources = project.hasProperty('generateSources') ? project.generateSources : true
      generateJavadoc = project.hasProperty('generateJavadoc') ? project.generateJavadoc : true
      groovy_version = project.hasProperty('groovy_version') ? project.groovy_version : '2.2.1'
      junit_version = project.hasProperty('junit_version') ? project.junit_version : '4.11'
      spock_version = project.hasProperty('spock_version') ? project.spock_version : '0.7-groovy-2.0'
      logback_version = project.hasProperty('logback_version') ? project.logback_version : '1.0.13'
      slf4j_version = project.hasProperty('slf4j_version') ? project.slf4j_version : '1.7.5'
      logback = "ch.qos.logback:logback-classic:$logback_version"
      slf4j_api = "org.slf4j:slf4j-api:$slf4j_version"
    }

    project.afterEvaluate {
      rootProjectAfterEvaluate(project)
    }

    project.subprojects {

      apply plugin: 'maven'

      repositories {
        mavenLocal()
        mavenCentral()
      }

      def commonFile = rootProject.file('common.gradle')
      if(commonFile.exists() && commonFile.isFile())
        apply from: commonFile

      def sonatypeFile = rootProject.file('sonatype.gradle')
      if(sonatypeFile.exists() && sonatypeFile.isFile())
        apply from: sonatypeFile

      afterEvaluate {
        subprojectAfterEvaluate(it)
      }
    } // subprojects

  } // apply

  private void rootProjectAfterEvaluate(Project project) {

    if(!project.tasks.findByName('build'))
      project.task('build')

    def appsDir = project.file('apps')
    if(appsDir.exists()) {

      if(!project.tasks.findByName('buildApps')) {
        project.task('buildApps', type: GradleBuild) { task ->
          dir = appsDir
          tasks = [ 'build' ]
        }
        project.build.finalizedBy project.buildApps
      }

      if(!project.tasks.findByName('cleanApps')) {
        project.task('cleanApps', type: GradleBuild) { task ->
          dir = appsDir
          tasks = [ 'clean' ]
        }
        project.clean.dependsOn project.cleanApps
      }
    }

    def examplesDir = project.file('examples')
    if(examplesDir.exists()) {

      if(!project.tasks.findByName('buildExamples')) {
        project.task('buildExamples', type: GradleBuild) { task ->
          dir = examplesDir
          tasks = [ 'build' ]
        }
        project.build.finalizedBy project.buildExamples
      }

      if(!project.tasks.findByName('cleanExamples')) {
        project.task('cleanExamples', type: GradleBuild) { task ->
          dir = examplesDir
          tasks = [ 'clean' ]
        }
        project.clean.dependsOn project.cleanExamples
      }
    }

    def buildSrcDir = project.file('buildSrc')
    if(buildSrcDir.exists()) {
      if(!project.tasks.findByName('buildSrc_uploadArchives')) {
        project.task('buildSrc_uploadArchives', type: GradleBuild) { task ->
          dir = buildSrcDir
          tasks = [ 'uploadArchives' ]
        }
        project.uploadArchives.dependsOn project.buildSrc_uploadArchives
      }

      if(!project.tasks.findByName('buildSrc_clean')) {
        project.task('buildSrc_clean', type: GradleBuild) { task ->
          dir = buildSrcDir
          tasks = [ 'clean' ]
        }
        project.clean.dependsOn project.buildSrc_clean
      }
    }
  } // rootProjectAfterEvaluate

  private void subprojectAfterEvaluate(Project project) {
    project.version = (project.version == 'unspecified' ? (project.rootProject.version == 'unspecified' ? '0.0.1' : project.rootProject.version) : project.version)

    if(project.plugins.findPlugin('java')) {
      project.sourceCompatibility = '1.7'
      project.targetCompatibility = '1.7'
      [project.compileJava, project.compileTestJava]*.options*.encoding = 'UTF-8'
    }

    if(project.plugins.findPlugin('groovy'))
      project.dependencies {
        compile "org.codehaus.groovy:groovy-all:${project.groovy_version}"
      }

    if(project.parent.name == 'libs' && project.tasks.findByName('install')) {
      // lib projects should be always installed into "$HOME/.m2/repository"
      project.tasks.build.finalizedBy project.tasks.install
      project.rootProject.tasks.findByName('buildExamples')?.dependsOn project.tasks.install
      project.rootProject.tasks.findByName('buildApps')?.dependsOn project.tasks.install
    }

    if(project.plugins.findPlugin('java') || project.plugins.findPlugin('groovy'))
      project.task('createFolders', description: 'Creates the source folders if they do not exist.') << {
        project.sourceSets*.allSource*.srcDirs*.each { File srcDir ->
          if (!srcDir.isDirectory()) {
            println "Creating source folder: ${srcDir}"
            srcDir.mkdirs()
          }
        }
      }

    if(project.tasks.findByName('test'))
      project.dependencies {
        testCompile "junit:junit:${project.junit_version}"
        if(!project.plugins.findPlugin('groovy'))
          testCompile "org.codehaus.groovy:groovy-all:${project.groovy_version}"
        testCompile "org.spockframework:spock-core:${project.spock_version}"
      }

    if(project.generateSources && project.tasks.findByName('classes')) {
      project.task('sourcesJar', type: Jar, dependsOn: project.classes, description: 'Creates sources jar') {
        classifier = 'sources'
        from project.sourceSets.main.allSource
      }
      project.artifacts {
        archives project.sourcesJar
      }
    }

    if(project.generateJavadoc && project.tasks.findByName('javadoc')) {
      project.task('javadocJar', type: Jar, description: 'Creates javadoc jar') {
        dependsOn project.javadoc
        classifier = 'javadoc'
        from project.javadoc.destinationDir
        if(project.tasks.findByName('groovydoc')) {
          dependsOn project.groovydoc
          from project.groovydoc.destinationDir
        }
      }
      project.artifacts {
        archives project.javadocJar
      }
    }
  }
}
