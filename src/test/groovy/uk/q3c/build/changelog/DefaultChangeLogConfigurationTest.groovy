package uk.q3c.build.changelog

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.GitPlusFactory
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitBranch
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.local.WikiLocal
import uk.q3c.build.gitplus.remote.GitRemoteResolver
import uk.q3c.build.gitplus.remote.ServiceProvider
import uk.q3c.build.gitplus.remote.github.GitHubRemote

/**
 * Created by David Sowerby on 13 Mar 2016
 */
class DefaultChangeLogConfigurationTest extends Specification {


    DefaultChangeLogConfiguration config
    GitPlus gitPlus
    GitLocal gitLocal = Mock(GitLocal)
    WikiLocal wikiLocal = Mock(WikiLocal)
    GitHubRemote defaultRemote = Mock(GitHubRemote)
    GitRemoteResolver remoteResolver = Mock(GitRemoteResolver)
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()
    String notSpecified = ConstantsKt.notSpecified

    def setup() {
        config = new DefaultChangeLogConfiguration()
        config.projectName = "dummy"
        config.remoteRepoUser = "davidsowerby"
        remoteResolver.getDefault() >> defaultRemote
        remoteResolver.defaultProvider() >> ServiceProvider.GITHUB
        defaultRemote.repoUser(_) >> defaultRemote
        defaultRemote.repoName(_) >> defaultRemote
        gitPlus = GitPlusFactory.instance
        gitPlus.remote.repoUser('davidsowerby').repoName('scratch')
    }

    def "defaults"() {
        expect:

        // Version or commit range properties - see interface javadoc for order of priorities.
        config.fromVersionId == notSpecified
        config.toVersionId == notSpecified
        config.fromCommitId == notSpecified
        config.toCommitId == notSpecified
        config.branch == ""
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
        config.outputDirectorySpec == new File(".")
        config.useStoredIssues
        config.storeIssuesLocally
        config.issuesFilename == "issueRecords.md"

        //commit control
        config.typoMap == DefaultChangeLogConfiguration.defaultTypoMap
        !config.correctTypos

        config.exclusionTags.isEmpty()

        config.versionTagFilter instanceof AllTagsAreVersionsTagFilter
        config.currentBuildTagName == "current build"

        // source
        config.projectDirParent == new File(".")

    }


    def "set get"() {
        given:
        String fromId = "fromId"
        String toId = "toId"
        String fromCommit = "ffg"
        String toCommit = "fsfg"
        String branch = "master"
        int nVersions = 8
        int nCommits = 3
        String filename = "any"
        OutputTarget target = OutputTarget.PROJECT_BUILD_ROOT
        File outputDirSpec = new File("/user/home/wiggly")
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
                .useStoredIssues(false)
                .storeIssuesLocally(false)
                .issuesFilename("other")
                .maxVersions(nVersions)
                .maxCommits(nCommits)
                .separatePullRequests(false)
                .pullRequestTitle(pullRequestTitle)
                .templateName(templateName)
                .labelGroups(labelGroups)
                .outputFilename(filename)
                .outputTarget(OutputTarget.PROJECT_BUILD_ROOT)
                .outputDirectorySpec(outputDirSpec)
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
        config.outputDirectorySpec == outputDirSpec
        config.typoMap == typoMap
        config.correctTypos
        config.versionTagFilter == tagFilter
        !config.autoTagLatestCommit
        config.exclusionTags == exclusionTags
        config.currentBuildTagName == currentBuildTag
        config.issuesFilename == "other"
        !config.useStoredIssues
        !config.storeIssuesLocally
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

    def "Json round trip"() {
        given:
        ObjectMapper objectMapper = new ObjectMapper()
        StringWriter sw = new StringWriter()
        //do not want all defaults for test
        config.branch = new GitBranch("wiggly")
        config.projectName = "beanbag"

        when:
        objectMapper.writeValue(sw, config)
        DefaultChangeLogConfiguration config2 = objectMapper.readValue(sw.toString(), DefaultChangeLogConfiguration.class)

        then:
        config2 == config
    }

    def "copyFrom"() {
        given:
        DefaultChangeLogConfiguration config2 = new DefaultChangeLogConfiguration()

        when:
        config2.copyFrom(config)

        then: "defaults match"
        config == config2

        when: "some changes to defaults"
        config.branch = new GitBranch("wiggly")
        config.projectName = "beanbag"
        config2.copyFrom(config)

        then: "changes match"
        config == config2
    }
}
