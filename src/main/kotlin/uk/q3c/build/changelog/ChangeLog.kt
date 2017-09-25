package uk.q3c.build.changelog

import uk.q3c.build.gitplus.gitplus.GitPlus
import java.io.File

/**
 * Created by David Sowerby on 12 Nov 2016
 */
interface ChangeLog : ChangeLogConfiguration {

    /**
     * Must be a val, because Kotlin delegation will not recognise a replacement made after construction
     */
    val configuration: ChangeLogConfiguration

    /**
     * A combination of [outputFilename] and [outputTarget], unless USE_DIRECTORY_SPEC is selected, in which case the [outputDirectorySpec] must
     * be given
     *
     * @throws UninitializedPropertyAccessException if [outputTarget] is [OutputTarget.USE_DIRECTORY_SPEC], but [outputDirectorySpec]
     * has not been set
     */
    fun outputFile(): File

    fun generate(): File
    fun gitPlus(): GitPlus
}