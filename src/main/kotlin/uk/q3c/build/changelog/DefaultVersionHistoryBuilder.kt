package uk.q3c.build.changelog

import com.google.common.collect.UnmodifiableListIterator
import com.google.inject.Inject
import org.slf4j.LoggerFactory
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitCommit
import uk.q3c.build.gitplus.local.Tag

/**
 * Created by David Sowerby on 18 Nov 2016
 */
class DefaultVersionHistoryBuilder @Inject constructor(val fileLocator: FileLocator) : VersionHistoryBuilder {
    private val log = LoggerFactory.getLogger(this.javaClass.name)
    // maps commit hash to tag
    val tagMap: MutableMap<String, Tag> = mutableMapOf()
    lateinit var gitPlus: GitPlus
    lateinit var changeLogConfiguration: ChangeLogConfiguration
    val versionRecords: MutableList<VersionRecord> = mutableListOf()
    var commitsProcessed: Int = 0
        set
    lateinit var lastProcessedCommit: GitCommit

    private lateinit var commitIterator: UnmodifiableListIterator<GitCommit>

    override fun build(gitPlus: GitPlus, changeLogConfiguration: ChangeLogConfiguration): List<VersionRecord> {
        val commits = gitPlus.local.extractCommitsFor(changeLogConfiguration.branch)
        this.changeLogConfiguration = changeLogConfiguration
        this.gitPlus = gitPlus
        versionRecords.clear()
        buildTagMap(gitPlus)

        commitIterator = commits.listIterator()
        if (!commitIterator.hasNext()) {
            log.warn("There are no commits to build a change log from")
            return versionRecords
        }

        verifyCurrentBuildTag(commitIterator.next(), changeLogConfiguration)
        commitIterator = commits.listIterator() // reset the iterator

        if (changeLogConfiguration.processingAsVersions) {
            return processAsVersions()
        } else {
            return processAsCommits()
        }

    }


    /**
     * If [ChangeLogConfiguration.autoTagLatestCommit] is true (the default) then the latest commit is tagged with
     * a [CurrentBuildTag], but only if it does not have a version tag already
     */
    private fun verifyCurrentBuildTag(latestCommit: GitCommit, changeLogConfiguration: ChangeLogConfiguration) {
        if (changeLogConfiguration.autoTagLatestCommit) {
            if (!tagMap.containsKey(latestCommit.hash)) {
                tagMap.put(latestCommit.hash, CurrentBuildTag(latestCommit, changeLogConfiguration.currentBuildTagName))
            }
        }
    }


    /**
     * If [ChangeLogConfiguration.toVersionId] is specified, then scroll to that version, otherwise scroll to the first
     * available version
     *
     * @throws ChangeLogException if a specified [ChangeLogConfiguration.toVersionId] is not found, or no versions are
     * found
     */
    private fun processAsVersions(): List<VersionRecord> {
        log.debug("Looking for the most recent required version to start the process")
        var startVersionFound = false

        while (commitIterator.hasNext() && (!startVersionFound)) {
            val gCommit: GitCommit = commitIterator.next()

            if (isVersion(gCommit)) {
                val tagName = tagMap.get(gCommit.hash)?.tagName
                log.debug("toVersionId is '{}'", tagName)
                if ((changeLogConfiguration.toVersionId == notSpecified) || (changeLogConfiguration.toVersionId == tagName)) {
                    startVersionFound = true
                    log.debug("most recent required version is: '{}'", tagName)
                    val versionRecord = addNewVersion(gCommit)
                    collateVersions(versionRecord, commitIterator)
                }
            }
        }

        if (!startVersionFound) {
            throw ChangeLogException("ChangeLog has been set to process versions, but no versions exist using the configuration given")
        }
        return versionRecords
    }


    /**
     * If [gCommit] does not have a tag, it is assumed to be the latest, untagged commit, and a [CurrentBuildTag] is used
     * to 'pseudo tag' the latest commit - this allows all commits to be included, and is useful for [selectAll]
     * and [selectLastNCommits].
     */
    private fun addNewVersion(gCommit: GitCommit): VersionRecord {
        val tag: Tag = tagMap.get(gCommit.hash)!!
        val versionRecord = VersionRecord(tag, changeLogConfiguration, gitPlus, fileLocator)
        versionRecord.addCommit(gCommit)
        commitsProcessed++
        lastProcessedCommit = gCommit
        versionRecords.add(versionRecord)
        log.debug("New version added: '{}'", tag.tagName)
        return versionRecord
    }


