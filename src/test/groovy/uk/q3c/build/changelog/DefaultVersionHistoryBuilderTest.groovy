package uk.q3c.build.changelog

import spock.lang.Specification
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.remote.GitRemote

/**
 * Created by David Sowerby on 20 Nov 2016
 */
class DefaultVersionHistoryBuilderTest extends Specification {

    DefaultVersionHistoryBuilder builder
    VersionTagFilter versionTagFilter = Mock(VersionTagFilter)
    ChangeLogConfiguration changeLogConfiguration
    GitPlus gitPlus = Mock(GitPlus)
    GitRemote gitRemote = Mock(GitRemote)
    MockGitLocal gitLocal

    def setup() {
        versionTagFilter.isVersionTag(_) >> true
        changeLogConfiguration = new DefaultChangeLogConfiguration()
        gitLocal = new MockGitLocal()
        gitPlus.local >> gitLocal
        gitPlus.remote >> gitRemote
        builder = new DefaultVersionHistoryBuilder()
    }

    def "maxVersions set, no versions exist, throw exception"() {
        given:
        changeLogConfiguration.maxVersions(1).autoTagLatestCommit(false)

        when:
        builder.build(gitPlus, changeLogConfiguration)

        then:
        thrown(ChangeLogException)
    }


    def "maxVersions set, only one version exists"() {
        given:
        gitLocal.createVersionTag('0.0.1', 0, 'any')
        changeLogConfiguration.maxVersions(2)

        when:
        List<VersionRecord> versions = builder.build(gitPlus, changeLogConfiguration)

        then:
        versions.size() == 1
        VersionRecord version = versions.get(0)
        version.getCommits().size() == gitLocal.commits1.size() // only the very first commit has a tag, so all commits in one version
    }

    def "maxVersions set, 2 versions required, only 2 exist"() {
        given:
        gitLocal.createVersionTag('0.0.2', 0, 'any')
        gitLocal.createVersionTag('0.0.1', 4, 'any')
        changeLogConfiguration.maxVersions(2)

        when:
        List<VersionRecord> versions = builder.build(gitPlus, changeLogConfiguration)

        then:
        versions.size() == 2
        VersionRecord version2 = versions.get(0)
        version2.tagName == '0.0.2'
        version2.getCommits().size() == 4

        VersionRecord version1 = versions.get(1)
        version1.tagName == '0.0.1'
        version1.getCommits().size() == gitLocal.commits1.size() - 4


    }

    def "maxVersions set, 2 versions required, more than 2 exist"() {
        given:
        gitLocal.createVersionTag('0.0.3', 0, 'any')
        gitLocal.createVersionTag('0.0.2', 4, 'any')
        gitLocal.createVersionTag('0.0.1', 5, 'any')
        changeLogConfiguration.maxVersions(2)

        when:
        List<VersionRecord> versions = builder.build(gitPlus, changeLogConfiguration)

        then:
        versions.size() == 2
        VersionRecord version2 = versions.get(0)
        version2.tagName == '0.0.3'
        version2.getCommits().size() == 4

        VersionRecord version1 = versions.get(1)
        version1.tagName == '0.0.2'
        version1.getCommits().size() == 1
    }

    def "default config is to process all commits, pseudo version used for latest commit"() {
        given:
        gitLocal.createVersionTag('0.0.2', 4, 'any')
        gitLocal.createVersionTag('0.0.1', 5, 'any')

        when:
        List<VersionRecord> versions = builder.build(gitPlus, changeLogConfiguration)

        then:
        versions.size() == 3
        VersionRecord version3 = versions.get(0)
        version3.tagName == 'current build'
        version3.getCommits().size() == 4


        VersionRecord version2 = versions.get(1)
        version2.tagName == '0.0.2'
        version2.getCommits().size() == 1

        VersionRecord version1 = versions.get(2)
        version1.tagName == '0.0.1'
        version1.getCommits().size() == gitLocal.commits1.size() - 5
    }

    def "maxVersions with both fromVersion and toVersion set, takes maxVersions back from toVersionId"() {
        given:
        gitLocal.createVersionTag('0.0.4', 0, 'any')
        gitLocal.createVersionTag('0.0.3', 2, 'any')
        gitLocal.createVersionTag('0.0.2', 4, 'any')
        gitLocal.createVersionTag('0.0.1', 5, 'any')
        gitLocal.createVersionTag('0.0.0.1', 7, 'any')
        changeLogConfiguration.maxVersions(2).fromVersionId('0.0.1').toVersionId('0.0.3')

        when:
        List<VersionRecord> versions = builder.build(gitPlus, changeLogConfiguration)

        then:
        versions.size() == 2
        versions.get(0).tagName == '0.0.3'
    }

