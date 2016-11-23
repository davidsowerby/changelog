package uk.q3c.build.changelog

import uk.q3c.build.gitplus.local.Tag

/**
 * Created by David Sowerby on 17 Nov 2016
 */
interface VersionTagFilter {

    fun isVersionTag(tag: Tag): Boolean

}