    /**
     * Must enter this method with [versionRecord] already populated with its first commit (the one with the version tag)
     * and [commitIterator] pointing to that first commit.  This method then cycles through commits, creating new version records
     * as needed, and adding commits to those records.
     *
     * The cycle comes to an end when either the last commit has been processed or the [isEarliestVersion] condition is met.
     */
    private fun collateVersions(versionRecord: VersionRecord, commitIterator: UnmodifiableListIterator<GitCommit>) {
        var vRecord = versionRecord
        var versionsCompleted = false
        while (commitIterator.hasNext() && (!versionsCompleted)) {
            val aCommit = commitIterator.next()
            // if is is another version start a new record
            if (isVersion(aCommit)) {
                vRecord = addNewVersion(aCommit)
                // else add commit to existing record
            } else {
                vRecord.addCommit(aCommit)
                commitsProcessed++
                lastProcessedCommit = aCommit
            }
            if (isEarliestVersion(aCommit)) {
                versionsCompleted = true
            }
        }
        // above will breakout as soon as we have the total number of versions we required, but the last one
        // may not have all its commits, so we need to scan for more commits
        while (commitIterator.hasNext()) {
            val aCommit = commitIterator.next()
            if (isVersion(aCommit)) {
                break
            }
            vRecord.addCommit(aCommit)
            commitsProcessed++
            lastProcessedCommit == aCommit
        }
    }

    private fun collateCommits(versionRecord: VersionRecord, commitIterator: UnmodifiableListIterator<GitCommit>) {
        var vRecord = versionRecord
        while (commitIterator.hasNext() && (!commitsCompleted())) {
            val aCommit = commitIterator.next()
            // if is is another version start a new record
            if (isVersion(aCommit)) {
                vRecord = addNewVersion(aCommit)
                // else add commit to existing record
            } else {
                vRecord.addCommit(aCommit)
                commitsProcessed++
                lastProcessedCommit = aCommit
            }
        }
    }

    private fun commitsCompleted(): Boolean {
        if (changeLogConfiguration.fromCommitId == notSpecified) {
            return commitsProcessed >= changeLogConfiguration.maxCommits
        } else {
            return lastProcessedCommit.hash == changeLogConfiguration.fromCommitId
        }
    }

    /**
     * Because the commits are processed in reverse chronological order (that is, most recent first), this method tests
     * whether we have reached the last version to be processed - this can be set explicitly by
     * [ChangeLogConfiguration.fromVersionId]
     */
    private fun isEarliestVersion(gCommit: GitCommit): Boolean {
        log.debug("Looking for the earliest required version")
        val isAVersion = isVersion(gCommit)
        if (!isAVersion) {
            log.debug("commit '{}' is not version tagged", gCommit.hash)
            return false
        }
        if ((changeLogConfiguration.maxVersions > 0) && (versionRecords.size >= changeLogConfiguration.maxVersions)) {
            log.debug("Maximum required number of versions ({}) reached, earliest version to process is: {}", changeLogConfiguration.maxVersions, versionRecords.last().tagName)
            return true
        }
        log.debug("fromVersionId is:  '{}'", changeLogConfiguration.fromVersionId)
        val tag = tagMap.get(gCommit.hash)!!
        log.debug("tag is: '{}'", tag.tagName)
        val matchFound = tag.tagName == changeLogConfiguration.fromVersionId
        log.debug("version match found: '{}'", matchFound)
        return matchFound
    }

    private fun isVersion(gCommit: GitCommit): Boolean {
        val versionTagFilter = changeLogConfiguration.versionTagFilter
        val tag = tagMap.get(gCommit.hash)
        if (tag != null) {
            if (versionTagFilter.isVersionTag(tag)) {
                return true
            }
        }
        return false
    }


    private fun processAsCommits(): List<VersionRecord> {
        val gCommit: GitCommit = scrollToLatestCommit(commitIterator)
        //pseudo tag to enable creation of version
        tagMap.put(gCommit.hash, CommitRangeTag(gCommit))
        val versionRecord = addNewVersion(gCommit)
        collateCommits(versionRecord, commitIterator)
        return versionRecords
    }

    private fun scrollToLatestCommit(commitIterator: UnmodifiableListIterator<GitCommit>): GitCommit {
        var gCommit: GitCommit = commitIterator.next()
        if (changeLogConfiguration.toCommitId == notSpecified) {
            return gCommit
        }
        while (commitIterator.hasNext()) {
            gCommit = commitIterator.next()
            if (gCommit.hash == changeLogConfiguration.toCommitId) {
                return gCommit
            }
        }
        throw ChangeLogException("ChangeLogConfiguration.toCommitId is set to '{}', but no such commit exists")
    }


    private fun buildTagMap(gitPlus: GitPlus) {
        val tags = gitPlus.local.tags()
        tagMap.clear()
        for (tag in tags) {
            tagMap.put(tag.commit.hash, tag)
        }
    }


}

