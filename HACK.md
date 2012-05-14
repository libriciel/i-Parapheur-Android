Title: Build & Hack iparapheur-tab

# Build & Hack iparapheur-tab


## Build from source


### Prerequisites

* Java >= 6
* Maven >= 3
* Up to date Android SDK
* Shell prompt
* Java IDE with Maven support of your choice
* Integration tests require an online Android >=3.0 device


### Build system

iparapheur-tab build system is based on [Maven](http://maven.apache.org). If
you need to get started about it, check the
[Sonatype books](http://www.sonatype.com/Support/Books).

The project is split in two modules, *application* and *integration-tests*. Their
names should be self-explanatory.


#### Install Android SDK artifacts to your local maven repository

Use 
[maven-android-sdk-deployer](https://github.com/mosabua/maven-android-sdk-deployer)
to install needed Android SDK artifacts to your local maven repository:

    git clone https://github.com/mosabua/maven-android-sdk-deployer.git
    cd maven-android-sdk-deployer
    mvn install -P 3.0
    mvn install -P 3.1
    mvn install -P 3.2
    mvn install -P 4.0
    mvn install -P 4.0.3

The [README](https://github.com/mosabua/maven-android-sdk-deployer#readme) from
this project clearly describe how to deploy thoses artifacts to a hosted
maven repository. This can be usefull to prevent the need of the manual 
installation described above.


#### Maven & Android

The maven project is based on the "release" archetype provided by the android maven plugin project :
[android-maven-plugin](http://code.google.com/p/maven-android-plugin).

Embedded help is quite explicit:

    mvn -pl application android:help


#### Common build commands

Delete all produced data

    mvn clean

Run unit test and integration tests on a plugged device

    mvn test

Package the APK in ~/application/target

    mvn -pl application package 

Package the APK, deploy it on plugged devices and run it

    mvn -pl application package android:deploy android:run


### Git branching model and release process

We use the git branching model provided by 
[git-flow](https://github.com/nvie/gitflow#readme) described in 
[this web page](http://nvie.com/posts/a-successful-git-branching-model/).

**TODO** Sync release process to Adullact conventions and describe it here.


### Code conventions

* Don't use class names in android layout files
    * This one is opiniated and can bother some of you but it makes refactoring much easier and it is considered important.
    * Each violation of this convention must be argued and documented below.
    * This convention may be dismissed later.

#### Exceptions to code conventions

None for now.

### Architecture Overview

TO BE DONE

#### Network & Http

http://www.google.com/events/io/2010/sessions/developing-RESTful-android-apps.html







