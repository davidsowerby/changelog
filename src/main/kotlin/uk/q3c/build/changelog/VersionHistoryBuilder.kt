package uk.q3c.build.changelog

/**
 * Builds a list of [VersionRecord], ordered by time (with most recent at index 0).  Each [VersionRecord] holds one or commits that
 * makes up a version
 *
 * The list may be constrained by settings in [ChangeLogConfiguration], for example, to provide only the last N versions
 *
 * Created by David Sowerby on 18 Nov 2016
 */
interface VersionHistoryBuilder