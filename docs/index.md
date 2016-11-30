# ChangeLog Overview

ChangeLog uses Git commit comments and tags from a local repository, combined with issue information from an associated remote repository, to build a change log.

parameters are highly configurable, and a [Velocity](https://velocity.apache.org/) is used to generate the output, enabling the user to modify presentation however they wish.
 
## Alternatives
  
There are quite a few similar projects out there, and the preference would have been to use one that already exists.  None of them quite do what was wanted, although in functionality terms [this one](https://github.com/skywinder/Github-Changelog-Generator) looked very good.  Unfortunately, for our purposes it introduced a dependency on Ruby, for which we have no other purpose.
That project also provides a [useful list](https://github.com/skywinder/Github-Changelog-Generator/wiki/Alternatives) of similar projects.

## Limitations
Currently only [GitHub](https://github.com) is supported as a source of issue information.  This limitation is actually caused by [GitPlus](https://github.com/davidsowerby/gitplus), which is used to manage the interaction with Git)

## Languages

ChangeLog is written in [Kotlin](https://kotlinlang.org/), but published as a Java library.  Kotlin is 100% interoperable with Java

# Configuration and use

## Instantiation

### Using Guice

Include a `ChangeLogModule` instance in your injector and inject `ChangeLog` where required

### Without Guice

tbd - [open issue](https://github.com/davidsowerby/changelog/issues/6) 

## Configuration

A minimum configuration requires, a project name, the user name of the remote repo, and target project's parent directory:

```
changeLog
   .projectName('a-project-name')
   .remoteRepoUser('remote-username')
   .projectDirParent({parent directory of the target project})
```

Once configured:

```
changelog.generate()
```

There are many [configuration options](configuration.md), but this minimal configuration will:

- generate a change log based on tags (every tag is considered to be a version).
- create a 'version' called 'current build' for any commits after the latest version (some refer to these as 'unreleased commits')
- create a Markdown output, called 'markdown.md', placed in the root of the local copy of the associated wiki repository.
- Push the change log update to the wiki (the local wiki repo must exist and be properly configured for Git push)


## Sample Outputs

The links below all use the standard Velocity template - you can of course change that and use your own template by changing the [templateName] property

Projects
--------

[changelog](https://github.com/davidsowerby/changelog/wiki/changelog)