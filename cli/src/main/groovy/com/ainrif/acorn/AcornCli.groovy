package com.ainrif.acorn

import groovy.util.logging.Slf4j
import picocli.CommandLine

import java.nio.file.Path

import static picocli.CommandLine.Model.CommandSpec
import static picocli.CommandLine.Model.OptionSpec
import static picocli.CommandLine.ParseResult

@Slf4j
class AcornCli {
    static void main(String[] args) {
        try {
            CmdArgs cmdArgs = parseArgs(args)
            new Acorn(cmdArgs.src, cmdArgs.dest, new HashMap<String, Object>())
        } catch (ExitException ee) {
            ee.description.ifPresent {
                log.error(it)
            }
            System.exit(ee.exitCode)
        }
    }

    static CmdArgs parseArgs(String[] args) {
        CommandSpec cmdSpec = buildSpec()

        def commandLine = new CommandLine(cmdSpec)
        commandLine.setUnmatchedArgumentsAllowed(false)

        ParseResult pr
        try {
            pr = commandLine.parseArgs(args)
        } catch (CommandLine.ParameterException e) {
            throw new ExitException(40, e.message)
        }

        if (pr.usageHelpRequested) {
            commandLine.usage(System.out, CommandLine.Help.Ansi.AUTO)
            throw ExitException.ok()
        }

        if (pr.versionHelpRequested) {
            commandLine.printVersionHelp(System.out, CommandLine.Help.Ansi.AUTO)
            throw ExitException.ok()
        }

        pr.matchedOptionValue('param', new HashMap<String, String>())
                .entrySet()
                .each {
                    try {
                        def parsed = new BigDecimal(it.value)
                        it.value = parsed
                    } catch (NumberFormatException ignore) {
                        // no-op
                    }
                }

        return new CmdArgs(
                src: pr.matchedOption('src').value as Path,
                dest: pr.matchedOption('dest').value as Path,
                params: pr.matchedOptionValue('param', new HashMap<String, Object>())
        )
    }

    static CommandSpec buildSpec() {
        CommandSpec.create()
                .mixinStandardHelpOptions(true)
                .name('acorn')
                .addOption(OptionSpec.builder('-s', '--src')
                        .required(true)
                        .paramLabel('PATH')
                        .type(Path)
                        .description('Template source directory')
                        .build())
                .addOption(OptionSpec.builder('-d', '--dest')
                        .required(true)
                        .paramLabel('PATH')
                        .type(Path)
                        .description('Destination directory. Will be created if absent')
                        .build())
                .addOption(OptionSpec.builder('-p', '--param')
                        .required(false)
                        .type(Map).auxiliaryTypes(String, Object)
                        .paramLabel('KEY=VALUE')
                        .description('Params to render template')
                        .build())
                .versionProvider({ new String[]{AcornCli.package.implementationVersion ?: 'UNPACKED'} })
    }
}
