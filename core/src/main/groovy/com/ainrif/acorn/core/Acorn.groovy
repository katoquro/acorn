package com.ainrif.acorn.core

import groovy.text.GStringTemplateEngine
import groovy.text.SimpleTemplateEngine
import groovy.util.logging.Slf4j

import java.nio.file.Files
import java.nio.file.Path

import static java.nio.charset.StandardCharsets.UTF_8

@Slf4j
class Acorn {
    public static final String TMPL_EXT = '.tmpl'
    public static final String HOLDER_FILE = 'HOLDER'

    final File src
    final File dest
    final Map<String, Object> params
    final SimpleTemplateEngine simpleEngine
    final GStringTemplateEngine gStringEngine

    Acorn(Path src, Path dest, Map<String, Object> params) {
        this.src = src.toFile()
        this.dest = dest.toFile()
        this.params = params
        this.simpleEngine = new SimpleTemplateEngine()
        this.gStringEngine = new GStringTemplateEngine()
    }

    void generate() {
        validate()

        copyDir(src, dest)
    }

    void copyDir(File srcDir, File destDir) {
        log.debug('Process - {}', srcDir.absolutePath)

        srcDir.eachFile {
            def fileName = it.name

            if (it.isDirectory()) {
                String destPath = fileName.contains('${') ?
                        simpleEngine.createTemplate(fileName).make(params).toString() :
                        fileName

                def newDest = new File(destDir, destPath)
                newDest.mkdirs()
                copyDir(it, newDest)

                return
            }

            if (it.isFile()) {
                if (HOLDER_FILE == fileName) {
                    // no-op for folder holder file
                    return
                }

                String destName = fileName.contains('${') ?
                        simpleEngine.createTemplate(fileName).make(params).toString() :
                        fileName

                if (destName.endsWith(TMPL_EXT)) {
                    def destFilePath = destDir.toPath().resolve(destName - TMPL_EXT).toAbsolutePath().toString()
                    gStringEngine.createTemplate(it)
                            .make(params)
                            .writeTo(new FileWriter(destFilePath, UTF_8))
                            .close()

                } else {
                    Files.copy(it.toPath(), destDir.toPath().resolve(destName))
                }

                return
            }

            throw new ExitException(1, "UNSUPPORTED FILE TYPE")
        }
    }

    void validate() {
        if (!src.exists()) {
            throw new ExitException(40, "SRC path doesn't exists. ${src.absolutePath}")
        }

        if (!src.canRead()) {
            throw new ExitException(40, "SRC path cannot be read. ${src.absolutePath}")
        }

        if (0 == src.list().length) {
            throw new ExitException(40, "SRC path directory is empty. ${src.absolutePath}")
        }

        if (dest.exists()) {
            throw new ExitException(40, "DEST already exists. ${dest.absolutePath}")
        }

        try {
            dest.mkdirs()
        } catch (SecurityException ignore) {
            throw new ExitException(40, "DEST path cannot be created. ${dest.absolutePath}")
        }
    }
}
