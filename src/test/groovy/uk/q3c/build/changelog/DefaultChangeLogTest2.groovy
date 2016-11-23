package uk.q3c.build.changelog

import org.apache.commons.codec.digest.DigestUtils
import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitLocal
import uk.q3c.build.gitplus.remote.GitRemote

/**
 * Tests for correct extraction of Git commit comments, and any issue references within them
 *
 * Created by David Sowerby on 07 Mar 2016
 */
class DefaultChangeLogTest2 extends Specification {

    ChangeLog changeLog
    GitPlus gitPlus = Mock(GitPlus)
    ChangeLogConfiguration configuration
    GitLocal gitLocal
    GitRemote gitRemote

    def setup() {
        gitLocal = new MockGitLocal()
        gitRemote = new MockGitRemote()
        gitPlus.local >> gitLocal
        gitPlus.remote = gitRemote
        configuration = new DefaultChangeLogConfiguration()
        changeLog = new DefaultChangeLog(gitPlus, configuration)
    }


    private String hash(int key) {
        return DigestUtils.sha1Hex(Integer.toString(key))
    }

}