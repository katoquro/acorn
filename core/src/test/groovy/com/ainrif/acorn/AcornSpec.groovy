package com.ainrif.acorn

import com.ainrif.acorn.Acorn
import spock.lang.Specification

import java.nio.file.Path

class AcornSpec extends Specification {

    Path srcBasePath = Path.of(System.getProperty('user.dir'), 'src/test/resources/test-data')
    Path destBasePath = File.createTempDir('acorn-test').toPath()

    def "acorn can generate result for empty src"() {
        when: "select folder for destination"
        def destDirName = specificationContext.currentIteration.name.replaceAll(/[^\w]/, '')
        def dest = destBasePath.resolve(destDirName)

        then: "first run in doesn't exist"
        !dest.toFile().exists()

        when:
        def acorn = new Acorn(srcBasePath.resolve('empty'), dest, [:])
        acorn.generate()

        then: 'dest folder exists'
        dest.toFile().exists()
        dest.toFile().directory

        and: 'holder file is not copied'
        !dest.resolve(Acorn.HOLDER_FILE).toFile().exists()

        when: 'generate over existing folder'
        acorn.generate()

        then:
        noExceptionThrown()
    }

    def "nested folder structure can be copied"() {
        given:
        def src = srcBasePath.resolve('nested-copy')
        def destDirName = specificationContext.currentIteration.name.replaceAll(/[^\w]/, '')
        def dest = destBasePath.resolve(destDirName)

        when:
        new Acorn(src, dest, [:]).generate()

        then:
        with(dest.resolve('file.txt').toFile()) {
            it.exists()
            it.text == 'top-level content'
        }

        and:
        with(dest.resolve('subdir').resolve('file.txt').toFile()) {
            it.exists()
            it.text == 'subdir content'
        }

        and: 'no new files were added, just 2 files and one dir'
        def fileCount = 0
        dest.toFile().eachFileRecurse { fileCount++ }
        fileCount == 3
    }

    def "only *.tmpl files are processed"() {
        given:
        def src = srcBasePath.resolve('file-and-template')
        def destDirName = specificationContext.currentIteration.name.replaceAll(/[^\w]/, '')
        def dest = destBasePath.resolve(destDirName)

        when:
        new Acorn(src, dest, [testVar: '4 2']).generate()

        then:
        with(dest.resolve('plain-file.txt').toFile()) {
            it.exists()
            it.text == '''
                    ${testVar}
                    <% out << testVar %> 
                    '''.stripIndent().strip()
        }

        and:
        with(dest.resolve('template.txt').toFile()) {
            it.exists()
            it.text.strip() == '''
                    4 2
                    4 2   
                    '''.stripIndent().strip()
        }
    }

    def "templates can be used in file and dir names"() {
        given:
        def src = srcBasePath.resolve('templated-path-and-name')
        def destDirName = specificationContext.currentIteration.name.replaceAll(/[^\w]/, '')
        def dest = destBasePath.resolve(destDirName)

        when:
        new Acorn(src, dest, [nameVar: 'named', folderVar: 'one/two/three', emptyValue: '']).generate()

        then:
        dest.resolve('named-file.txt').toFile().exists()
        dest.resolve('file-in-root.txt').toFile().exists()
        dest.resolve('one').resolve('two').resolve('three').resolve('nested-file.txt').toFile().exists()
    }

    def "path templated can overlap each other"() {
        given:
        def src = srcBasePath.resolve('overlapping-path-template')
        def destDirName = specificationContext.currentIteration.name.replaceAll(/[^\w]/, '')
        def dest = destBasePath.resolve(destDirName)

        when:
        new Acorn(src, dest, [firstPath: '/one/two/', secondPath: 'one/two/three']).generate()

        then:
        with(dest.resolve('one').resolve('two').resolve('first-file.txt').toFile()) {
            it.exists()
            it.file
        }

        with(dest.resolve('one').resolve('two').resolve('three').resolve('second-file.txt').toFile()) {
            it.exists()
            it.file
        }
    }

    def "escape chars should work correctly"() {
        given:
        def src = srcBasePath.resolve('escape-chars')
        def destDirName = specificationContext.currentIteration.name.replaceAll(/[^\w]/, '')
        def dest = destBasePath.resolve(destDirName)
        def expected = 'Dear Andrew. You have 2 things in your cart.\nTotal price is $18'

        when:
        new Acorn(src, dest, [user: 'Andrew', count: 2, totalPrice: 18]).generate()

        then:
        with(dest.resolve('simple.txt').toFile()) {
            it.exists()
            it.text == expected
        }

        with(dest.resolve('advanced.txt').toFile()) {
            it.exists()
            it.text == expected
        }
    }
}
