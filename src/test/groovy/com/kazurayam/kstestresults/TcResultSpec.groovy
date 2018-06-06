package com.kazurayam.kstestresults

import java.nio.file.Path
import java.nio.file.Paths

import groovy.json.JsonOutput
import spock.lang.Specification

//@Ignore
class TcResultSpec extends Specification {

    // fields
    private static Path workdir
    private static Path fixture = Paths.get("./src/test/fixture/Results")
    private static TestResultsImpl tri
    private static TsResult tsr

    // fixture methods
    def setup() {}
    def cleanup() {}
    def setupSpec() {
        workdir = Paths.get("./build/tmp/${Helpers.getClassShortName(TcResultSpec.class)}")
        if (!workdir.toFile().exists()) {
            workdir.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture, workdir)
        tri = new TestResultsImpl(workdir, new TsName('TS1'))
        tsr = tri.getCurrentTsResult()
    }
    def cleanupSpec() {}

    // feature methods
    def testToJson() {
        setup:
        TcResult tcr = tsr.findOrNewTcResult(new TcName('TC1'))
        TargetURL tp = tcr.findOrNewTargetURL(new URL('http://demoaut.katalon.com/'))
        MaterialWrapper sw = tp.findOrNewMaterialWrapper('', FileType.PNG)
        when:
        def str = tcr.toString()
        def pretty = JsonOutput.prettyPrint(str)
        System.out.println("#testToString: \n${pretty}")
        then:
        str.startsWith('{"TcResult":{')
        str.contains('tcName')
        str.contains('TC1')
        str.contains('tcDir')
        str.contains(Helpers.escapeAsJsonText( sw.getMaterialFilePath().toString()))
        str.contains('tcStatus')
        str.contains(TcStatus.TO_BE_EXECUTED.toString())
        str.endsWith('}}')
    }



    // helper methods
}
