package com.ainrif.acorn.cli


import groovy.transform.EqualsAndHashCode

import java.nio.file.Path

@EqualsAndHashCode
class CmdArgs {
    Path src
    Path dest
    Map<String, Object> params
}
