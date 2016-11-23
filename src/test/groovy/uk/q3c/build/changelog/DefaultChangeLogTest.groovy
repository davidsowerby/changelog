package uk.q3c.build.changelog

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import org.apache.commons.codec.digest.DigestUtils
import org.eclipse.jgit.lib.PersonIdent
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll
import uk.q3c.build.gitplus.gitplus.DefaultGitPlus
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitCommit
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.local.Tag
import uk.q3c.build.gitplus.local.WikiLocal
import uk.q3c.build.gitplus.remote.GPIssue
import uk.q3c.build.gitplus.remote.GitRemote
import uk.q3c.util.testutil.FileTestUtil

import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

import static uk.q3c.build.changelog.DefaultChangeLogConfiguration.*

//import static uk.q3c.build.changelog.DefaultChangeLogConfiguration.DEFAULT_PULL_REQUESTS_TITLE
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
    GitRemote gitRemote = Mock(GitRemote)
    final String projectFolderName = 'project'
    final String wikiFolderName = 'project.wiki'
    static File projectFolder
    static File wikiFolder
    PersonIdent personIdent = Mock(PersonIdent)


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
        personIdent.when >> Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        personIdent.timeZone >> TimeZone.default

    }


    def "constructor uses provided configuration"() {

        given:
        changeLogConfiguration = new DefaultChangeLogConfiguration()

        when:
        DefaultChangeLog changelog = new DefaultChangeLog(gitPlus, changeLogConfiguration)

        then:
        changelog.getConfiguration() == changeLogConfiguration
    }


    def "not really a test, just checking test setup"() {
        given:
        createDataWithMostRecentCommitTagged()
        gitPlus()

        expect:
        gitPlus.local.tags().size() == 5
        commits.size() == 10
        tags.get(0).getCommit() == commits.get(1)
        tags.get(4).getCommit() == commits.get(9)
        gitPlus.local.extractDevelopCommits().iterator().next() == commits.get(9)
    }

    @Unroll
    def "latest commit is  tagged as version"() {
        given:
        changeLogConfiguration.outputTarget(USE_FILE_SPEC)
        changeLogConfiguration.fromCommitId = fromVersion
        changeLogConfiguration.toCommitId = toVersion
//        changeLogConfiguration.isFromVersion(fromVersion) >> isFromVersion
//        changeLogConfiguration.isToVersion(toVersion) >> isToVersion
        changeLogConfiguration.numberOfVersions = wantedVersions
        createDataWithMostRecentCommitTagged()
        gitPlus()
        changeLog = new DefaultChangeLog(gitPlus, changeLogConfiguration)
        File expectedResult = testResource(expected)

        when:
        changeLog.createChangeLog()

        then:
        changeLog.getVersionRecords().size() == numberOfVersions
        !FileTestUtil.compare(changeLog.outputFile(), expectedResult).isPresent()


        where:
        expected        | fromVersion    | numberOfVersions | fromLatestCommit | fromLatestVersion | isFromVersion | toVersion     | isToVersion | wantedVersions
        'changelog.md'  | LATEST_COMMIT  | 5                | true             | false             | false         | LATEST_COMMIT | false       | 0
        'changelog.md'  | LATEST_VERSION | 5                | false            | true              | false         | LATEST_COMMIT | false       | 0
        'changelog6.md' | '1.3.12'       | 4                | false            | false             | true          | LATEST_COMMIT | false       | 0
        'changelog7.md' | '1.3.12'       | 3                | false            | false             | true          | '0.2'         | true        | 0
        'changelog7.md' | '1.3.12'       | 3                | false            | false             | true          | LATEST_COMMIT | false       | 3
    }


    @Unroll
    def "latest commit is not tagged as version"() {
        given:
        changeLogConfiguration.getPullRequestTitle() >> DEFAULT_PULL_REQUESTS_TITLE
        changeLogConfiguration.getOutputTarget() >> USE_FILE_SPEC
        changeLogConfiguration.getFromCommitId() >> fromVersion
        changeLogConfiguration.getToCommitId() >> toVersion
        changeLogConfiguration.startFromLatestCommit() >> fromLatestCommit
        changeLogConfiguration.startFromLatestVersion() >> fromLatestVersion
        changeLogConfiguration.isFromVersion(fromVersion) >> isFromVersion
        changeLogConfiguration.isToVersion(toVersion) >> isToVersion
        changeLogConfiguration.getNumberOfVersions() >> wantedVersions
        createDataWithMostRecentCommitNotTagged()
        gitPlus()
        changeLog = new DefaultChangeLog(gitPlus, changeLogConfiguration)
        File expectedResult = testResource(expected)

        when:
        changeLog.createChangeLog()

        then:
        changeLog.getVersionRecords().size() == numberOfVersions
        !FileTestUtil.compare(changeLogConfiguration.getOutputFile(), expectedResult).isPresent()


        where:
        expected        | fromVersion                                  | numberOfVersions | fromLatestCommit | fromLatestVersion | isFromVersion | toVersion | isToVersion | wantedVersions
        'changelog2.md' | DefaultChangeLogConfiguration.LATEST_COMMIT  | 6                | true             | false             | false         | null      | false       | 0
        'changelog3.md' | DefaultChangeLogConfiguration.LATEST_VERSION | 5                | false            | true              | false         | null      | false       | 0
        'changelog4.md' | '1.3.12'                                     | 4                | false            | false             | true          | null      | false       | 0
        'changelog5.md' | '1.3.12'                                     | 3                | false            | false             | true          | '0.2'     | true        | 0
        'changelog5.md' | '1.3.12'                                     | 3                | false            | false             | true          | null      | false       | 3
    }

    def "getOutputFile using FILE_SPEC"() {
        given:
        File fileSpec = new File(temp, 'changelog.md')
        changeLogConfiguration.getOutputTarget() >> USE_FILE_SPEC
        changeLogConfiguration.getTemplateName() >> 'markdown.vm'
        changeLogConfiguration.outputFileSpec(fileSpec)

        when:
        changeLog = new DefaultChangeLog(gitPlus, changeLogConfiguration)

        then:
        changeLog.outputFile().equals(fileSpec)
    }

    def "getOutputFile using PROJECT_ROOT"() {
        given:
        changeLogConfiguration.getOutputTarget() >> PROJECT_ROOT
        changeLogConfiguration.getTemplateName() >> 'markdown.vm'
        changeLogConfiguration.getOutputFilename() >> 'changelog.md'
        changeLog = new DefaultChangeLog(gitPlus, changeLogConfiguration)

        expect:
        changeLog.outputFile().equals(new File(projectFolder, 'changelog.md'))
    }

    def "getOutputFile using PROJECT_BUILD_ROOT"() {
        given:
        changeLogConfiguration.getOutputTarget() >> PROJECT_BUILD_ROOT
        changeLogConfiguration.getTemplateName() >> 'markdown.vm'
        changeLogConfiguration.getOutputFilename() >> 'changelog.md'
        changeLog = new DefaultChangeLog(gitPlus, changeLogConfiguration)

        File buildFolder = new File(projectFolder, 'build')

        expect:
        changeLog.outputFile().equals(new File(buildFolder, 'changelog.md'))
    }

    def "getOutputFile using WIKI_ROOT"() {
        given:
        changeLogConfiguration.getOutputTarget() >> WIKI_ROOT
        changeLogConfiguration.getTemplateName() >> 'markdown.vm'
        changeLogConfiguration.getOutputFilename() >> 'changelog.md'
        changeLog = new DefaultChangeLog(gitPlus, changeLogConfiguration)

        expect:
        changeLog.outputFile().equals(new File(wikiFolder, 'changelog.md'))
    }

    def "getOutputFile using CURRENT_DIR"() {
        given:
        changeLogConfiguration.getOutputTarget() >> CURRENT_DIR
        changeLogConfiguration.getTemplateName() >> 'markdown.vm'
        changeLogConfiguration.getOutputFilename() >> 'changelog.md'
        changeLog = new DefaultChangeLog(gitPlus, changeLogConfiguration)

        File currentDir = new File('.')

        expect:
        changeLog.outputFile().equals(new File(currentDir, 'changelog.md'))
    }

    def "create changelog output to wiki is pushed to remote wiki"() {
        given:
        createDataWithMostRecentCommitNotTagged()
        gitPlus()
        wikiLocal.projectDir() >> temp
        gitPlus.getWikiLocal() >> wikiLocal

        changeLog = new DefaultChangeLog(gitPlus, changeLogConfiguration)

        when:
        changeLog.createChangeLog()

        then:
        1 * wikiLocal.add(_)
        1 * wikiLocal.commit('Auto generated changelog')
        1 * wikiLocal.push(false, false)
    }

    private void createDataWithMostRecentCommitNotTagged() {
        createCommits(2, 3, 5, 7, 8)
        issues.get(2).pullRequest(true)
    }

