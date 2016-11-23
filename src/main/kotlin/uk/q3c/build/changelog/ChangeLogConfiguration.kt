package uk.q3c.build.changelog

import uk.q3c.build.gitplus.local.GitBranch
import java.io.File

/**
 * Configures a [ChangeLog] output.  The range of output is defined in terms of either version or commits - only one or
 * the other can be used for a generation run
 *
 * A version is defined by Git tags - tags can be filtered by instances of [VersionTagFilter] to ensure that
 * only relevant tags are identified as versions, see [fromVersionId] and [toVersionId]
 *
 * Output range can be expressed as commit ids, see [fromCommitId] and [toCommitId].
 *
 * The maximum output can be limited by [maxVersions] and [maxCommits] respectively
 *
 * Support for producing output for only the most recent versions or commits is provided by [maxVersions] and [maxCommits]
 * respectively
 *
 * If the default range settings are used, the most recent versions will be produced, up to a maximum of 50
 *
 *
 *
 * Created by David Sowerby on 12 Nov 2016
 */
interface ChangeLogConfiguration {

    //************************************************************************************************************
    //  Properties - each also has a fluent setter
    //************************************************************************************************************

    // ===========================================================================================================
    // Version or commit range properties - see interface javadoc for order of priorities.
    // Unused settings are simply left as 'unspecified'
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
     * Used only when [processingAsVersions] is true.  Limits the number of versions produced.  Default is 1000
     */
    var maxVersions: Int

    /**
     * Used only when [processingAsVersions] is false.  Limits the number of commits processed.  Default is 0
     */
    var maxCommits: Int


    // ===========================================================================================================
    // Output layout and presentation properties
    // ===========================================================================================================

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
     * Implementation should use a [LinkedHashMap] to retain insertion order - this therefore also determines the
     * order of presentation.  Note that if an issues has multiple labels, it may appear in multiple places in the output,
     * depending on how the labels are grouped.  Duplicates within a group are ignored.
     *
     * Default is [DefaultChangeLogConfiguration.defaultLabelGroups]
     */
    var labelGroups: Map<String, Set<String>>

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

    fun toVersionId(versionId: String): ChangeLogConfiguration
    /**
     * Sets [processingAsVersions] to true
     */
    fun processAsVersions()

    /**
     * Sets [processingAsVersions] to false
     */
    fun processAsCommits()

    // ===========================================================================================================
    //    Other functions
    // ===========================================================================================================

    /**
     * Validates the configuration
     *
     * @throws ChangeLogConfigurationException if invalid
     */
    fun validate()


}

/**
 * Used in conjunction with the [ChangeLogConfiguration.outputFile] method to define where the output file is placed
 */
enum class OutputTarget {
    PROJECT_ROOT, PROJECT_BUILD_ROOT, WIKI_ROOT, CURRENT_DIR, USE_FILE_SPEC
}