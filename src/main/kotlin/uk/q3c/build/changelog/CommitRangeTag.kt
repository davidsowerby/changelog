package uk.q3c.build.changelog

import uk.q3c.build.gitplus.local.GitCommit
import uk.q3c.build.gitplus.local.Tag

/**
 * A pseudo Tag instance used to identify the most recent build, without actually tagging Git directly
 *
 *
 * Created by David Sowerby on 30 Mar 2016
 */
class CommitRangeTag(commit: GitCommit) : Tag("Commit range", commit.commitDate, commit.commitDate, commit.committer, "Pseudo tag on range of commits", commit, TagType.PSEUDO) {

    /**
     * When used with the tag url, forces the Git tree to look at the develop branch, rather than the default version number from the tag name
     */
    override val urlSegment: String = "develop"
}
