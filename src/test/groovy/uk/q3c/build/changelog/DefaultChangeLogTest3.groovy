package uk.q3c.build.changelog

import com.google.inject.Inject
import org.eclipse.jgit.api.Status
import spock.guice.UseModules
import spock.lang.Specification

/**
 *
 * Generates the change log for this project to its wiki, but with a changed name ('changelog-test.md') so as not to
 * overwrite the real one
 *
 * Created by David Sowerby on 26 Nov 2016
 */
@UseModules([ChangeLogModule])
class DefaultChangeLogTest3 extends Specification {

    @Inject
    ChangeLog changeLog

    def "generate log for this project, different file name"() {
        given:
        File userHome = new File(System.getProperty('user.home'))
        File gitHome = new File(userHome, 'git')
        String filename = 'changelog-test.md'
        File wikiDir = new File(gitHome, 'changelog.wiki')
        File outputFile = new File(wikiDir, filename)
        changeLog.outputFilename(filename).outputTarget(OutputTarget.WIKI_ROOT)
        changeLog.projectName('changelog').remoteRepoUser('davidsowerby').projectDirParent(gitHome)

        when:
        changeLog.generate()

        then:
        outputFile.exists()
        Status status = changeLog.gitPlus().local.status()
        status.clean

    }
}
