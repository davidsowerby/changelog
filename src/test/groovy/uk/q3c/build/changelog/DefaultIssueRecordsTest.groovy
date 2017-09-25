package uk.q3c.build.changelog

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.GitPlusFactory
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.remote.GPIssue
import uk.q3c.build.gitplus.test.MocksKt

import static org.mockito.Mockito.*

/**
 * Created by David Sowerby on 20 Sep 2017
 */
class DefaultIssueRecordsTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp

    IssueRecords records
    GitPlus gitPlus
    final String repoUser = "davidsowerby"
    final String repoName = "q3c-testutils"
    final String issue1Url = "https://github.com/davidsowerby/q3c-testutils/issues/1"

    def setup() {
        records = new DefaultIssueRecords()
        temp = temporaryFolder.getRoot()
    }

    def "get by number, empty cache"() {
        given:
        gitPlus = GitPlusFactory.instance
        gitPlus.propertiesFromGradle()
        gitPlus.useRemoteOnly(repoUser, repoName)
        gitPlus.execute()

        when: "issue requested"
        records.getIssue(gitPlus, 1)

        then: "issue is retrieved from remote"
        records.isCached(issue1Url)
    }

    def "get by number, already in cache"() {
        given:
        GPIssue issue1 = new GPIssue(1)
        issue1.htmlUrl = issue1Url

        gitPlus = MocksKt.mockGitPlusWithMockConfig()
        when(gitPlus.remote.providerBaseUrl).thenReturn("github.com")
        when(gitPlus.remote.repoUser).thenReturn(repoUser)
        when(gitPlus.remote.repoName).thenReturn(repoName)
        when(gitPlus.remote.getIssue(repoUser, repoName, 1)).thenReturn(issue1)

        when: "issue is requested the first time"
        records.getIssue(gitPlus, 1)

        then: "issue is retrieved from remote"
        records.isCached(issue1Url)

        when: "issue is requested the second time"
        GPIssue returnedIssue = records.getIssue(gitPlus, 1)

        then: "remote is not accessed second time, but issue is returned"
        returnedIssue == issue1
        verify(gitPlus.remote, times(1)).getIssue(repoUser, repoName, 1) == null

    }

    def "round trip save and load"() {
        given:
        gitPlus = GitPlusFactory.instance
        gitPlus.propertiesFromGradle()
        gitPlus.useRemoteOnly(repoUser, repoName)
        gitPlus.execute()

        when: "issue requested"
        records.getIssue(gitPlus, 1)

        then: "issue is retrieved from remote"
        records.isCached(issue1Url)

        when:
        records.save(new File(temp, "issues.json"))
        records = new DefaultIssueRecords()
        records.load(new File(temp, "issues.json"))

        then: "new instance has issue loaded"
        records.isCached(issue1Url)
    }
}
