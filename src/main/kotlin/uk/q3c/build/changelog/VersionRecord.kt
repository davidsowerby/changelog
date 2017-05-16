package uk.q3c.build.changelog

import org.apache.commons.lang3.text.StrMatcher
import org.apache.commons.lang3.text.StrTokenizer
import org.eclipse.jgit.lib.PersonIdent
import org.slf4j.LoggerFactory
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.GitCommit
import uk.q3c.build.gitplus.local.Tag
import uk.q3c.build.gitplus.remote.GPIssue
import uk.q3c.build.gitplus.remote.GitRemote
import java.time.ZonedDateTime
import java.util.*

/**
 * Created by David Sowerby on 07 Mar 2016
 */
class VersionRecord(val tag: Tag, val changeLogConfiguration: ChangeLogConfiguration, val gitPlus: GitPlus) {
    private val log = LoggerFactory.getLogger(this.javaClass.name)
    val commits: MutableList<GitCommit>
    val excludedCommits: MutableList<GitCommit>
    val fixesByGroup: MutableMap<String, MutableSet<GPIssue>>
    val labelLookup: Map<String, String>
    val pullRequests: MutableSet<GPIssue>
    val expandedCommits: MutableList<ExpandedGitCommit> = mutableListOf()


    init {
        labelLookup = createLabelLookup(changeLogConfiguration.labelGroups)
        pullRequests = TreeSet<GPIssue>()
        commits = ArrayList<GitCommit>()
        excludedCommits = ArrayList<GitCommit>()
        fixesByGroup = LinkedHashMap<String, MutableSet<GPIssue>>()
        for (group in changeLogConfiguration.labelGroups.keys) {
            fixesByGroup.put(group, TreeSet())
        }

    }

    val tagName: String
        get() = tag.tagName


    val tagRef: String
        get() = if (tag is CurrentBuildTag) {
            changeLogConfiguration.branch.name
        } else {
            tag.tagName
        }



    val releaseDate: ZonedDateTime
        get() {
            return tag.releaseDate
        }


    fun getPersonIdent(): PersonIdent {
        return tag.taggerIdent
    }

    /**
     * Provides a 'reverse' lookup to map an issue back to the group to which it belongs
     */
    private fun createLabelLookup(labelGroups: Map<String, Set<String>>): MutableMap<String, String> {
        log.debug("creating label lookup")
        val labelLookup: MutableMap<String, String> = mutableMapOf()
        for ((group, labelSet) in labelGroups) {
            for (label in labelSet) {
                labelLookup.put(label, group)
            }
        }
        return labelLookup
    }


    /**
     * [.getReleaseDate] converted to [Date] (primarily for Velocity)

     * @return [.getReleaseDate] converted to [Date] (primarily for Velocity)
     */
    val releaseDateAsDate: Date
        get() = Date.from(releaseDate.toInstant())

    /**
     * [commitDate] converted to [Date] (primarily for Velocity)

     * @return [commitDate] converted to [Date] (primarily for Velocity)
     */
    val commitDateAsDate: Date
        get() = Date.from(commitDate.toInstant())

    val commitDate: ZonedDateTime
        get() = tag.commitDate

    val tagCommit: GitCommit
        get() = tag.commit

    fun addCommit(commit: GitCommit) {
        if (excludedFromChangeLog(commit, changeLogConfiguration)) {
            excludedCommits.add(commit)
        } else {
            commits.add(commit)
        }
    }

    /**
     * Expands commit and issue information to make it ready for output to Velocity

     * @return the issues referenced by all the commit comments
     */
    fun parse(): List<GPIssue> {
        val fixReferences: MutableList<GPIssue> = mutableListOf()
        expandedCommits.clear()
        for (c in commits) {
            if (!excludedFromChangeLog(c, changeLogConfiguration)) {
                val expandedCommitMessage = extractIssueReferences(c, fixReferences)
                val expandedCommit = ExpandedGitCommit(c, expandedCommitMessage, extractShortMessage(expandedCommitMessage))
                expandedCommits.add(expandedCommit)
                for (issue in fixReferences) {
                    if (issue.isPullRequest) {
                        pullRequests.add(issue)
                    } else {
                        mapIssueToGroups(issue)
                    }
                }
            }
        }
        mergePullRequests()
        removeEmptyGroups()
        return fixReferences
    }

    private fun extractShortMessage(fullMessage: String): String {
        return fullMessage.split("\n").get(0)
    }

    /**
     * Looks for any occurrence of an exclusion tag in a commit comment, and returns true if it finds one, otherwise returns false

     * @param changeLogConfiguration the configuration for the change log, which defines the exclusion tags
     * *
     * @return true if an exclusion tag is found
     */
    fun excludedFromChangeLog(commit: GitCommit, changeLogConfiguration: ChangeLogConfiguration): Boolean {
        for (exclusionTag in changeLogConfiguration.exclusionTags) {
            if (commit.fullMessage.contains(exclusionTag)) {
                return true
            }
        }
        return false
    }

