package uk.q3c.build.changelog

import org.eclipse.jgit.lib.PersonIdent
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitCommit
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.local.Tag
import uk.q3c.build.gitplus.local.WikiLocal
import uk.q3c.build.gitplus.remote.GPIssue

import java.time.LocalDate
import java.time.ZoneId

import static uk.q3c.build.changelog.OutputTarget.*

/**
 * Created by David Sowerby on 07 Mar 2016
 */
class DefaultChangeLogTest extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    File temp

    DefaultChangeLog changeLog
    ChangeLogConfiguration changeLogConfiguration = new DefaultChangeLogConfiguration()
    GitPlus gitPlus = Mock(GitPlus)
    GitLocal gitLocal = Mock(GitLocal)
    WikiLocal wikiLocal = Mock(WikiLocal)
    List<Tag> tags
    List<GitCommit> commits
    List<GPIssue> issues
    List<Set<String>> labels
    final String projectFolderName = 'project'
    final String wikiFolderName = 'project.wiki'
    static File projectFolder
    static File wikiFolder
    PersonIdent personIdent = Mock(PersonIdent)
    VersionHistoryBuilder versionHistoryBuilder


    def setup() {

        tags = new ArrayList<>()
        temp = temporaryFolder.getRoot()
        gitPlus.local >> gitLocal
        gitPlus.wikiLocal >> wikiLocal
        projectFolder = new File(temp, projectFolderName)
        wikiFolder = new File(temp, wikiFolderName)
        gitLocal.projectDir() >> projectFolder
        wikiLocal.projectDir() >> wikiFolder
        personIdent.name >> "A Person"
        LocalDate localDate = LocalDate.of(2016, 2, 3)
        personIdent.when >> Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        personIdent.timeZone >> TimeZone.default
        versionHistoryBuilder = new DefaultVersionHistoryBuilder()

    }


    def "constructor uses provided configuration"() {

        given:
        changeLogConfiguration = new DefaultChangeLogConfiguration()

        when:
        DefaultChangeLog changelog = new DefaultChangeLog(gitPlus, changeLogConfiguration, versionHistoryBuilder)

        then:
        changelog.getConfiguration() == changeLogConfiguration
    }


    def "getOutputFile using FILE_SPEC"() {
        given:
        File fileSpec = new File(temp, 'changelog.md')
        changeLogConfiguration.templateName('markdown.vm').outputTarget(USE_FILE_SPEC).outputFileSpec(fileSpec)

        when:
        changeLog = new DefaultChangeLog(gitPlus, changeLogConfiguration, versionHistoryBuilder)

        then:
        changeLog.outputFile().equals(fileSpec)
    }

    def "getOutputFile using PROJECT_ROOT"() {
        given:
        changeLogConfiguration.templateName('markdown.vm').outputTarget(PROJECT_ROOT).outputFilename('changelog.md')
        changeLog = new DefaultChangeLog(gitPlus, changeLogConfiguration, versionHistoryBuilder)

        expect:
        changeLog.outputFile().equals(new File(projectFolder, 'changelog.md'))
    }

    def "getOutputFile using PROJECT_BUILD_ROOT"() {
        given:
        changeLogConfiguration.templateName('markdown.vm').outputTarget(PROJECT_BUILD_ROOT).outputFilename('changelog.md')
        changeLog = new DefaultChangeLog(gitPlus, changeLogConfiguration, versionHistoryBuilder)

        File buildFolder = new File(projectFolder, 'build')

        expect:
        changeLog.outputFile().equals(new File(buildFolder, 'changelog.md'))
    }

    def "getOutputFile using WIKI_ROOT"() {
        given:
        changeLogConfiguration.templateName('markdown.vm').outputTarget(WIKI_ROOT).outputFilename('changelog.md')
        changeLog = new DefaultChangeLog(gitPlus, changeLogConfiguration, versionHistoryBuilder)

        expect:
        changeLog.outputFile().equals(new File(wikiFolder, 'changelog.md'))
    }

    def "getOutputFile using CURRENT_DIR"() {
        given:
        changeLogConfiguration.templateName('markdown.vm').outputTarget(CURRENT_DIR).outputFilename('changelog.md')
        changeLog = new DefaultChangeLog(gitPlus, changeLogConfiguration, versionHistoryBuilder)

        File currentDir = new File('.')

        expect:
        changeLog.outputFile().equals(new File(currentDir, 'changelog.md'))
    }





}