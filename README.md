Title: iParapheur Tab

# iParapheur Tab

Ce projet contiens les sources de l'application cliente d'iParapheur pour tablettes Android.

## TODO

TODO

## Développer pour iParapheur Tab

### Prérequis

* Java >= 6
* Maven >= 3
* Android SDK dernière version
* Un terminal
* Un IDE de votre choix supportant Maven
* Une tablette Android >=3.0 branchée pour passer les tests d'intégration

### Système de build

Maven TODO

#### Déployer en local les artifacts du SDK android

Avant de pouvoir utiliser le système de build du projet vous devez installer sur votre poste
l'ensemble des artifacts du SDK android nécessaires aux plateformes supportées.

Utilisez [maven-android-sdk-deployer](https://github.com/mosabua/maven-android-sdk-deployer) à cet effet :

    git clone https://github.com/mosabua/maven-android-sdk-deployer.git
    cd maven-android-sdk-deployer
    mvn install -P 3.0
    mvn install -P 3.1
    mvn install -P 3.2
    mvn install -P 4.0
    mvn install -P 4.0.3

Le README de ce projet explique clairement comment déployer ces artifacts sur un repository maven
interne. Utile pour éviter d'avoir à faire cette installation manuellement sur chaque poste de 
développement.

#### Maven et Android

C'est très simple, le projet est basé sur l'archetype "release" fourni par le plugin android utilisé :
[android-maven-plugin](http://code.google.com/p/maven-android-plugin).

L'aide intégrée est assez complète :

    mvn android:help


