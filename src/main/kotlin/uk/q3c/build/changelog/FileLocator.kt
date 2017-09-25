package uk.q3c.build.changelog

import uk.q3c.build.gitplus.gitplus.GitPlus
import java.io.File

/**
 * Created by David Sowerby on 20 Sep 2017
 */
interface FileLocator {
    fun locateChangeLogFile(configuration: ChangeLogConfiguration, gitPlus: GitPlus): File
    //    fun locateVersionHistoryFile(configuration: ChangeLogConfiguration, gitPlus: GitPlus): File
    fun locateIssueRecordsFile(configuration: ChangeLogConfiguration, gitPlus: GitPlus): File
}

class DefaultFileLocator : FileLocator {

    override fun locateChangeLogFile(configuration: ChangeLogConfiguration, gitPlus: GitPlus): File {
        return locateOutputDir(configuration, gitPlus, configuration.outputFilename)
    }

    override fun locateIssueRecordsFile(configuration: ChangeLogConfiguration, gitPlus: GitPlus): File {
        return locateOutputDir(configuration, gitPlus, configuration.issuesFilename)
    }


    private fun locateOutputDir(configuration: ChangeLogConfiguration, gitPlus: GitPlus, filename: String): File {
        return when (configuration.outputTarget) {
            OutputTarget.USE_DIRECTORY_SPEC -> File(configuration.outputDirectorySpec, filename)
            OutputTarget.PROJECT_ROOT -> File(gitPlus.local.projectDir(), filename)
            OutputTarget.PROJECT_BUILD_ROOT -> {
                val buildDir = File(gitPlus.local.projectDir(), "build")
                File(buildDir, filename)
            }
            OutputTarget.WIKI_ROOT -> File(gitPlus.wikiLocal.projectDir(), filename)
            OutputTarget.CURRENT_DIR -> {
                val currentDir = File(".")
                File(currentDir, filename)
            }
        }
    }


}