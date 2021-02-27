package com.ainrif.acorn

import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Path

class AcornCliSpec extends Specification {
    @Unroll
    def "cli arguments should be parsed correctly"(String[] rawArgs, CmdArgs cmdArgs) {
        expect:
        AcornCli.parseArgs(rawArgs) == cmdArgs

        where:
        rawArgs << [
                '-s srcPath -d destPath -p param-1=one'.split(/ /),
                '--src srcPath --dest=destPath'.split(/ /),
                '-s srcPath -d destPath -pparam-1=one --param=param-2=two -p=param-3=three'.split(/ /)
        ]
        cmdArgs << [
                new CmdArgs(src: Path.of('srcPath'), dest: Path.of('destPath'), params: ['param-1': 'one']),
                new CmdArgs(src: Path.of('srcPath'), dest: Path.of('destPath'), params: Map.<String, String> of()),
                new CmdArgs(src: Path.of('srcPath'), dest: Path.of('destPath'), params: ['param-1': 'one',
                                                                                         'param-2': 'two',
                                                                                         'param-3': 'three'])
        ]
    }
}
