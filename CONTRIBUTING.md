# Contributing

## Importing project

##### Setup in Android-Studio :

- File → Open...
- Open the `build.gradle` file
- Enable Auto-import
- Go to Preferences → Build, Execution, Deployment → Compiler → Annotation Processors
- Check Enable annotation processing
- Go to Preferences → Plugins → Search and install `Lombok`

##### Building :

For every Gradle task, you'll need a signing keystore file in the root directory,  
and the appropriate properties file, looking like this :

```
storeFile=********
storePassword=********
keyAlias=********
keyPassword=********
```

Those elements are set in the GitLab CI, but were hidden (using the CI variables).  
For test/contribute settigns, any other keystore can be used.
Or the configuration can be simply removed from the `build.gradle` file.


## Code style and project structure

TODO


## Unit tests

the `.gitlab-ci.yml` file has some examples, running multiple tests.  

- JUnit test are the simple ones. (`utils`, `models`, static methods...)
- Instrumented tests need an emulator. (UI, DB operations...)

Both are run with a and code-coverage is merged by Jacoco.

