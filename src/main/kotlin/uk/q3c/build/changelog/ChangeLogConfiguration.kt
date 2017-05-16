package uk.q3c.build.changelog

import uk.q3c.build.gitplus.local.GitBranch
import java.io.File

/**
 * Configures a [ChangeLog] output.
 *
 * See [user documentation](http://ds-changelog.readthedocs.io/en/develop/) for detail
 *
 * Created by David Sowerby on 12 Nov 2016
 */
interface ChangeLogConfiguration {

    //************************************************************************************************************
    //  Properties - grouped into functional areas.  Each also has a fluent setter
    //************************************************************************************************************

    // ===========================================================================================================
    //  Project identification
    // ===========================================================================================================

    var projectName: String

    /**
     * Combines with [projectName] to form identity of remote repository, for example 'davidsowerby/krail'
     */
    var remoteRepoUser: String


    /**
     * Used with [projectName] to identify the project directory
     */
    var projectDirParent: File


    // ===========================================================================================================
    // Version or commit range properties
    // ===========================================================================================================

    /**
     * If true, tags are evaluated as versions (using [versionTagFilter]) and the output collated into versions.
     * The range of versions selected, depends on [fromVersionId], [toVersionId] and [maxVersions]
     *
     * If false, commits are processed as though they belong to a single version, and if no version tag is present on
     * the most recent commit selected, then a [CommitRangeTag] is used as a placeholder.  The range of commits selected
     * depends on [fromCommitId], [toCommitId] and [maxCommits]
     *
     * Default is true
     */
    var processingAsVersions: Boolean

    /**
     * When [processingAsVersions] is true, if the latest commit does not have a version tag, and this property is true,
     * a pseudo tag is associated with the latest commit.  This can be useful where a changelog is generated before a tag
     * is applied - although it should be noted that the changelog will show the final version as "current build" because
     * it cannot know what the version should be.
     *
     * When [processingAsVersions] is false (that is, versions are ignored and just commits used), this property is not used.
     * Processing as commits always uses a [CommitRangeTag] if no version tag is present
     *
     * Default is true
     */
    var autoTagLatestCommit: Boolean

    /**
     * Starts the change log from this version (inclusive).  A version is taken from the Git tag name
     */
    var fromVersionId: String

    /**
     * Ends the change log output at this version (inclusive).  A version is taken from the Git tag name
     */
    var toVersionId: String

    /**
     *  Starts the change log from this commit (inclusive)
     *
     */
    var fromCommitId: String

    /**
     * Ends the change log output at this commit (inclusive)
     */
    var toCommitId: String

    /**
     * The branch to take commits / versions from
     */
    var branch: GitBranch

    /**
     * Used only when [processingAsVersions] is true.  Limits the number of versions produced.  Default is 50
     */
    var maxVersions: Int

    /**
     * Used only when [processingAsVersions] is false.  Limits the number of commits processed.  Default is 1000
     */
    var maxCommits: Int

    /**
     * Identifies which tags are versions tags.  Default is to treat all tags as version tags. To treat tags differently,
     * provide your own implementation of [VersionTagFilter] and assign an instance to this property.
     */
    var versionTagFilter: VersionTagFilter


    // ===========================================================================================================
    // Output layout and presentation properties
    // ===========================================================================================================

    /**
     * When the most recent build is not tagged, a "pseudo tag" is added (if [autoTagLatestCommit] is true).  The name of this tag
     * is defeind by this property
     */
    var currentBuildTagName: String

    /**
     * If true, pull requests are extracted under their own heading in the change log.  If false, they are merged with other issues according to the labels
     * attached to them
     */
    var separatePullRequests: Boolean

    /**
     * The title to use for the change log section containing pull requests. Default is
     * [DefaultChangeLogConfiguration.DEFAULT_PULL_REQUESTS_TITLE]
     */
    var pullRequestTitle: String

    /**
     * The name of the Velocity template to use for change log layout
     */
    var templateName: String

    /**
     * A mapping of label groups to labels, so that issues bearing the same label are grouped together in the output
     * Implementation should retain insertion order - this map also determines the order of presentation.  If an issue
     * has multiple labels, it may appear in multiple places in the output, depending on how the labels are grouped.
     * Duplicates within a group are ignored.
     *
     * Default is [DefaultChangeLogConfiguration.defaultLabelGroups]
     */
    var labelGroups: Map<String, Set<String>>

    /**
     * When true, show the full commit message in a detail section.  Also requires that the Velocity template uses this
     * property (the default template does)
     */
    var showDetail: Boolean

