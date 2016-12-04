# ChangeLog configuration

A summary of each property is given in the tables below, with properties grouped by function.
A more detailed explanation of some of these properties is given below the tables

## Project and branch identification properties

This section are all mandatory values - all others are optional

| name                  | purpose                                                                              | default                   |
|-----------------------|--------------------------------------------------------------------------------------|---------------------------|
| branch                | The branch to take commits / versions from                                           | "develop"                 |
| projectName           | name of the project, used to identify the local and remote project repo              | "not specified"           |
| remoteRepUser         | name of the remote repo user                                                         | "not specified"           |
| projectDirParent      | File object pointing to parent directory of project                                  |  null                     |


## Version or control

Output can be based on versions taken from Git tags, or simply a collection of commits, collated into a "pseudo version".  The two cannot be mixed, and the mode of operation is determined by this single property:



| name                  | purpose                                                                              | default                   |
|-----------------------|--------------------------------------------------------------------------------------|---------------------------|
| processingAsVersions  | If true, process as versions, else process as commits                                | true                      |


## Version properties

Used only when [processingAsVersions](#processingAsVersions) is true.


| name                  | purpose                                                                              | default                   |
|-----------------------|--------------------------------------------------------------------------------------|---------------------------|
| autoTagLatestCommit   | If true, unless already tagged, "pseudo tag" most recent commit                      | true                      |
| fromVersionId         | Starts the change log from this version (inclusive)                                  | The oldest version        |
| maxVersions           | Limits the number of versions produced. Takes precedence over [fromVersionId]        | 50                        |
| toVersionId           | Ends the change log output at this version (inclusive)                               | The most recent version   |
| versionTagFilter      | User may provide a filter instance to select which tags considered as versions       | All tags are versions     |

<a name="autoTagLatestCommit"></a>
### autoTagLatestCommit
    
When [processingAsVersions](#processingAsVersions) is true, if the latest commit does not have a version tag, the user may wish to control whether or not these "unversioned" or "unreleased" commits are presented as part of the change log.

If this property is true, and there is no tag on the most recent commit, a pseudo tag is added to it.  By default, this 'version' is called 'current build', but that can be configured by the [currentBuild] property. 
  
This can also be useful where a changelog is generated before a tag is applied.

When [processingAsVersions](#processingAsVersions) is false (that is, versions are ignored and just commits used), this property is not used.

Default is true
    
<a name="processingAsVersions"></a>
### processingAsVersions

If true, tags are evaluated as versions (using [versionTagFilter](#versionTagFilter)) and the output collated into versions.
The range of versions selected depends on **[fromVersionId]**, **[toVersionId]** and **[maxVersions]**

If false, commits are processed as though they belong to a single version, and if no version tag is present on
the most recent commit selected, then a `CommitRangeTag` is used as a placeholder.  The range of commits selected
depends on **[fromCommitId]**, **[toCommitId]** and **[maxCommits]**

Default is true

<a name="versionTagFilter"></a>
### versionTagFilter
Git tags may be used for a variety of purposes, not just versions.  A custom implementation of `VersionTagFilter` can be used to filter out those tags which do not represent versions 

## Commit properties

Used only when [processingAsVersions](#processingAsVersions) is false


| name                  | purpose                                                                              | default                   |
|-----------------------|--------------------------------------------------------------------------------------|---------------------------|
| fromCommitId          | Starts the change log from this commit (inclusive)                                   | The oldest commit         |
| maxCommits            | Limits the number of commits produced                                                | 1000                      |
| toCommitId            | Ends the change log at this commit (inclusive). Takes precedence over [fromCommitId] | The most recent commit    |


## Output layout and presentation properties

| name                  | purpose                                                                              | default                   |
|-----------------------|--------------------------------------------------------------------------------------|---------------------------|
| currentBuildTagName   | Tag name to use when most recent commit is pseudo-tagged, (see [autoTagLatestCommit](#autoTagLatestCommit))| "current build            |
| labelGroups           | A mapping of label groups to labels                                                  | see property detail below |
| pullRequestTitle      | The title to use for the change log section containing pull requests                 | "Pull Requests"           |
| separatePullRequests  | If true, list PRs under their own heading, otherwise just list with other issues     | true                      |
| showDetail            | When true, show the full commit message in a detail section.                         | true                      |
| <a name="templateName"></a>templateName          | The name of the Velocity template to use for change log layout                       | "markdown.vm"             |
    

### labelGroups

Issues may have multiple labels, and it may not always be desirable to use every label in the change log presentation.

This property allows issues to be grouped by label - for example "testing" and "documentation" labels could be combined into a "quality" group. 

Insertion order is maintained, so that the order of presentation is the same as defined by this property.

If an issue has multiple labels, it may appear in multiple places in the output, depending on how the labels are grouped.

Duplicates within a group are ignored.

Default value for this property is `DefaultChangeLogConfiguration.defaultLabelGroups`

### showDetail

When true, show the full commit message in a detail section.  Also requires that the Velocity template uses this
property (the default template does)

## Output destination properties

| name                  | purpose                                                                              | default                   |
|-----------------------|--------------------------------------------------------------------------------------|---------------------------|
| outputFilename        |  The file name to use for the change log output.  Used with [outputTarget]           | "changelog.md"            |
| outputFileSpec        |  File to be used as output, when [outputTarget] is [OutputTarget.USE_FILE_SPEC]      | null                      |
| outputTarget        |  The output directory for the generated change log.  Used with [outputFilename]        | [OutputTarget.WIKI_ROOT]  |


<a name="outputFilename"></a>
### outputFilename

The file name to use for the change log output.  Used in conjunction with [outputTarget](#outputTarget).  Default is "changelog.md".  By default therefore, a file named "changelog.md" is output to the root of the wiki directory
<a name="outputTarget"></a>
### outputTarget

The output directory for the generated change log.  Used in conjunction with [outputFilename](#outputFilename).  Default is [OutputTarget.WIKI_ROOT].  By default therefore, a file named "changelog.md" is output to the root of the wiki directory

### outputFileSpec

Required only when [outputTarget](#outputTarget) is [OutputTarget.USE_FILE_SPEC].  This property then points to the file which is to be used as output.  Default is null

## Commit comment control properties

| name                  | purpose                                                                              | default                   |
|-----------------------|--------------------------------------------------------------------------------------|---------------------------|
| correctTypos          |  If true, the [typoMap] is used to correct typos, otherwise no corrections are made. | false                     |
| exclusionTags         |  An exclusionTag stops a commit message from being processed into the Change Log.    | empty - no exclusions     |
| typoMap               |  Typo corrections, used only if [correctTypos] is true.                              | defaultTypoMap            |


### exclusionTags
    
An exclusionTag stops a commit message from being processed into the Change Log.  Any commit comment which
contains an exclusion tag is just ignored.

For example, you may not want to include commits which are javadoc changes only - to do so, specify 

> "{{javadoc}}"

as an exclusion tag, and include it in any commit comments as needed.

By default, there are no tags, and therefore all commit comments are included in the change log.

### typoMap

If [correctTypos] is true, git commit text is searched for each key in this map, and replaced by its associated value
Default is [DefaultChangeLogConfiguration.defaultTypoMap] - these are limited to looking for occasions where the space has been missed out of an issue reference - for example: 'Fix#5' gets corrected to 'Fix #5'.  

Of course, the original commit comment remains unchanged, these corrections apply only to the change log output.
    
