package com.ainrif.acorn

import groovy.text.GStringTemplateEngine
import groovy.text.SimpleTemplateEngine

import java.nio.file.Files
import java.nio.file.Path

import static java.nio.charset.StandardCharsets.UTF_8

class Acorn {
    public static final String TMPL_EXT = '.tmpl'
    public static final String HOLDER_FILE = 'HOLDER'

    final Path src
    final Path dest
    final Map<String, Object> params
    final SimpleTemplateEngine simpleEngine
    final GStringTemplateEngine gStringEngine

    Acorn(Path src, Path dest, Map<String, Object> params) {
        this.src = src
        this.dest = dest
        this.params = params
        this.simpleEngine = new SimpleTemplateEngine()
        this.gStringEngine = new GStringTemplateEngine()
    }

    void generate() {
        new Validator().validate()

        dest.toFile().mkdirs()

        copyDir(src.toFile(), dest.toFile())
    }

    void copyDir(File srcDir, File destDir) {
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

            throw new RuntimeException("UNSUPPORTED FILE TYPE")
        }
    }
}