    // ===========================================================================================================
    // Output destination properties
    // ===========================================================================================================

    /**
     * The file name to use for the change log output.  Used in conjunction with [outputTarget]
     */
    var outputFilename: String
    /**
     * The output directory for the generated change log.  Used in conjunction with [outputFilename].  Default is
     * [OutputTarget.WIKI_ROOT]
     */
    var outputTarget: OutputTarget
    /**
     * Required only when [outputTarget] is [OutputTarget.USE_FILE_SPEC].  this property then points to the file
     * which is ued as output.
     */
    var outputFileSpec: File

    // ===========================================================================================================
    // Commit comment control properties
    // ===========================================================================================================

    /**
     * An exclusionTag stops a commit message from being processed into the Change Log.  Any commit comment which
     * contains an exclusion tag is just ignored.
     *
     * For example, you may not want to include commits which are javadoc changes only - to do so, specify "{{javadoc}}"
     * as an exclusion tag, and include it in any commit comments as needed.
     *
     * By default, there are no tags, and therefore all commit comments are included in the change log.
     */
    var exclusionTags: Set<String>

    /**
     * If [correctTypos] is true, git commit text is searched for each key in this map, and replaced by its associated value
     * Default is [DefaultChangeLogConfiguration.defaultTypoMap]
     */
    var typoMap: Map<String, String>

    /**
     * If true, the [typoMap] is used to correct typos, otherwise no corrections are made.  Default is false
     */
    var correctTypos: Boolean


    // ===========================================================================================================
    //  Fluent property 'setters'.  See properties of the same name for an explanation
    // ===========================================================================================================

    fun autoTagLatestCommit(value: Boolean): ChangeLogConfiguration

    fun outputTarget(outputTarget: OutputTarget): ChangeLogConfiguration

    fun outputFilename(outputFile: String): ChangeLogConfiguration

    fun correctTypos(correctTypos: Boolean): ChangeLogConfiguration

    fun typoMap(typoMap: Map<String, String>): ChangeLogConfiguration

    fun exclusionTags(vararg tag: String): ChangeLogConfiguration

    fun exclusionTags(exclusionTags: Set<String>): ChangeLogConfiguration

    fun separatePullRequests(separatePullRequests: Boolean): ChangeLogConfiguration

    fun labelGroups(labelGroups: Map<String, Set<String>>): ChangeLogConfiguration

    fun templateName(templateName: String): ChangeLogConfiguration

    fun pullRequestTitle(pullRequestTitle: String): ChangeLogConfiguration

    fun outputFileSpec(outputFileSpec: File): ChangeLogConfiguration

    fun branch(branch: GitBranch): ChangeLogConfiguration

    fun maxVersions(numberOfVersions: Int): ChangeLogConfiguration

    fun maxCommits(numberOfCommits: Int): ChangeLogConfiguration

    fun fromCommitId(commitId: String): ChangeLogConfiguration

    fun toCommitId(commitId: String): ChangeLogConfiguration

    fun fromVersionId(versionId: String): ChangeLogConfiguration

    fun showDetail(value: Boolean): ChangeLogConfiguration

    fun toVersionId(versionId: String): ChangeLogConfiguration

    fun versionTagFilter(versionTagFilter: VersionTagFilter): ChangeLogConfiguration

    fun remoteRepoUser(remoteRepoUser: String): ChangeLogConfiguration

    fun projectName(projectName: String): ChangeLogConfiguration

    fun projectDirParent(projectDirParent: File): ChangeLogConfiguration

    fun currentBuildTagName(tagName: String)
    /**
     * Sets [processingAsVersions] to true - but as that is the default, you will rarely need this
     */
    fun processAsVersions(): ChangeLogConfiguration

    /**
     * Sets [processingAsVersions] to false
     */
    fun processAsCommits(): ChangeLogConfiguration


    // ===========================================================================================================
    //    Other functions
    // ===========================================================================================================

    /**
     * Validates the configuration
     *
     * @throws ChangeLogConfigurationException if invalid
     */
    fun validate()

    /**
     * Copies all values (safely where required) from [other] to this instance.  It would be easier to replace the whole instance,
     * but this causes Kotlin's delegation to fail in [ChangeLog]
     */
    fun copyFrom(other: ChangeLogConfiguration)
}

/**
 * Used in conjunction with the [ChangeLogConfiguration.outputFile] method to define where the output file is placed
 */
enum class OutputTarget {
    PROJECT_ROOT, PROJECT_BUILD_ROOT, WIKI_ROOT, CURRENT_DIR, USE_FILE_SPEC
}