package com.ainrif.acorn

class ExitException extends Exception {
    final int exitCode
    final Optional<String> description

    private ExitException(int exitCode) {
        this.exitCode = exitCode
        this.description = Optional.empty()
    }

    ExitException(int exitCode, String message) {
        this.exitCode = exitCode
        this.description = Optional.of(message)
    }

    static ExitException ok() {
        return new ExitException(0)
    }
}
