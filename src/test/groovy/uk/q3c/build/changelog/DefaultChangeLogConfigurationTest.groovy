package uk.q3c.build.changelog

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import kotlin.UninitializedPropertyAccessException
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.DefaultGitPlus
import uk.q3c.build.gitplus.local.GitBranch
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.local.WikiLocal
import uk.q3c.build.gitplus.remote.GitRemoteProvider
import uk.q3c.build.gitplus.remote.ServiceProvider
import uk.q3c.build.gitplus.remote.github.GitHubRemote

import static uk.q3c.build.changelog.ConstantsKt.getNotSpecified

/**
 * Created by David Sowerby on 13 Mar 2016
 */
class DefaultChangeLogConfigurationTest extends Specification {


    DefaultChangeLogConfiguration config
    DefaultGitPlus gitPlus
    GitLocal gitLocal = Mock(GitLocal)
    WikiLocal wikiLocal = Mock(WikiLocal)
    GitHubRemote defaultRemote = Mock(GitHubRemote)
    GitRemoteProvider remoteProvider = Mock(GitRemoteProvider)
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    def setup() {
        config = new DefaultChangeLogConfiguration()
        config.projectName = "dummy"
        config.remoteRepoUser = "davidsowerby"
        remoteProvider.getDefault() >> defaultRemote
        remoteProvider.defaultProvider() >> ServiceProvider.GITHUB
        defaultRemote.repoUser(_) >> defaultRemote
        defaultRemote.repoName(_) >> defaultRemote
        gitPlus = new DefaultGitPlus(gitLocal, wikiLocal, remoteProvider)
        gitPlus.remote.repoUser('davidsowerby').repoName('scratch')
    }

    def "defaults"() {
        expect:

        // Version or commit range properties - see interface javadoc for order of priorities.
        config.fromVersionId == notSpecified
        config.toVersionId == notSpecified
        config.fromCommitId == notSpecified
        config.toCommitId == notSpecified
        config.branch == new GitBranch("develop")
        config.maxVersions == 50
        config.maxCommits == 1000
        config.autoTagLatestCommit

        // Output layout and presentation properties
        config.separatePullRequests
        config.pullRequestTitle == DefaultChangeLogConfiguration.DEFAULT_PULL_REQUESTS_TITLE
        config.templateName == DefaultChangeLogConfiguration.DEFAULT_TEMPLATE
        config.labelGroups == DefaultChangeLogConfiguration.defaultLabelGroups

        // Output destination
        config.outputFilename == 'changelog.md'
        config.outputTarget == OutputTarget.WIKI_ROOT

        //commit control
        config.typoMap == DefaultChangeLogConfiguration.defaultTypoMap
        !config.correctTypos

        config.exclusionTags.isEmpty()

        config.versionTagFilter instanceof AllTagsAreVersionsTagFilter
        config.currentBuildTagName == "current build"

    }

    def "defaults with late init"() {
        when:
        config.outputFileSpec

        then:
        thrown UninitializedPropertyAccessException
    }

    def "set get"() {
        given:
        String fromId = "fromId"
        String toId = "toId"
        String fromCommit = "ffg"
        String toCommit = "fsfg"
        GitBranch branch = new GitBranch("master")
        int nVersions = 8
        int nCommits = 3
        String filename = "any"
        OutputTarget target = OutputTarget.PROJECT_BUILD_ROOT
        File outputFileSpec = new File("/user/home/wiggly")
        Map<String, String> typoMap = ImmutableMap.of()
        Set<String> exclusionTags = ImmutableSet.of()
        String pullRequestTitle = "rrrrrrrrr"
        String templateName = "tname"
        Map<String, Set<String>> labelGroups = ImmutableMap.of()
        VersionTagFilter tagFilter = Mock(VersionTagFilter)
        String currentBuildTag = 'unreleased'

        when:
        config
                .fromVersionId(fromId)
                .toVersionId(toId)
                .fromCommitId(fromCommit)
                .toCommitId(toCommit)
                .branch(branch)
                .maxVersions(nVersions)
                .maxCommits(nCommits)
                .separatePullRequests(false)
                .pullRequestTitle(pullRequestTitle)
                .templateName(templateName)
                .labelGroups(labelGroups)
                .outputFilename(filename)
                .outputTarget(OutputTarget.PROJECT_BUILD_ROOT)
                .outputFileSpec(outputFileSpec)
                .exclusionTags(exclusionTags)
                .typoMap(typoMap)
                .correctTypos(true)
                .versionTagFilter(tagFilter)
                .autoTagLatestCommit(false)
                .currentBuildTagName(currentBuildTag)

        then:
        // Version or commit range properties - see interface javadoc for order of priorities.
        config.fromVersionId == fromId
        config.toVersionId == toId
        config.fromCommitId == fromCommit
        config.toCommitId == toCommit
        config.branch == branch
        config.maxVersions == nVersions
        config.maxCommits == nCommits
        !config.separatePullRequests
        config.pullRequestTitle == pullRequestTitle
        config.templateName == templateName
        config.labelGroups == labelGroups
        config.outputFilename == filename
        config.outputTarget == OutputTarget.PROJECT_BUILD_ROOT
        config.outputFileSpec == outputFileSpec
        config.typoMap == typoMap
        config.correctTypos
        config.versionTagFilter == tagFilter
        !config.autoTagLatestCommit
        config.exclusionTags == exclusionTags
        config.currentBuildTagName == currentBuildTag
    }

    def "versions or commits"() {
        when:
        true //do nothing

        then: "default"
        config.processingAsVersions

        when:
        config.processAsCommits()

        then:
        !config.processingAsVersions

        when:
        config.processAsVersions()

        then:
        config.processingAsVersions

    }

    def "validate with maxVersions and maxCommits <=0 throw exception"() {
        given:
        config.maxCommits(0).maxVersions(0)

        when:
        config.validate()

        then:
        thrown ChangeLogConfigurationException

        when:
        config.maxCommits(1).maxVersions(0)
        config.validate()

        then:
        noExceptionThrown()

        when:
        config.maxCommits(0).maxVersions(1)
        config.validate()

        then:
        noExceptionThrown()
    }

    def "validate with missing projectName or remoteRepoUser throws Exception"() {
        when:
        config.projectName = notSpecified
        config.validate()

        then:
        thrown ChangeLogConfigurationException

        when:
        config.projectName = "dummy"
        config.remoteRepoUser = notSpecified
        config.validate()

        then:
        thrown ChangeLogConfigurationException

        when:
        config.remoteRepoUser = "davidsowerby"
        config.validate()

        then:
        noExceptionThrown()

    }

    def "set exclusion tags"() {
        when:
        true

        then: "default"
        config.exclusionTags.isEmpty()

        when:
        config.exclusionTags(ImmutableSet.of("wiggly"))

        then:
        config.exclusionTags == ImmutableSet.of("wiggly")

        when:
        config.exclusionTags("a", "b")

        then:
        config.exclusionTags == ImmutableSet.of("a", "b")
    }
}
