package uk.q3c.build.changelog

import spock.lang.Specification

/**
 * Created by David Sowerby on 30 Nov 2016
 */
class ChangeLogFactoryTest extends Specification {
    def "GetInstance"() {

        expect:
        ChangeLogFactory.getInstance() instanceof DefaultChangeLog
    }
}
