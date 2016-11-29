# ChangeLog 

ChangeLog uses Git commit comments and tags from a local repository, combined with issue information from an associated remote repository, to build a change log.

Parameters are highly configurable, and a [Velocity] template is used to generate the output, enabling the user to modify presentation however they wish.

# Example Output
The [change log for this project](https://github.com/davidsowerby/changelog/wiki/changelog) provides an example of the output, but the output is highly configurable 
 
# Alternatives
  
There are quite a few similar projects out there, and the preference would have been to use one that already exists.  None of them quite do what was wanted, although in functionality terms [this one](https://github.com/skywinder/Github-Changelog-Generator) looked very good.  Unfortunately, for our purposes it introduced a dependency on Ruby, for which we have no other purpose.
That project also provides a [useful list](https://github.com/skywinder/Github-Changelog-Generator/wiki/Alternatives) of similar projects.

# Use with Gradle
It is intended that this library will also have a Gradle wrapper so that it can be readily invoked as part of a build

# Limitations
Currently only [GitHub](https://github.com) is supported as a source of issue information.  This limitation is actually caused by [GitPlus](https://github.com/davidsowerby/gitplus), which is used to manage the interaction with Git)

# Documentation

refer to the [user guide](http://ds-changelog.readthedocs.io/en/latest/) for configuration and usage

# Language

ChangeLog is written in [Kotlin](https://kotlinlang.org/), but published as a Java library.  Kotlin is 100% interoperable with Java

# Licence

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

# Build from source

> ./gradlew build

# Download

Not yet available from JCenter or Maven Central

# Acknowledgements

Thanks to:
 
[Guice](https://github.com/google/guice)<br>
[Gradle](http://gradle.org/)<br>
[Gradle Docker Plugin](https://github.com/bmuschko/gradle-docker-plugin)<br>
[Gradle Bintray Plugin](https://github.com/bintray/gradle-bintray-plugin)<br>
[Bintray](https://bintray.com)<br>
[Logback](http://logback.qos.ch/)<br>
[slf4j](http://www.slf4j.org/)<br>
[spock](https://github.com/spockframework/spock)
[FindBugs](http://findbugs.sourceforge.net/)
[Velocity](https://velocity.apache.org/)