/**
 * This creates:<ol>
 * <li>release 0.1 with tag 0 at commit 1, version includes commit 0,1, issues 0,1,2</li>
 * <li>release 0.2 with tag 1 at commit 3, version includes commit 2,3, issues 3,4,5,6,7</li>
 * </ol>
 */
    private void createDataWithMostRecentCommitTagged() {
        createCommits(1, 3, 5, 8, 9)
        issues.get(2).pullRequest(true)
    }

/**
 * Commits 0-9, oldest to newest, hash the same as index.  Needs to be reversed to represent the default order returned by a call to Git log
 */
    private void createCommits(int ... tagged) {
        ZonedDateTime startCommitDate = ZonedDateTime.of(LocalDateTime.of(2015, 3, 4, 8, 0, 0, 0), ZoneId.of("Z"))
        createIssues()
        List<Integer> numberOfIssuesToAssign = ImmutableList.of(2, 1, 0, 5, 3, 3, 2, 1, 3, 0) //total 20
        Map<Integer, List<GPIssue>> commitIssueMap = new HashMap<>()

        commits = new ArrayList<>()
        int issueIndex = 0;
        // for each commit allocate issues
        // number of issues is in 'numberOfIssuesToAssign'
        for (int c = 0; c < 10; c++) {
            List<GPIssue> issueList = new ArrayList<>()
            for (int i = 0; i < numberOfIssuesToAssign.get(c); i++) {
                issueList.add(issues.get(issueIndex))
                issueIndex++
            }
            commitIssueMap.put(c, issueList)
        }

        int tagIndex = 0
        for (int i = 0; i < 10; i++) {
            String hash = DigestUtils.sha1Hex(Integer.toString(i))
            String commitMessage = commitMessage(i, commitIssueMap)
            GitCommit commit = new GitCommit(commitMessage, hash, personIdent, personIdent)
            commits.add(commit)

            if (tagged.contains(i)) {
                createTag(tagIndex, commit)
                tagIndex++
            }
        }
    }

    private String commitMessage(int commitNumber, Map<Integer, List<GPIssue>> commitIssueMap) {
        List<GPIssue> issueList = commitIssueMap.get(commitNumber)
        if (issueList.isEmpty()) {
            return "No issues referenced in commit comment"
        }
        StringBuilder buf = new StringBuilder()
        boolean first = true
        for (GPIssue issue : issueList) {
            if (!first) {
                buf.append("Also ")
            }
            buf.append("Fix #")
            buf.append(issue.number)
            buf.append(" do fixy thing \n")
            first = false
        }
        return buf.toString()
    }


    private void createTag(int index, GitCommit commit) {
        ZonedDateTime zdt = ZonedDateTime.of(2016, 2, 3, 12, 11, 15, 0, ZoneId.of("Z"))
        List<String> tagNames = ImmutableList.of('0.1', '0.2', '1.3.1', '1.3.12', '2.0')
        List<ZonedDateTime> releaseDates = ImmutableList.of(zdt, zdt.plusDays(1), zdt.plusDays(2), zdt.plusDays(3), zdt.plusDays(4))
        Tag tag = new Tag(tagNames.get(index), releaseDates.get(index), releaseDates.get(index), personIdent, "version " + tagNames.get(index), commit, Tag.TagType.ANNOTATED)
        tags.add(tag)
    }

    private DefaultGitPlus gitPlus() {
        gitPlus = Mock(GitPlus)
        gitPlus.local >> gitLocal
        gitPlus.remote >> gitRemote
        gitPlus.wikiLocal >> wikiLocal
        gitPlus.local.tags() >> tags
        gitPlus.local.extractDevelopCommits() >> ImmutableList.copyOf(ImmutableList.copyOf(commits).reverse())
        gitPlus.remote.tagUrl() >> 'https://github.com/davidsowerby/dummy/tree'
        gitPlus.remote >> gitRemote
        gitPlus.local.projectName >> 'Dummy'
        changeLogConfiguration.getOutputFilename() >> new File(temp, 'changelog.md')
        changeLogConfiguration.getTemplateName() >> DefaultChangeLogConfiguration.DEFAULT_TEMPLATE
        changeLogConfiguration.getLabelGroups() >> DefaultChangeLogConfiguration.defaultLabelGroups
    }


    private List<Set<String>> createLabels() {
        List<Set<String>> labels
        labels = new ArrayList<>()
        labels.add(ImmutableSet.of('bug'))
        labels.add(ImmutableSet.of('bug', 'build'))
        labels.add(ImmutableSet.of('task'))
        labels.add(ImmutableSet.of('enhancement'))
        labels.add(ImmutableSet.of('testing'))
        labels.add(ImmutableSet.of('rubbish'))
        labels.add(ImmutableSet.of())// deliberately empty
        labels.add(ImmutableSet.of('task', 'build'))
        labels.add(ImmutableSet.of('quality'))
        labels.add(ImmutableSet.of('documentation'))
        labels.add(ImmutableSet.of('bug'))
        labels.add(ImmutableSet.of('bug'))
        labels.add(ImmutableSet.of('bug', 'build'))
        labels.add(ImmutableSet.of('task'))
        labels.add(ImmutableSet.of('bug'))
        labels.add(ImmutableSet.of('performance', 'enhancement')) // 2 in same group
        labels.add(ImmutableSet.of('enhancement', 'documentation')) // 2 in different groups
        labels.add(ImmutableSet.of('bug'))
        labels.add(ImmutableSet.of('enhancement'))
        labels.add(ImmutableSet.of('bug'))
        return labels
    }

    private void createIssues() {
        labels = createLabels()
        issues = new ArrayList<>()
        for (int i = 0; i < 20; i++) {
            GPIssue issue = new GPIssue(i)
            issue.title("issue " + i)
                    .labels(labels.get(i))
                    .htmlUrl("https:/github.com/davidsowerby/dummy/issues/" + i)
            issues.add(issue)
        }
    }

    private File testResource(String fileName) {
        URL url = this.getClass()
                .getResource(fileName);
        return Paths.get(url.toURI())
                .toFile();
    }
}