    def "maxCommits, stops even when version incomplete"() {
        gitLocal.createVersionTag('0.0.4', 0, 'any')
        gitLocal.createVersionTag('0.0.3', 2, 'any')
        gitLocal.createVersionTag('0.0.2', 7, 'any')
        gitLocal.createVersionTag('0.0.1', 8, 'any')
        changeLogConfiguration.maxCommits(5).processAsCommits()

        when:
        List<VersionRecord> versions = builder.build(gitPlus, changeLogConfiguration)

        then:
        !changeLogConfiguration.processingAsVersions
        builder.commitsProcessed == 5
        versions.size() == 2
    }

    def "only fromVersion set, continues to most recent"() {
        gitLocal.createVersionTag('0.0.4', 0, 'any')
        gitLocal.createVersionTag('0.0.3', 2, 'any')
        gitLocal.createVersionTag('0.0.2', 7, 'any')
        gitLocal.createVersionTag('0.0.1', 8, 'any')
        changeLogConfiguration.fromVersionId('0.0.2')

        when:
        List<VersionRecord> versions = builder.build(gitPlus, changeLogConfiguration)

        then:
        versions.size() == 3
        VersionRecord version4 = versions.get(0)
        version4.tagName == '0.0.4'
    }

    def "toVersion does not exist"() {
        gitLocal.createVersionTag('0.0.4', 0, 'any')
        gitLocal.createVersionTag('0.0.3', 2, 'any')
        gitLocal.createVersionTag('0.0.2', 7, 'any')
        gitLocal.createVersionTag('0.0.1', 8, 'any')
        changeLogConfiguration.toVersionId('0.0.2a')

        when:
        List<VersionRecord> versions = builder.build(gitPlus, changeLogConfiguration)

        then:
        thrown ChangeLogException
    }

    def "fromVersion and toVersion both set with maxVersions not effective"() {
        gitLocal.createVersionTag('0.0.4', 0, 'any')
        gitLocal.createVersionTag('0.0.3', 2, 'any')
        gitLocal.createVersionTag('0.0.2', 7, 'any')
        gitLocal.createVersionTag('0.0.1', 8, 'any')
        changeLogConfiguration.fromVersionId('0.0.2').toVersionId('0.0.3')

        when:
        List<VersionRecord> versions = builder.build(gitPlus, changeLogConfiguration)

        then:
        versions.size() == 2
        VersionRecord version3 = versions.get(0)
        version3.tagName == '0.0.3'
        version3.commits.size() == 5

        VersionRecord version2 = versions.get(1)
        version2.tagName == '0.0.2'
        version2.commits.size() == 1
    }

    def "fromCommit into CurrentBuildTag"() {
        given:
        changeLogConfiguration.fromCommitId(gitLocal.commits1.get(4).hash).processAsCommits()

        when:
        List<VersionRecord> versions = builder.build(gitPlus, changeLogConfiguration)

        then:
        versions.size() == 1  // Just using currentBuildTag
        versions.get(0).commits.last() == gitLocal.commits1.get(4)
        versions.get(0).commits.first() == gitLocal.commits1.get(0)
    }

    def "fromCommit and toCommit set, both inclusive"() {
        given:
        changeLogConfiguration.fromCommitId(gitLocal.commits1.get(4).hash).toCommitId(gitLocal.commits1.get(3).hash).processAsCommits()

        when:
        List<VersionRecord> versions = builder.build(gitPlus, changeLogConfiguration)

        then:
        versions.size() == 1  // Just using currentBuildTag
        versions.get(0).commits.last() == gitLocal.commits1.get(4)
        versions.get(0).commits.first() == gitLocal.commits1.get(3)
    }

    def "fromCommitId specified but does not exist, just continue to the end"() {
        gitLocal.createVersionTag('0.0.4', 0, 'any')
        gitLocal.createVersionTag('0.0.3', 2, 'any')
        gitLocal.createVersionTag('0.0.2', 7, 'any')
        gitLocal.createVersionTag('0.0.1', 8, 'any')
        changeLogConfiguration.fromCommitId('rubbish')

        when:
        List<VersionRecord> versions = builder.build(gitPlus, changeLogConfiguration)

        then:
        versions.size() == 4
        builder.commitsProcessed == 11
    }

    def "toCommitId specified but does not exist, throws exception"() {
        gitLocal.createVersionTag('0.0.4', 0, 'any')
        gitLocal.createVersionTag('0.0.3', 2, 'any')
        gitLocal.createVersionTag('0.0.2', 7, 'any')
        gitLocal.createVersionTag('0.0.1', 8, 'any')
        changeLogConfiguration.toCommitId('rubbish').processAsCommits()

        when:
        List<VersionRecord> versions = builder.build(gitPlus, changeLogConfiguration)

        then:
        thrown ChangeLogException
    }

    def "No commits available, just return empty result"() {
        given:
        gitLocal.commits1.clear()

        when:
        List<VersionRecord> versions = builder.build(gitPlus, changeLogConfiguration)

        then:
        versions.size() == 0
    }
}
