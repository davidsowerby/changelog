package uk.q3c.build.changelog

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import org.apache.commons.codec.digest.DigestUtils
import org.eclipse.jgit.lib.PersonIdent
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitCommit
import uk.q3c.build.gitplus.local.Tag
import uk.q3c.build.gitplus.remote.GPIssue
import uk.q3c.build.gitplus.remote.GitRemote

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

import static org.assertj.core.api.Assertions.*

/**
 * Limited unit testing - tried to Mock RevCommit with Spock and Mockito, both failed, probably because of the
 * static call to RawParseUtils.commitMessage in getFullMessage().  Also, getFullMessage cannot be overridden,
 * as it is final. Will have to rely on higher level testing
 *
 * Created by David Sowerby on 09 Mar 2016
 */
class VersionRecordTest extends Specification {
    VersionRecord record
    GitCommit rc1
    PersonIdent personIdent = Mock(PersonIdent)
    GitRemote gitRemote = Mock(GitRemote)
    @Shared
    MockGitRemote mockRemote
    GitPlus gitPlus = Mock(GitPlus)
    MockGitLocal gitLocal = new MockGitLocal()
    DefaultChangeLogConfiguration changeLogConfiguration
    ZonedDateTime commitDate = ZonedDateTime.of(LocalDateTime.of(2010, 11, 11, 12, 2), ZoneId.of("Z"))
    ZonedDateTime releaseDate = ZonedDateTime.of(LocalDateTime.of(2015, 1, 11, 12, 12), ZoneId.of("Z"))
    FileLocator fileLocator = new DefaultFileLocator()
    IssueRecords issueRecords = Mock(IssueRecords)

    def setup() {
        mockRemote = new MockGitRemote()
        mockRemote.createIssues(0)
        changeLogConfiguration = new DefaultChangeLogConfiguration()
        gitPlus.remote >> gitRemote
        rc1 = gitLocal.commits1.get(0)
        personIdent.timeZone >> TimeZone.default
        personIdent.name >> "me"
        personIdent.when >> new Date()
    }


    def "construct builds labelLookup"() {
        given:
        final String tagName = "0.1"
        Tag tag = newTag(tagName)

        when:
        record = new VersionRecord(tag, changeLogConfiguration, gitPlus, fileLocator)

        then:
        Map<String, String> lookup = record.getLabelLookup()
        lookup.get('bug') == ('Fixes')
        lookup.get('enhancement') == ('Enhancements')
        lookup.get('performance') == ('Enhancements')
        lookup.get('testing') == ('Quality')
        lookup.get('task') == ('Tasks')

    }

    def "construct with all parameters and get"() {
        given:
        final String tagName = "0.1"
        Tag tag = newTag(tagName)

        when:
        record = new VersionRecord(tag, changeLogConfiguration, gitPlus, fileLocator)
        record.addCommit(rc1)

        then:
        record.getCommitDate() == (commitDate)
        record.getReleaseDate() == (releaseDate)
        record.getTagName() == (tagName)
        record.getTagCommit() == (rc1)
        record.getPersonIdent() == personIdent
        record.getReleaseDateAsDate().toInstant() == (releaseDate.toInstant())
        record.commitDateAsDate.toInstant() == (commitDate.toInstant())
        record.getCommits().size() == 1
        record.getCommits().contains(rc1)
    }


    def "parse"() {
        given:
        final String tagName = "0.1"
        Tag tag = newTag(tagName)
        GPIssue issue1 = newIssue(1, 'Making unnecessary calls', 'bug')
        record = new VersionRecord(tag, changeLogConfiguration, gitPlus, fileLocator)
        record.addCommit(rc1)
        gitRemote.isIssueFixWord('Fix') >> true

        when:
        record.parse(issueRecords)
        Map<String, Set<GPIssue>> fixes = record.getFixesByGroup()

        then:
        1 * issueRecords.getIssue(gitPlus, 1) >> issue1
        fixes.size() == 1
    }

