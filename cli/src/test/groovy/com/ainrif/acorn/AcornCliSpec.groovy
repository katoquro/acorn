package com.ainrif.acorn

import com.ainrif.acorn.cli.AcornCli
import com.ainrif.acorn.cli.CmdArgs
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

    def "params can be parsed as numbers"() {
        expect:
        AcornCli.parseArgs('-s any -d any -ptxt=42.a -pone=42 -p two=42.42 -p three=1. -p four=1.0000'.split(/ /)).params ==
                [txt: '42.a', one: 42, two: 42.42, three: 1, four: 1.0000]
    }
}
