package uk.q3c.build.changelog

import uk.q3c.build.gitplus.gitplus.GitPlus
import java.io.File

/**
 * Created by David Sowerby on 12 Nov 2016
 */
interface ChangeLog : ChangeLogConfiguration {

    /**
     * A combination of [outputFilename] and [outputTarget], unless USE_FILE_SPEC is selected, in which case the [outputFileSpec] must
     * be given
     *
     * @throws UninitializedPropertyAccessException if [outputTarget] is [OutputTarget.USE_FILE_SPEC], but [outputFileSpec]
     * has not been set
     */
    fun outputFile(): File

    fun generate(): File
    fun gitPlus(): GitPlus
}