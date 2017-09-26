package uk.q3c.build.changelog

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.time.StopWatch
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import uk.q3c.build.gitplus.GitPlusFactory
import uk.q3c.build.gitplus.gitplus.GitPlus
import uk.q3c.util.testutil.FileTestUtil

import java.util.concurrent.TimeUnit
/**
 * Created by David Sowerby on 07 Mar 2016
 */
class DefaultChangeLogFunctionalTest extends Specification {


    @Rule
    TemporaryFolder temporaryFolder
    File temp

    ChangeLog changelog
    GitPlus gitPlus


    final String projectName = 'changelog'
    final String userName = 'davidsowerby'
    File logFile1
    File logFile2


    def setup() {

        temp = temporaryFolder.getRoot()
        gitPlus = GitPlusFactory.instance
        changelog = ChangeLogFactory.instance
        changelog.projectName = projectName
        changelog.projectDirParent = temp
        changelog.remoteRepoUser = userName
        gitPlus.cloneFromRemote(temp, userName, projectName, true)


    }

    def "generate without issue load, then with and show difference"() {
        given:
        gitPlus.execute()

        long time1
        long time2
        StopWatch stopWatch = new StopWatch()




        when: "generated to wiki with no stored issues"
        changelog.configuration.useStoredIssues = false
        stopWatch.start()
        logFile1 = changelog.generate()
        stopWatch.stop()
        time1 = stopWatch.getTime(TimeUnit.MILLISECONDS)

        then: "keep copy of file for comparison"
        saveFirstFile()
        println "Run time with NO locally stored issue records = " + time1

        when: "locally stored issues used"
        stopWatch.reset()
        changelog.configuration.useStoredIssues = true
        stopWatch.start()
        logFile1 = changelog.generate()
        stopWatch.stop()
        time2 = stopWatch.getTime(TimeUnit.MILLISECONDS)

        then:
        FileTestUtil.compare(logFile1, logFile2)
        time2 < time1
        println "Run time with NO locally stored issue records = " + time1
        println "Run time with locally stored issue records = " + time2

    }

    private boolean saveFirstFile() {
        logFile2 = new File(logFile1.absolutePath + "0")
        FileUtils.copyFile(logFile1, logFile2)
        return true
    }


}