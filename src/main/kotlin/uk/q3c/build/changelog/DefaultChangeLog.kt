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
import java.io.File
import java.io.IOException
import java.io.StringWriter

/**
 * Builds a list of [VersionRecord].  The versions are identified by tags. Each VersionRecord holds a set of GitCommit instances, which make up a
 * version.  GitCommit parses commit messages fro issue fix references, and the VersionRecord collates those into groups of issues (for example, 'bug',
 * 'enhancement', 'quality'.  The mapping of issues labels to issue groups is user defined via [ChangeLogConfiguration.labelGroups].
 *
 *
 * Output format is defined by a Velocity template
 *
 *
 * @param gitPlus the gitPlus instance used for access to local and remote Git repositories
 *
 *
 * Created by David Sowerby on 07 Mar 2016
 */
class DefaultChangeLog @Inject constructor(val gitPlus: GitPlus, val configuration: ChangeLogConfiguration, val versionHistoryBuilder: VersionHistoryBuilder) : ChangeLog, ChangeLogConfiguration by configuration {
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

    override fun generate(): File {
        validate()
        prepareGitPlus()
        versionRecords.addAll(versionHistoryBuilder.build(gitPlus, configuration))


        versionRecords.forEach { vr ->
            try {
                vr.parse()
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
        FileUtils.writeStringToFile(outputFile, w.toString())
        if (outputTarget == OutputTarget.WIKI_ROOT) {
            val wikiLocal = gitPlus.wikiLocal
            wikiLocal.add(outputFile)
            wikiLocal.commit("Auto generated changelog")
            wikiLocal.push(false, false)
        }
        return outputFile
    }

    private fun prepareGitPlus() {
        gitPlus.local.projectName = projectName
        gitPlus.remote.repoUser = remoteRepoUser
        gitPlus.remote.repoName = projectName
        gitPlus.local.projectDirParent = projectDirParent
        if (outputTarget == OutputTarget.WIKI_ROOT) {
            gitPlus.wikiLocal.active(true)
        }
        gitPlus.execute()
    }


    override fun outputFile(): File {
        val outputFile: File
        when (outputTarget) {
            OutputTarget.USE_FILE_SPEC -> outputFile = outputFileSpec
            OutputTarget.PROJECT_ROOT -> outputFile = File(gitPlus.local.projectDir(), outputFilename)
            OutputTarget.PROJECT_BUILD_ROOT -> {
                val buildDir = File(gitPlus.local.projectDir(), "build")
                outputFile = File(buildDir, outputFilename)
            }
            OutputTarget.WIKI_ROOT -> outputFile = File(gitPlus.wikiLocal.projectDir(), outputFilename)
            OutputTarget.CURRENT_DIR -> {
                val currentDir = File(".")
                outputFile = File(currentDir, outputFilename)
            }
            else -> throw ChangeLogConfigurationException("Unrecognised output directory, " + outputTarget.name)
        }
        return outputFile
    }


}
