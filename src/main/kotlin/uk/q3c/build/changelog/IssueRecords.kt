package uk.q3c.build.changelog

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.gitplus.IssueDescriptor
import uk.q3c.build.gitplus.gitplus.RepoDescriptor
import uk.q3c.build.gitplus.remote.GPIssue
import uk.q3c.build.gitplus.remote.GitRemoteException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 *
 * Provides a local 'cache' of issues to reduce the need for accessing the remote service provider API
 *
 * Created by David Sowerby on 20 Sep 2017
 */
interface IssueRecords {
    /**
     * Retrieve issue for the current repoUser/repoName
     *
     * @throws GitRemoteException if the issue is not stored locally and cannot be accessed remotely
     */
    fun getIssue(gitPlus: GitPlus, number: Int): GPIssue

    /**
     * Retrieve issue for the current remote provider, but with specified repoUser/repoName
     *
     * @throws GitRemoteException if the issue is not stored locally and cannot be accessed remotely
     */
    fun getIssue(gitPlus: GitPlus, repoUser: String, repoName: String, number: Int): GPIssue

    /**
     * Retrieve issue for a fully qualified url - see https://github.com/davidsowerby/changelog/issues/25
     *
     * @throws GitRemoteException if the issue is not stored locally and cannot be accessed remotely
     */
    fun getIssue(gitPlus: GitPlus, issueUrl: String): GPIssue

    fun load(file: File)
    fun save(file: File)
    fun isCached(issueUrl: String): Boolean
}


class DefaultIssueRecords : IssueRecords {
    private val log = LoggerFactory.getLogger(this.javaClass.name)
    private var issueMap: MutableMap<String, GPIssue> = mutableMapOf()

    override fun getIssue(gitPlus: GitPlus, number: Int): GPIssue {
        return getIssue(gitPlus, gitPlus.remote.repoUser, gitPlus.remote.repoName, number)
    }


    override fun getIssue(gitPlus: GitPlus, repoUser: String, repoName: String, number: Int): GPIssue {
        val repoDescriptor = RepoDescriptor("https://${gitPlus.remote.providerBaseUrl}", repoUser, repoName)
        val issueDescriptor = IssueDescriptor(repoDescriptor, number)
        val issueUrl = issueDescriptor.toUrl()
        return if (issueMap.containsKey(issueUrl)) {
            log.debug("returning cached version of issue $issueUrl")
            issueMap[issueUrl]!!
        } else {
            log.debug("no cached version of issue $issueUrl found, retrieving from remote API")
            val gpIssue: GPIssue = gitPlus.remote.getIssue(repoUser, repoName, number)
            issueMap.put(gpIssue.htmlUrl, gpIssue)
            gpIssue
        }
    }


    override fun getIssue(gitPlus: GitPlus, issueUrl: String): GPIssue {
        TODO()
    }

    override fun load(file: File) {
        if (file.exists()) {
            log.info("loading locally store issue records file from {}, only additional issue data will be retrieved from the remote API", file)
            val mapper = ObjectMapper()
            val fis = FileInputStream(file)
            fis.use {
                issueMap = mapper.readValue(fis, issueMap::class.java)
            }
        } else {
            log.info("no issue records file found at {}, all issue data will be retrieved from the remote API", file)
        }
    }

    override fun save(file: File) {
        val mapper = ObjectMapper()
        val fos = FileOutputStream(file)
        fos.use {
            mapper.writeValue(fos, issueMap)
        }

    }

    override fun isCached(issueUrl: String): Boolean {
        return issueMap.containsKey(issueUrl)
    }


}