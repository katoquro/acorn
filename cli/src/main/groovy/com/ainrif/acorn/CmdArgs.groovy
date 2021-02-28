package com.ainrif.acorn


import groovy.transform.EqualsAndHashCode

import java.nio.file.Path

@EqualsAndHashCode
class CmdArgs {
    Path src
    Path dest
    Map<String, Object> params
}