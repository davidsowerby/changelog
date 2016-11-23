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
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.build.gitplus.remote.GitRemoteProvider
import uk.q3c.build.gitplus.remote.ServiceProvider
import uk.q3c.build.gitplus.remote.github.GitHubRemote

/**
 * Created by David Sowerby on 13 Mar 2016
 */
class DefaultChangeLogConfigurationTest extends Specification {


    DefaultChangeLogConfiguration config;
    DefaultGitPlus gitPlus;
    GitLocal gitLocal = Mock(GitLocal)
    WikiLocal wikiLocal = Mock(WikiLocal)
    GitHubRemote defaultRemote = Mock(GitHubRemote)
    GitRemote mockRemote = new MockGitRemote()
    GitRemoteProvider remoteProvider = Mock(GitRemoteProvider)
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder();

    def setup() {
        config = new DefaultChangeLogConfiguration()
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
        config.fromVersionId == ConstantsKt.notSpecified
        config.toVersionId == ConstantsKt.notSpecified
        config.fromCommitId == ConstantsKt.notSpecified
        config.toCommitId == ConstantsKt.notSpecified
        config.branch == new GitBranch("develop")
        config.lastNVersions == -1
        config.lastNCommits == -1

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

        then:
        // Version or commit range properties - see interface javadoc for order of priorities.
        config.fromVersionId == fromId
        config.toVersionId == toId
        config.fromCommitId == fromCommit
        config.toCommitId == toCommit
        config.branch == branch
        config.lastNVersions == nVersions
        config.lastNCommits == nCommits
        !config.separatePullRequests
        config.pullRequestTitle == pullRequestTitle
        config.templateName == templateName
        config.labelGroups == labelGroups
        config.outputFilename == filename
        config.outputTarget == OutputTarget.PROJECT_BUILD_ROOT
        config.outputFileSpec == outputFileSpec
        config.typoMap == typoMap
        config.correctTypos

        config.exclusionTags == exclusionTags
    }


}
