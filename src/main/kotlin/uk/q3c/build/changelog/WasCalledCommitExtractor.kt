package uk.q3c.build.changelog

import org.apache.commons.lang3.text.StrMatcher
import org.apache.commons.lang3.text.StrTokenizer
import org.eclipse.jgit.revwalk.RevCommit
import org.slf4j.LoggerFactory
import uk.q3c.build.gitplus.remote.GPIssue
import uk.q3c.build.gitplus.remote.GitRemote
import java.util.*

/**
 * Created by David Sowerby on 17 Oct 2016
 */
class WasCalledCommitExtractor(val gitRemote: GitRemote) {
    private val log = LoggerFactory.getLogger(this.javaClass.name)

    val fixReferences: MutableList<GPIssue>

    init {
        fixReferences = ArrayList<GPIssue>()
    }

    private fun extractIssueReferences(revCommit: RevCommit): String {
        var fMessage = correctCommonTypos(revCommit.fullMessage)
        val tokenizer = StrTokenizer(fMessage, StrMatcher.charSetMatcher(TOKEN_SPLIT_CHARS))
        val tokens = tokenizer.tokenList
        val expandedTokens = ArrayList<String>()
        var previousToken: String
        var currentToken: String = ""
        for (token in tokens) {
            previousToken = currentToken
            currentToken = token
            val expandedReference = expandIssueReferences(previousToken, currentToken, gitRemote)
            expandedTokens.add(expandedReference)
        }
        val tokensIterator = tokens.iterator()
        val expandedTokensIterator = expandedTokens.iterator()

        while (tokensIterator.hasNext()) {
            val token = tokensIterator.next()
            val expandedToken = expandedTokensIterator.next()
            if (token != expandedToken) {
                fMessage = fMessage.replaceFirst(token.toRegex(), expandedToken)
            }
        }
        return fMessage
    }

    private fun expandIssueReferences(previousToken: String, currentToken: String, gitRemote: GitRemote): String {
        if (!currentToken.contains("#") || currentToken.length < 2) {
            return currentToken
        }

        val s = currentToken.split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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

        return captureFixesAndExpandReference(gitRemote, fullRepoName, issueNumber, previousToken, currentToken)

    }

    private fun captureFixesAndExpandReference(gitRemote: GitRemote, fullRepoName: String, issueNumber: Int, previousToken: String, currentToken: String): String {
        val gpIssue: GPIssue
        try {
            if (fullRepoName.isEmpty()) {
                gpIssue = gitRemote.getIssue(issueNumber)
            } else {
                val splitRepoName = fullRepoName.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                gpIssue = gitRemote.getIssue(splitRepoName[0], splitRepoName[1], issueNumber)
            }

            if (gitRemote.isIssueFixWord(previousToken)) {
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

    /**
     * Puts in missing space before the '#' where previous word is a key word
     */


    private fun correctCommonTypos(original: String): String {
        return original.replace("Fix#", "Fix #").replace("fix#", "fix #").replace("Fixes#", "Fixes #").replace("fixes#", "fixes #").replace("See#", "See #").replace("see#", "see #").replace("Close#", "Close #").replace("close#", "close #").replace("Closes#", "Closes #").replace("closes#", "closes #").replace("Resolve#", "Resolve #").replace("resolve#", "resolve #").replace("Resolves#", "Resolves #").replace("resolves#", "resolves #")

    }

    companion object {
        private val TOKEN_SPLIT_CHARS = " \t\n\r,.:;*?`![]'"
    }
}