    def "parse groups issues in the group order of configuration.getLabelGroups() and ignores duplicate issues in group"() {
        given:

        Tag tag = newTag("0.1")

        GPIssue issue1 = newIssue(1, 'Making unnecessary calls', 'documentation')
        GPIssue issue2 = newIssue(2, 'Making unnecessary calls', 'task')
        GPIssue issue3 = newIssue(3, 'Making unnecessary calls', 'quality')
        GPIssue issue4 = newIssue(4, 'Making unnecessary calls', 'quality').pullRequest(true)
        GPIssue issue5 = newIssue(5, 'Making unnecessary calls', 'bug')

        record = new VersionRecord(tag, changeLogConfiguration, gitPlus, fileLocator)
        addCommits(record, 5)
        gitRemote.isIssueFixWord('Fix') >> true

        when:
        record.parse(issueRecords)
        Map<String, Set<GPIssue>> fixes = record.getFixesByGroup()
        Set<GPIssue> pullRequests = record.getPullRequests()

        then:
        1 * issueRecords.getIssue(gitPlus, 1) >> issue1
        1 * issueRecords.getIssue(gitPlus, 2) >> issue2
        1 * issueRecords.getIssue(gitPlus, 3) >> issue3
        1 * issueRecords.getIssue(gitPlus, 4) >> issue4
        2 * issueRecords.getIssue(gitPlus, 5) >> issue5 // deliberately added twice to ensure output not duplicated
        fixes.size() == 5
        assertThat(fixes.keySet()).containsExactly(DefaultChangeLogConfiguration.DEFAULT_PULL_REQUESTS_TITLE, 'Fixes', 'Quality', 'Tasks', 'Documentation')
        assertThat(pullRequests).containsOnly(issue4)
    }

    def "hasCommits"() {
        given:
        final String tagName = "0.1"
        Tag tag = newTag(tagName)

        when:
        record = new VersionRecord(tag, changeLogConfiguration, gitPlus, fileLocator)

        then:
        !record.hasCommits()

        when:
        record.addCommit(rc1)

        then:
        record.hasCommits()
    }

    def "commit ignored when it contains exclusion tag"() {
        given:
        final String tagName = "0.1"
        Tag tag = newTag(tagName)
        changeLogConfiguration.exclusionTags(ImmutableSet.of("javadoc"))
        record = new VersionRecord(tag, changeLogConfiguration, gitPlus, fileLocator)
        addCommitsOneWithExclusionTag(record)

        when:
        record.parse(issueRecords)

        then:
        record.getCommits().size() == 1
        record.getCommits().get(0).getFullMessage().equals(gitLocal.commits1.get(0).fullMessage)
        record.getExcludedCommits().size() == 1


    }

    @Unroll
    def "references"() {
        given:

        List<Integer> results1 = ImmutableList.of(2, 3, 4)
        GitPlus gitPlus2 = Mock(GitPlus)

        gitPlus2.remote >> mockRemote
        gitPlus2.local >> gitLocal
        Tag tag = newTag("0.1")
        changeLogConfiguration.correctTypos(true)
        record = new VersionRecord(tag, changeLogConfiguration, gitPlus2, fileLocator)
        record.addCommit(gitLocal.commits1.get(commitNo))
        issueRecords = new DefaultIssueRecords()

        when:
        List<GPIssue> fixReferences = record.parse(issueRecords)

        then:
        fixReferences.size() == issueNo.size()
        fixReferences.containsAll(issueNo)

        where:
        commitNo | issueNo
        0        | mockRemote.issueSet(1)  // single line, single fix
        1        | mockRemote.issueSet(2)  // 2 additional fix refs in body
        2        | mockRemote.issueSet(3)  // typo corrected
        3        | mockRemote.issueSet(0)  // issue number not found by API
        4        | mockRemote.issueSet(0)  // issue number invalid format
        5        | mockRemote.issueSet(4)  // fix in other repo
        6        | mockRemote.issueSet(0)  // other reference incorrectly structured
        7        | mockRemote.issueSet(0)  // hash with non-number
        8        | mockRemote.issueSet(0)  // trailing hash
        9        | mockRemote.issueSet(0)  // commit excluded
        10       | mockRemote.issueSet(0)  // double hash

    }


    def addCommitsOneWithExclusionTag(VersionRecord versionRecord) {
        GitCommit commit1 = gitLocal.commits1.get(0)
        GitCommit commit2 = gitLocal.commits1.get(9)
        versionRecord.addCommit(commit1)
        versionRecord.addCommit(commit2)
    }

    Tag newTag(String tagName) {
        return new Tag(tagName, releaseDate, commitDate, personIdent, "version " + tagName, rc1, Tag.TagType.ANNOTATED)
    }

    def addCommits(VersionRecord record, int i) {
        for (int j = 1; j <= i; j++) {
            String msg = 'Fix #' + j + ' commit summary'
            if (j == 4) {
                msg = msg + '\n\n Fix #5 commit detail'
            }
            String hash = DigestUtils.sha1Hex(Integer.toString(i))
            GitCommit commit = new GitCommit(msg, hash, personIdent, personIdent)
            record.addCommit(commit)
        }
    }


    private GPIssue newIssue(int number, String title, String label) {
        return new GPIssue(number).title(title).htmlUrl('https:/github.com/davidsowerby/dummy/issues/1').labels(ImmutableSet.of(label))
    }
}

