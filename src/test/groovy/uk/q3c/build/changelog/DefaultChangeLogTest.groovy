package uk.q3c.build.changelog

import com.google.common.collect.ImmutableList
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitBranch
import uk.q3c.build.gitplus.local.GitCommit
import uk.q3c.build.gitplus.local.WikiLocal
import uk.q3c.build.gitplus.test.MocksKt

import static org.mockito.Mockito.*

/**
 * Created by David Sowerby on 07 Mar 2016
 */
class DefaultChangeLogTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp

    DefaultChangeLog changelog
    ChangeLogConfiguration changeLogConfiguration = new DefaultChangeLogConfiguration()
    GitPlus gitPlus = Mock(GitPlus)
    WikiLocal wikiLocal = mock(WikiLocal)


    final String projectName = 'project'

    VersionHistoryBuilder versionHistoryBuilder
    FileLocator fileLocator = new DefaultFileLocator()
    IssueRecords issueRecords = Mock(IssueRecords)
    ImmutableList<GitCommit> commits

    def setup() {

        temp = temporaryFolder.getRoot()

        commits = ImmutableList.of()
        gitPlus = MocksKt.mockGitPlusWithDataConfig()
        when(gitPlus.wikiLocal).thenReturn(wikiLocal)
        when(gitPlus.local.extractCommitsFor(new GitBranch("develop"))).thenReturn(commits)
        changeLogConfiguration.projectName = projectName
        changeLogConfiguration.remoteRepoUser = "davidsowerby"
//        gitPlus.local.configuration.projectName=projectName
        versionHistoryBuilder = new DefaultVersionHistoryBuilder(fileLocator)

    }


    def "constructor uses provided configuration"() {

        given:
        changeLogConfiguration = new DefaultChangeLogConfiguration()

        when:
        changelog = new DefaultChangeLog(gitPlus, changeLogConfiguration, versionHistoryBuilder, issueRecords, fileLocator)

        then:
        changelog.getConfiguration() == changeLogConfiguration
    }

    def "issue records, load and save defaults"() {

        given: "defaults is to use stored issue records"
        DefaultChangeLog changelog = new DefaultChangeLog(gitPlus, changeLogConfiguration, versionHistoryBuilder, issueRecords, fileLocator)

        when: "useStoredIssues is true"
        changelog.generate()

        then:
        1 * issueRecords.load(_)
        1 * issueRecords.save(_)
    }

    def "do not load issue records when useStoredIssues false"() {

        given: "defaults is to use and store issue records"
        DefaultChangeLog changelog = new DefaultChangeLog(gitPlus, changeLogConfiguration, versionHistoryBuilder, issueRecords, fileLocator)
        changeLogConfiguration.useStoredIssues = false
        changeLogConfiguration.storeIssuesLocally = false

        when: "useStoredIssues and storeIssuesLocally is false"
        changelog.generate()

        then:
        0 * issueRecords.load(_)
        0 * issueRecords.save(_)


    }


}