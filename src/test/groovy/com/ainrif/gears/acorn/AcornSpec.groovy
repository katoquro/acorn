package com.ainrif.gears.acorn


import spock.lang.Specification

class AcornSpec extends Specification {
    def "application has a greeting"() {
        setup:
        def app = new Acorn()

        when:
        def result = app.greeting

        then:
        result != null
    }
}
