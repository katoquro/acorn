package com.ainrif.gears.acorn

class Acorn {
    String getGreeting() {
        return 'Hello World!'
    }

    static void main(String[] args) {
        println new Acorn().greeting
    }
}
