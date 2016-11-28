package uk.q3c.build.changelog

import uk.q3c.build.gitplus.local.Tag

/**
 * Simply returns true for all tags - therefore all tags are treated as version tags
 *
 * Created by David Sowerby on 23 Nov 2016
 */
class AllTagsAreVersionsTagFilter : VersionTagFilter {

    override fun isVersionTag(tag: Tag): Boolean {
        return true
    }
}