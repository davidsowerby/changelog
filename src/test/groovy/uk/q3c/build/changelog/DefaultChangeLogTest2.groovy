package uk.q3c.build.changelog

import com.google.common.collect.ImmutableList
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitBranch
import uk.q3c.build.gitplus.local.WikiLocal
import uk.q3c.util.testutil.FileTestUtil

import java.nio.file.Paths

import static uk.q3c.build.changelog.OutputTarget.*

/**
 * Tests for correct extraction of Git commit comments, and any issue references within them
 *
 * Created by David Sowerby on 07 Mar 2016
 */
class DefaultChangeLogTest2 extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp
    ChangeLog changeLog
    GitPlus gitPlus = Mock(GitPlus)
    ChangeLogConfiguration configuration
    MockGitLocal gitLocal
    WikiLocal wikiLocal = Mock(WikiLocal)
    MockGitRemote mockRemote = new MockGitRemote()
    VersionHistoryBuilder historyBuilder
    IssueRecords issueRecords = new DefaultIssueRecords()
    FileLocator fileLocator = new DefaultFileLocator()

    def setup() {
        temp = temporaryFolder.getRoot()
        gitLocal = new MockGitLocal()
        gitLocal.projectDirParent(temporaryFolder.getRoot())
        gitPlus.local >> gitLocal
        gitPlus.remote >> mockRemote
        gitPlus.wikiLocal >> wikiLocal
        gitLocal.currentBranch() >> new GitBranch("develop")
        gitLocal.branches() >> ImmutableList.of('master', 'develop')
        mockRemote.createIssues(1)
        configuration = new DefaultChangeLogConfiguration()
        historyBuilder = new DefaultVersionHistoryBuilder(fileLocator)
        changeLog = new DefaultChangeLog(gitPlus, configuration, historyBuilder, issueRecords, fileLocator)
        configuration.projectName = "dummy"
        configuration.remoteRepoUser = "davidsowerby"
    }

    def "output generated from default settings (except target), latest commit version tagged"() {
        given:
        gitLocal.projectName("Dummy")
        gitLocal.createVersionTag('2.0', 0, 'version 2.0')
        gitLocal.createVersionTag('1.1.0.1', 1, 'version 1.1.0.1')
        gitLocal.createVersionTag('0.0.5.1', 4, 'prep')
        gitLocal.createVersionTag('0.0.4.1', 5, 'prep')
        gitLocal.createVersionTag('0.0.3.1', 7, 'prep')
        gitLocal.createVersionTag('0.0.2.1', 9, 'prep')
        configuration.outputTarget(PROJECT_ROOT).correctTypos(true).projectDirParent(temp)
        changeLog = new DefaultChangeLog(gitPlus, configuration, historyBuilder, issueRecords, fileLocator)
        String expected = 'changelog.md'
        File expectedResult = testResource(expected)

        when:
        changeLog.generate()

        then:
        !FileTestUtil.compare(changeLog.outputFile(), expectedResult).isPresent()
    }

    def "no typo correction, latest build not versioned, detail suppressed"() {
        given:
        gitLocal.currentBranch = "master"
        gitLocal.projectName("Dummy")
        gitLocal.createVersionTag('2.0', 1, 'version 2.0')
        gitLocal.createVersionTag('1.1.0.1', 2, 'version 1.1.0.1')
        gitLocal.createVersionTag('0.0.5.1', 4, 'prep')
        gitLocal.createVersionTag('0.0.4.1', 5, 'prep')
        gitLocal.createVersionTag('0.0.3.1', 7, 'prep')
        gitLocal.createVersionTag('0.0.2.1', 9, 'prep')
        configuration.outputTarget(PROJECT_ROOT).correctTypos(false).showDetail(false).projectDirParent(temp)
        changeLog = new DefaultChangeLog(gitPlus, configuration, historyBuilder, issueRecords, fileLocator)
        String expected = 'changelog2.md'
        File expectedResult = testResource(expected)

        when:
        changeLog.generate()

        then:
        !FileTestUtil.compare(changeLog.outputFile(), expectedResult, 4).isPresent() //cannot compare line 4, date changes
        FileUtils.readLines(changeLog.outputFile()).get(4).startsWith('# [current build](https://github.com/davidsowerby/dummy/tree/)')
    }


    private String hash(int key) {
        return DigestUtils.sha1Hex(Integer.toString(key))
    }

    private File testResource(String fileName) {
        URL url = this.getClass()
                .getResource(fileName)
        return Paths.get(url.toURI())
                .toFile()
    }


}