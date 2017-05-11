package uk.q3c.build.changelog

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import uk.q3c.build.gitplus.local.GitBranch
import java.io.File

/**
 * A execute object for [DefaultChangeLog]
 *
 *
 * Created by David Sowerby on 13 Mar 2016
 */


data class DefaultChangeLogConfiguration(override var projectName: String = notSpecified) : ChangeLogConfiguration {

    override var remoteRepoUser: String = notSpecified
    override var templateName = DEFAULT_TEMPLATE
    override var labelGroups: Map<String, Set<String>> = defaultLabelGroups
    override var separatePullRequests = true
    override var exclusionTags: Set<String> = ImmutableSet.of()

    override var typoMap: Map<String, String> = defaultTypoMap
    override var correctTypos = false
    override var outputFilename = "changelog.md"
    override var pullRequestTitle = DEFAULT_PULL_REQUESTS_TITLE
    override var outputTarget = OutputTarget.WIKI_ROOT
    override var outputFileSpec: File = File(".", outputFilename)
    override var fromCommitId = notSpecified
    override var toCommitId = notSpecified
    override var fromVersionId = notSpecified
    override var toVersionId = notSpecified
    override var branch: GitBranch = GitBranch("develop")
    override var autoTagLatestCommit = true
    override var maxCommits: Int = 1000
    override var maxVersions: Int = 50
    override var processingAsVersions: Boolean = true
    override var versionTagFilter: VersionTagFilter = AllTagsAreVersionsTagFilter()
    override var showDetail = true
    override var projectDirParent: File = File(".")
    override var currentBuildTagName = "current build"


    override fun processAsVersions(): ChangeLogConfiguration {
        processingAsVersions = true
        return this
    }

    override fun processAsCommits(): ChangeLogConfiguration {
        processingAsVersions = false
        return this
    }


    override fun validate() {
        if (maxVersions <= 0 && maxCommits <= 0) {
            throw ChangeLogConfigurationException("Both maxCommits and maxVersions are <=0.  No output would be produced")
        }
        if (projectName == notSpecified) {
            throw ChangeLogConfigurationException("projectName must be specified")
        }
        if (remoteRepoUser == notSpecified) {
            throw ChangeLogConfigurationException("remoteRepoUser must be specified")
        }

    }

    override fun autoTagLatestCommit(value: Boolean): ChangeLogConfiguration {
        this.autoTagLatestCommit = value
        return this
    }

    override fun fromCommitId(commitId: String): ChangeLogConfiguration {
        this.fromCommitId = commitId
        return this
    }

    override fun toCommitId(commitId: String): ChangeLogConfiguration {
        this.toCommitId = commitId
        return this
    }

    override fun fromVersionId(versionId: String): ChangeLogConfiguration {
        this.fromVersionId = versionId
        return this
    }

    override fun toVersionId(versionId: String): ChangeLogConfiguration {
        this.toVersionId = versionId
        return this
    }

    override fun outputFileSpec(outputFileSpec: File): ChangeLogConfiguration {
        this.outputFileSpec = outputFileSpec
        return this
    }

    override fun outputTarget(outputTarget: OutputTarget): ChangeLogConfiguration {
        this.outputTarget = outputTarget
        return this
    }


    override fun pullRequestTitle(pullRequestTitle: String): ChangeLogConfiguration {
        this.pullRequestTitle = pullRequestTitle
        return this
    }

    override fun templateName(templateName: String): ChangeLogConfiguration {
        this.templateName = templateName
        return this
    }


    override fun labelGroups(labelGroups: Map<String, Set<String>>): ChangeLogConfiguration {
        this.labelGroups = labelGroups
        return this
    }


    override fun separatePullRequests(separatePullRequests: Boolean): ChangeLogConfiguration {
        this.separatePullRequests = separatePullRequests
        return this
    }


    override fun exclusionTags(exclusionTags: Set<String>): ChangeLogConfiguration {
        this.exclusionTags = exclusionTags
        return this
    }

    override fun exclusionTags(vararg tag: String): ChangeLogConfiguration {
        this.exclusionTags = ImmutableSet.copyOf(tag)
        return this
    }


    override fun typoMap(typoMap: Map<String, String>): ChangeLogConfiguration {
        this.typoMap = typoMap
        return this
    }

    override fun correctTypos(correctTypos: Boolean): ChangeLogConfiguration {
        this.correctTypos = correctTypos
        return this
    }

    override fun outputFilename(outputFile: String): ChangeLogConfiguration {
        this.outputFilename = outputFile
        return this
    }

    override fun branch(branch: GitBranch): ChangeLogConfiguration {
        this.branch = branch
        return this
    }

    override fun maxVersions(numberOfVersions: Int): ChangeLogConfiguration {
        this.maxVersions = numberOfVersions
        return this
    }

    override fun currentBuildTagName(tagName: String) {
        this.currentBuildTagName = tagName
    }

    override fun maxCommits(numberOfCommits: Int): ChangeLogConfiguration {
        this.maxCommits = numberOfCommits
        return this
    }

    override fun versionTagFilter(versionTagFilter: VersionTagFilter): ChangeLogConfiguration {
        this.versionTagFilter = versionTagFilter
        return this
    }

    override fun showDetail(value: Boolean): ChangeLogConfiguration {
        this.showDetail = value
        return this
    }

    override fun remoteRepoUser(remoteRepoUser: String): ChangeLogConfiguration {
        this.remoteRepoUser = remoteRepoUser
        return this
    }

    override fun projectName(projectName: String): ChangeLogConfiguration {
        this.projectName = projectName
        return this
    }

    override fun projectDirParent(projectDirParent: File): ChangeLogConfiguration {
        this.projectDirParent = projectDirParent
        return this
    }

    companion object {

        val DEFAULT_PULL_REQUESTS_TITLE = "Pull Requests"
        val defaultTypoMap = ImmutableMap.Builder<String, String>().put("Fix#", "Fix #").put("fix#", "fix #").put("Fixes#", "Fixes #").put("fixes#", "fixes #").put("See#", "See #").put("see#", "see #").put("Close#", "Close #").put("close#", "close #").put("Closes#", "Closes #").put("closes#", "closes #").put("Resolve#", "Resolve #").put("resolve#", "resolve #").put("Resolves#", "Resolves #").put("resolves#", "resolves #").build()
        val defaultFixSet = ImmutableSet.of("bug")
        val defaultEnhancementSet: Set<String> = ImmutableSet.of("enhancement", "performance")
        val defaultDocumentationSet = ImmutableSet.of("documentation")
        val defaultTaskSet = ImmutableSet.of("task")
        val defaultQualitySet = ImmutableSet.of("testing", "quality")

        //    @formatter:off
        val defaultLabelGroups = ImmutableMap.Builder<String, Set<String>>()
                .put(DEFAULT_PULL_REQUESTS_TITLE, ImmutableSet.of<String>()) // pull requests do not need mapping
                .put("Fixes", defaultFixSet)
                .put("Quality", defaultQualitySet)
                .put("Enhancements", defaultEnhancementSet)
                .put("Tasks", defaultTaskSet)
                .put("Documentation", defaultDocumentationSet)
                .build()

        //    @formatter:on

        val DEFAULT_TEMPLATE = "markdown.vm"
    }
}
