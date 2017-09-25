package uk.q3c.build.changelog

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.DefaultGitLocalConfiguration
import uk.q3c.build.gitplus.local.GitLocalConfiguration
import uk.q3c.build.gitplus.local.WikiLocal
import uk.q3c.build.gitplus.test.MocksKt

import static org.mockito.Mockito.*

/**
 * Created by David Sowerby on 24 Sep 2017
 */
class DefaultFileLocatorTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp
    FileLocator locator
    DefaultChangeLogConfiguration configuration
    GitPlus gitPlus
    File projectDirParent
    String projectName
    WikiLocal wikiLocal
    GitLocalConfiguration wikiConfiguration

    def setup() {
        temp = temporaryFolder.getRoot()
        locator = new DefaultFileLocator()
        configuration = new DefaultChangeLogConfiguration()
        projectDirParent = temp
        projectName = "wiggly"
        gitPlus = MocksKt.mockGitPlusWithDataConfig()
        wikiLocal = mock(WikiLocal)
        wikiConfiguration = new DefaultGitLocalConfiguration()
        when(gitPlus.wikiLocal).thenReturn(wikiLocal)
        when(gitPlus.wikiLocal.configuration).thenReturn(wikiConfiguration)
        when(gitPlus.local.projectDir()).thenReturn(new File(temp, projectName))
        when(gitPlus.wikiLocal.projectDir()).thenReturn(new File(temp, "wiggly-wiki"))
    }

    def "config defaults (WIKI_ROOT)"() {
        given: "using defaults"
        File changeLogFile = new File(gitPlus.wikiLocal.projectDir(), "changelog.md")
        File issuesFile = new File(gitPlus.wikiLocal.projectDir(), "issueRecords.md")


        expect:
        locator.locateChangeLogFile(configuration, gitPlus) == changeLogFile
        locator.locateIssueRecordsFile(configuration, gitPlus) == issuesFile
    }

    def "BUILD_ROOT"() {
        given: "using defaults"
        configuration.outputTarget = OutputTarget.PROJECT_BUILD_ROOT
        File changeLogFile = new File(gitPlus.local.projectDir(), "build/changelog.md")
        File issuesFile = new File(gitPlus.local.projectDir(), "build/issueRecords.md")


        expect:
        locator.locateChangeLogFile(configuration, gitPlus) == changeLogFile
        locator.locateIssueRecordsFile(configuration, gitPlus) == issuesFile
    }

    def "PROJECT_ROOT"() {
        given: "using defaults"
        configuration.outputTarget = OutputTarget.PROJECT_ROOT
        File changeLogFile = new File(gitPlus.local.projectDir(), "changelog.md")
        File issuesFile = new File(gitPlus.local.projectDir(), "issueRecords.md")


        expect:
        locator.locateChangeLogFile(configuration, gitPlus) == changeLogFile
        locator.locateIssueRecordsFile(configuration, gitPlus) == issuesFile
    }

    def "CURRENT_DIR"() {
        given: "using defaults"
        configuration.outputTarget = OutputTarget.CURRENT_DIR
        File currentDir = new File(".")
        File changeLogFile = new File(currentDir, "changelog.md")
        File issuesFile = new File(currentDir, "issueRecords.md")


        expect:
        locator.locateChangeLogFile(configuration, gitPlus) == changeLogFile
        locator.locateIssueRecordsFile(configuration, gitPlus) == issuesFile
    }

    def "USE_DIRECTORY_SPEC, default is current dir"() {
        given: "using defaults"
        configuration.outputTarget = OutputTarget.USE_DIRECTORY_SPEC
        File currentDir = new File(".")
        File changeLogFile = new File(currentDir, "changelog.md")
        File issuesFile = new File(currentDir, "issueRecords.md")


        expect:
        locator.locateChangeLogFile(configuration, gitPlus) == changeLogFile
        locator.locateIssueRecordsFile(configuration, gitPlus) == issuesFile
    }

    def "USE_DIRECTORY_SPEC, set"() {
        given: "changed from default"
        configuration.outputTarget = OutputTarget.USE_DIRECTORY_SPEC
        File outputDir = new File(temp, "somewhereForOutput")
        configuration.outputDirectorySpec = outputDir
        File changeLogFile = new File(outputDir, "changelog.md")
        File issuesFile = new File(outputDir, "issueRecords.md")


        expect:
        locator.locateChangeLogFile(configuration, gitPlus) == changeLogFile
        locator.locateIssueRecordsFile(configuration, gitPlus) == issuesFile
    }
}
