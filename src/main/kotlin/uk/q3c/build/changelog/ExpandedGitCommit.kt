package uk.q3c.build.changelog

import uk.q3c.build.gitplus.local.GitCommit

/**
 * Created by David Sowerby on 25 Nov 2016
 */
class ExpandedGitCommit(val gitCommit: GitCommit, val expandedMessage: String, val expandedShortMessage: String)