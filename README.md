Title: iparapheur-tab

# iparapheur-tab

This repository contains source of the Android iParapheur client application.

## TODO

TODO

## Build from source

### Prerequisites

* Java >= 6
* Maven >= 3
* Up to date Android SDK
* Shell prompt
* Java IDE with Maven support of your choice
* Integration tests require an online Android >=3.0 device

### Build system

TODO Short pointers about Maven

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

    mvn android:help

### Git branching model and release process

We use the git branching model provided by 
[git-flow](https://github.com/nvie/gitflow#readme) described in 
[this web page](http://nvie.com/posts/a-successful-git-branching-model/).

### Code conventions

* Don't use class names in android layout files
    * This one is opiniated and can bother some of you but it makes refactoring much easier and it is considered important.
    * Each violation of this convention must be argued and documented.
    * This convention may be dismissed later.

