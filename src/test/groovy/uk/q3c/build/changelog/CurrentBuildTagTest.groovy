package uk.q3c.build.changelog

import org.eclipse.jgit.lib.PersonIdent
import spock.lang.Specification
import uk.q3c.build.gitplus.local.GitCommit
import uk.q3c.build.gitplus.local.Tag

import java.time.ZonedDateTime

/**
 * Created by David Sowerby on 30 Mar 2016
 */
class CurrentBuildTagTest extends Specification {

    def "construct"() {
        given:
        ZonedDateTime now = ZonedDateTime.now()
        PersonIdent committer = Mock(PersonIdent)
        committer.when >> Date.from(now.toInstant());
        committer.timeZone >> TimeZone.default
        GitCommit commit = new GitCommit("commit message", "xx", committer, committer)
        commit.getCommitter() >> committer
        commit.getCommitDate() >> now

        when:
        CurrentBuildTag tag = new CurrentBuildTag(commit)

        then:
        tag.getReleaseDate().equals(now)
        tag.getCommit().equals(commit)
        tag.getFullMessage().equals('Pseudo tag on latest commit')
        tag.getTaggerIdent().equals(committer)
        tag.getTagType() == Tag.TagType.PSEUDO
    }
}
