package uk.q3c.build.changelog

import com.google.inject.Guice
import com.google.inject.Injector
import spock.lang.Specification

/**
 * Created by David Sowerby on 26 Nov 2016
 */
class ChangeLogModuleTest extends Specification {

    def "module test"() {
        when:
        Injector injector = Guice.createInjector(new ChangeLogModule())

        then:
        injector.getInstance(ChangeLog.class) instanceof DefaultChangeLog
        injector.getInstance(ChangeLogConfiguration.class) instanceof DefaultChangeLogConfiguration
        injector.getInstance(VersionHistoryBuilder.class) instanceof DefaultVersionHistoryBuilder
    }
}