    fun extractIssueReferences(commit: GitCommit, fixReferences: MutableList<GPIssue>): String {
        var fullMessage = correctCommonTypos(commit.fullMessage)

        val tokenizer = StrTokenizer(fullMessage, StrMatcher.charSetMatcher(TOKEN_SPLIT_CHARS))
        val tokens = tokenizer.tokenList
        val expandedTokens = ArrayList<String>()
        var previousToken: String
        var currentToken: String = ""
        for (token in tokens) {
            previousToken = currentToken
            currentToken = token
            val expandedReference = expandIssueReferences(previousToken, currentToken, fixReferences)
            expandedTokens.add(expandedReference)
        }
        val tokensIterator = tokens.iterator()
        val expandedTokensIterator = expandedTokens.iterator()

        while (tokensIterator.hasNext()) {
            val token = tokensIterator.next()
            val expandedToken = expandedTokensIterator.next()
            if (token != expandedToken) {
                fullMessage = fullMessage.replaceFirst(token.toRegex(), expandedToken)
            }
        }
        return fullMessage
    }

    private fun expandIssueReferences(previousToken: String, currentToken: String, fixReferences: MutableList<GPIssue>): String {
        if (!currentToken.contains("#") || currentToken.length < 2) {
            return currentToken
        }

        val s = currentToken.split("#")
        if (s.size != 2) {
            return currentToken
        }
        val fullRepoName = s[0]
        val issueNumberStr = s[1]

        val issueNumber: Int
        try {
            issueNumber = Integer.parseInt(issueNumberStr)
        } catch (nfe: NumberFormatException) {
            log.warn("{} is not a valid issue number", issueNumberStr)
            return currentToken
        }

        return captureFixesAndExpandReference(fullRepoName, issueNumber, previousToken, currentToken, fixReferences)

    }

    /**
     * A reference may be to an external project (that is, not the current project), so may be in the form of:
     *
     * > Fixes davidsowerby/krail#45
     *
     * This method may need to move to [GitRemote] - other providers may use a different syntax to GitHub
     */
    private fun captureFixesAndExpandReference(fullRepoName: String, issueNumber: Int, previousToken: String, currentToken: String, fixReferences: MutableList<GPIssue>): String {
        val gpIssue: GPIssue
        try {
            if (fullRepoName.isEmpty()) {
                gpIssue = gitPlus.remote.getIssue(issueNumber)
            } else {
                val splitRepoName = fullRepoName.split("/".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
                gpIssue = gitPlus.remote.getIssue(splitRepoName[0], splitRepoName[1], issueNumber)
            }

            if (gitPlus.remote.isIssueFixWord(previousToken)) {
                fixReferences.add(gpIssue)
            }
            return expandedIssue(gpIssue)
        } catch (e: Exception) {
            log.warn("Issue {} not found in repo {}", issueNumber, fullRepoName, e)
            return currentToken
        }

    }

    private fun expandedIssue(gpIssue: GPIssue): String {
        return "[" + gpIssue.number + "](" + gpIssue.htmlUrl + ")"
    }

    //TODO this depends on fix references, should be referenced back to GitRemote
    private fun correctCommonTypos(originalFullMessage: String): String {
        if (changeLogConfiguration.correctTypos) {
            return originalFullMessage.replace("Fix#", "Fix #")
                    .replace("fix#", "fix #")
                    .replace("Fixes#", "Fixes #")
                    .replace("fixes#", "fixes #")
                    .replace("See#", "See #")
                    .replace("see#", "see #")

                    .replace("Close#", "Close #")
                    .replace("close#", "close #")
                    .replace("Closes#", "Closes #")
                    .replace("closes#", "closes #")
                    .replace("Resolve#", "Resolve #")
                    .replace("resolve#", "resolve #")
                    .replace("Resolves#", "Resolves #")
                    .replace("resolves#", "resolves #")
        } else {
            return originalFullMessage
        }

    }


    /**
     * Using the labels on an issue, attach the issue to any group for which it has a label
     */
    private fun mapIssueToGroups(gpIssue: GPIssue) {
        for (label in gpIssue.labels) {
            val group = labelLookup[label]
            //if label in a group, add it, otherwise ignore
            if (group != null) {
                if (fixesByGroup[group] != null) {
                    val issues: MutableSet<GPIssue> = fixesByGroup[group] as MutableSet<GPIssue>
                    issues.add(gpIssue)
                }
            }
        }
    }

    /**
     * If pull requests are required in the output merge them in to the label groups
     */
    private fun mergePullRequests() {
        if (fixesByGroup.containsKey(changeLogConfiguration.pullRequestTitle)) {
            fixesByGroup.put(changeLogConfiguration.pullRequestTitle, pullRequests)
        }
    }

    /**
     * clear out any empty groups, so we don't just get headings with no entries
     */
    private fun removeEmptyGroups() {
        val toRemove = ArrayList<String>()
        for ((group, issues) in fixesByGroup) {
            if (issues.isEmpty()) {
                toRemove.add(group)
            }
        }
        for (unwanted in toRemove) {
            fixesByGroup.remove(unwanted)
        }

    }


    fun hasCommits(): Boolean {
        return !commits.isEmpty()
    }

    companion object {
        private val TOKEN_SPLIT_CHARS = " \t\n\r,.:;*?`![]'"
    }

}
