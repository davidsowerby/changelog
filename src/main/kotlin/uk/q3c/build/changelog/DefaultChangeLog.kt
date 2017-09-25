package uk.q3c.build.changelog

import com.google.inject.Inject
import org.apache.commons.io.FileUtils
import org.apache.velocity.Template
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import org.apache.velocity.tools.generic.DateTool
import org.slf4j.LoggerFactory
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.build.gitplus.local.CloneExistsResponse
import java.io.File
import java.io.IOException
import java.io.StringWriter

/**
 * Builds a list of [VersionRecord].  The versions are identified by tags. Each VersionRecord holds a set of GitCommit instances, which make up a
 * version.  GitCommit parses commit messages fro issue fix references, and the VersionRecord collates those into groups of issues (for example, 'bug',
 * 'enhancement', 'quality'.  The mapping of issues labels to issue groups is user defined via [ChangeLogConfiguration.labelGroups].
 *
 *  Note that Kotlin delegation does not allow the configuration to be changed for another instance, which means that [ChangeLogConfiguration.copyFrom] is needed
 *
 * Output format is defined by a Velocity template
 *
 *
 * @param gitPlus the gitPlus instance used for access to local and remote Git repositories
 *
 *
 * Created by David Sowerby on 07 Mar 2016
 */
class DefaultChangeLog @Inject constructor(
        val gitPlus: GitPlus,
        override val configuration: ChangeLogConfiguration,
        val versionHistoryBuilder: VersionHistoryBuilder,
        val issueRecords: IssueRecords,
        val fileLocator: FileLocator)

    : ChangeLog, ChangeLogConfiguration by configuration {


    private val log = LoggerFactory.getLogger(this.javaClass.name)

    val velocityContext: VelocityContext
    val velocityTemplate: Template
    val versionRecords: MutableList<VersionRecord> = mutableListOf()

    init {
        val velocityEngine = VelocityEngine()
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader::class.java.name)
        velocityEngine.init()
        velocityTemplate = velocityEngine.getTemplate(templateName)
        velocityContext = VelocityContext()
    }

    override fun gitPlus(): GitPlus {
        return gitPlus
    }

    private fun issueRecordFile(): File {
        return fileLocator.locateIssueRecordsFile(configuration, gitPlus)
    }

    private fun saveIssueRecords() {
        if (configuration.storeIssuesLocally) {
            issueRecords.save(issueRecordFile())
            // If using the wiki to hold the issue records we need to add the file to Git
            if (configuration.outputTarget == OutputTarget.WIKI_ROOT) {
                gitPlus.wikiLocal.add(issueRecordFile())
            }
        } else {
            log.info("storing of issue records locally has been disabled [configuration.storeIssuesLocally], performance of future log generation may be affected")
        }
    }

    private fun loadIssueRecords() {
        if (configuration.useStoredIssues) {
            issueRecords.load(issueRecordFile())
        } else {
            log.info("Loading of locally stored issue records is disabled [configuration.useStoredIssues], performance may be affected")
        }
    }

    override fun generate(): File {
        validate()
        prepareGitPlus()
        loadIssueRecords()
        versionRecords.addAll(versionHistoryBuilder.build(gitPlus, configuration))


        versionRecords.forEach { vr ->
            try {
                vr.parse(issueRecords)
            } catch (e: IOException) {
                log.error("Failed to parse a version record", e)
            }
        }
        velocityContext.put("projectName", gitPlus.local.projectName)
        velocityContext.put("versionRecords", versionRecords)
        velocityContext.put("baseUrl", gitPlus.remote.repoBaselUrl())
        velocityContext.put("tagUrl", gitPlus.remote.tagUrl())
        velocityContext.put("dateTool", DateTool())
        velocityContext.put("configuration", configuration)


        val w = StringWriter()
        velocityTemplate.merge(velocityContext, w)
        val outputFile = outputFile()
        log.debug("Output file is: {}", outputFile.absolutePath)
        FileUtils.writeStringToFile(outputFile, w.toString())
        if (outputTarget == OutputTarget.WIKI_ROOT) {
            val wikiLocal = gitPlus.wikiLocal
            wikiLocal.add(outputFile)
            wikiLocal.commit("Auto generated changelog")
            wikiLocal.push(false, false)
        }
        saveIssueRecords()
        return outputFile
    }

    private fun prepareGitPlus() {
        log.debug("Preparing GitPlus")
        gitPlus.propertiesFromGradle()
        gitPlus.local.projectName = projectName
        gitPlus.remote.repoUser = remoteRepoUser
        gitPlus.remote.repoName = projectName
        gitPlus.local.projectDirParent = projectDirParent
        log.debug("Output target is {}", outputTarget)
        if (outputTarget == OutputTarget.WIKI_ROOT) {
            gitPlus.wikiLocal.active(true)
            gitPlus.wikiLocal.cloneFromRemote = true
            // when running Gradle directly from command line, don't want to fail unnecessarily
            gitPlus.wikiLocal.cloneExistsResponse = CloneExistsResponse.PULL
        }
        gitPlus.execute()
    }


    override fun outputFile(): File {
        return fileLocator.locateChangeLogFile(configuration, gitPlus)
    }


}
