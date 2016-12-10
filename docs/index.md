# ChangeLog Overview

ChangeLog uses Git commit comments and tags from a local repository, combined with issue information from an associated remote repository, to build a change log.

Parameters are highly configurable, and [Velocity](https://velocity.apache.org/) is used to generate the output, enabling the user to modify presentation however they wish.
 
## Alternatives
  
There are quite a few similar projects out there, and the preference would have been to use one that already exists.  None of them quite do what was wanted, although in functionality terms [Github-Changelog-Generator](https://github.com/skywinder/Github-Changelog-Generator) looked very good.  Unfortunately, for our purposes it introduced a dependency on Ruby, for which we have no other purpose.

**Github-Changelog-Generator** also provides a [useful list](https://github.com/skywinder/Github-Changelog-Generator/wiki/Alternatives) of similar projects.

## Limitations
Currently only [GitHub](https://github.com) is supported as a source of issue information.  This limitation is actually caused by [GitPlus](https://github.com/davidsowerby/gitplus), which is used to manage the interaction with Git) (Contributions of other implementations would be very welcome)

## Languages

ChangeLog is written in [Kotlin](https://kotlinlang.org/), but published as a Java library.  Kotlin is 100% interoperable with Java

# Configuration and use

## Instantiation

### Using Guice

Include a `ChangeLogModule` instance in your injector and inject `ChangeLog` where required

### Without Guice

```
ChangeLogFactory.getInstance()
```

<a name="MinimumConfiguration"></a>
## Minimum Configuration

A minimum configuration requires a project name, the user name of the remote repo, and target project's parent directory:

```
changeLog
   .projectName('a-project-name')
   .remoteRepoUser('remote-username')
   .projectDirParent({parent directory of the target project})
```

## API Keys
An API key is needed to access issue information and potentially to push the change log output to the wiki.  An appropriate key should be located as described in the [GitPlus documentation](http://gitplus.readthedocs.io/en/develop/build-properties/)


## Generating the output

Once configured:

```
changelog.generate()
```


There are many [configuration options](configuration.md), but this [minimum configuration](#MinimumConfiguration) will:

- generate a change log based on tags (every tag is considered to be a version).
- create a 'version' called 'current build' for any commits after the latest version (some refer to these as 'unreleased commits')
- create a Markdown output, called 'markdown.md', placed in the root of the local copy of the associated wiki repository.
- Push the change log update to the wiki (the local wiki repo must exist and be properly configured for Git push)


## Sample Outputs

The links below all use the standard Velocity template - you can of course change that and use your own template by changing the [templateName](configuration/#templateName) property

Projects
--------

[changelog](https://github.com/davidsowerby/changelog/wiki/changelog)