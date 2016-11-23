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
import uk.q3c.build.gitplus.local.GitCommit
import uk.q3c.build.gitplus.local.Tag
import java.io.File
import java.io.IOException
import java.io.StringWriter
import java.util.*

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
class DefaultChangeLog @Inject constructor(val gitPlus: GitPlus, val configuration: ChangeLogConfiguration) : ChangeLog, ChangeLogConfiguration by configuration {
    private val log = LoggerFactory.getLogger(this.javaClass.name)

    val velocityContext: VelocityContext
    val velocityTemplate: Template
    val tagMap: MutableMap<String, Tag> = mutableMapOf()
    val versionRecords: MutableList<VersionRecord> = mutableListOf()

    init {
        val velocityEngine = VelocityEngine()
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
        velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader::class.java.name)
        velocityEngine.init()
        velocityTemplate = velocityEngine.getTemplate(templateName)
        velocityContext = VelocityContext()
    }

    val projectName: String
        get() = gitPlus.local.projectName


    fun createChangeLog(): File {
        assembleVersionRecords()


        versionRecords.forEach { vr ->
            try {
                vr.parse()
            } catch (e: IOException) {
                log.error("Failed to parse a version record", e)
            }
        }
        velocityContext.put("projectName", projectName)
        velocityContext.put("versionRecords", versionRecords)
        velocityContext.put("baseUrl", gitPlus.remote.repoBaselUrl())
        velocityContext.put("tagUrl", gitPlus.remote.tagUrl())
        velocityContext.put("dateTool", DateTool())


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


    private fun assembleVersionRecords() {
        buildTagMap()
        val commits = gitPlus.local.extractDevelopCommits()
        var index = scrollToStartCommit(commits)


        //If constructing changelog for a released version, most recent commit has a tag
        //if not yet released use 'current build' pseudo tag for most recent commit
        val firstCommit = commits[index]
        val tagForFirstCommit = tagForCommit(firstCommit)
        val tag: Tag = if (tagForFirstCommit.isPresent) tagForFirstCommit.get() else CurrentBuildTag(firstCommit)

        var currentVersionRecord = VersionRecord(tag, this, gitPlus)
        currentVersionRecord.addCommit(firstCommit)
        versionRecords.add(currentVersionRecord)

        //We need to count versions, but tagCount does not include pseudo tag, as that is not a version
        var tagCount = if (tag.tagType == Tag.TagType.PSEUDO) 0 else 1
        var parseComplete = false
        var onFinalTag = false
        while (index < commits.size - 1 && !parseComplete) {
            index++
            val currentCommit = commits[index]
            val tagForCommit = tagForCommit(currentCommit)
            if (tagForCommit.isPresent && isVersionTag(tagForCommit.get())) {
                if (onFinalTag) {
                    parseComplete = true
                } else {
                    currentVersionRecord = VersionRecord(tagForCommit.get(), this, gitPlus)
                    versionRecords.add(currentVersionRecord)
                    tagCount++
                    currentVersionRecord.addCommit(currentCommit)
                }
            } else {
                currentVersionRecord.addCommit(currentCommit)
            }
        }

    }


    /**
     * returns the index to the first selected commit (where selected is determined by the setting of [from]
     */
    private fun scrollToStartCommit(commits: List<GitCommit>): Int {
        var index = 0
//        var commit = commits[index]
//
//        var startFound = false
//        if (configuration.fromUfromfromId == LATEST_COMMIT) {
//            startFound = true
//        }
//        while (!startFound && index < commits.size) {
//            val tagForCommit = tagForCommit(commit)
//            if (tagForCommit.isPresent && isVersionTag(tagForCommit.get())) {
//                if (isFromVersion(tagForCommit.get().tagName) || (fromCommitId == tagForCommit.get().tagName)) {
//                    startFound = true
//                } else {
//                    index++
//                    commit = commits[index]
//                }
//            } else {
//                index++
//                commit = commits[index]
//            }
//        }
//        if (!startFound) {
//            throw ChangeLogConfigurationException("Unable to find the 'fromVersion' of " + fromCommitId)
//        }
        return index
    }

    /**
     * to be replaced by a filter

     * @param tagForCommit
     * *
     * @return
     */
    //TODO replace with filter or similar https://github.com/davidsowerby/changelog/issues/3
    private fun isVersionTag(tagForCommit: Tag): Boolean {
        return true
    }


    private fun buildTagMap() {
        val tags = gitPlus.local.tags()
        tagMap.clear()
        tags.forEach { t -> tagMap.put(t.commit.hash, t) }
    }

    private fun tagForCommit(commit: GitCommit): Optional<Tag> {
        return if (tagMap.containsKey(commit.hash)) Optional.of<Tag>(tagMap[commit.hash]) else Optional.empty<Tag>()
    }


}
