package com.kazurayam.materials

import com.kazurayam.materials.impl.MaterialRepositoryImpl
import com.kazurayam.materials.impl.TSuiteResultIdImpl
import com.kazurayam.materials.repository.RepositoryRoot
import groovy.json.JsonOutput
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime

//@Ignore
class TSuiteResultSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(TSuiteResultSpec.class);

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialRepositoryImpl mri_
    private static MaterialStorage ms_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(TSuiteResultSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        Path materialsDir = workdir_.resolve('Materials')
        mri_ = MaterialRepositoryImpl.newInstance(materialsDir)
        ms_  = MaterialStorageFactory.createInstance(workdir_.resolve('Storage'))
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testSetGetParent() {
        when:
        RepositoryRoot repoRoot = mri_.getRepositoryRoot()
        TSuiteName tsn = new TSuiteName('TS3')
        TExecutionProfile tep = new TExecutionProfile("default")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance(LocalDateTime.now())
        TSuiteResult tsr = TSuiteResult.newInstance(tsn, tep, tst)
        TSuiteResult modified = tsr.setParent(repoRoot)
        then:
        modified.getParent() == repoRoot
    }

    def testGetTSuiteName() {
        when:
        TSuiteResultId tsri = TSuiteResultId.newInstance(
            new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TSuiteResult tsr = mri_.getTSuiteResult(tsri)
        then:
        tsr.getTSuiteName() == new TSuiteName('Test Suites/main/TS1')
    }

    def testGetTSuiteTimestamp() {
        when:
        TSuiteResultId tsri = TSuiteResultId.newInstance(
            new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TSuiteResult tsr = mri_.getTSuiteResult(tsri)
        then:
        tsr.getTSuiteTimestamp() == TSuiteTimestamp.newInstance('20180530_130419')
    }
    
    def testGetId() {
        setup:
        TSuiteName tsn = new TSuiteName('Test Suites/main/TS1')
        TExecutionProfile tep = new TExecutionProfile("CURA_ProductionEnv")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance('20180530_130419')
        TSuiteResultId tsri = TSuiteResultIdImpl.newInstance(tsn, tep, tst)
        when:
        TSuiteResult tsr = mri_.getTSuiteResult(tsri)
        then:
        tsr.getId().equals(tsri)
    }
    
    def testGetMaterialList_all() {
        setup:
        TSuiteName tsn = new TSuiteName('47News_chronos_capture')
        TExecutionProfile tep = new TExecutionProfile("default")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance('20190215_222146')
        TSuiteResultId tsri = TSuiteResultIdImpl.newInstance(tsn, tep, tst)
        when:
        TSuiteResult tsr = ms_.getTSuiteResult(tsri)
        List<Material> materialList = tsr.getMaterialList()
        then:
        materialList.size() == 1
    }
    
    def testGetMaterialList_withParam() {
        setup:
        TSuiteResultId tsri = TSuiteResultIdImpl.newInstance(
                new TSuiteName('47News_chronos_capture'),
                new TExecutionProfile("default"),
                TSuiteTimestamp.newInstance('20190215_222146'))
        when:
        TSuiteResult tsr = ms_.getTSuiteResult(tsri)
        List<Material> materialListAll = tsr.getMaterialList()
        then:
        materialListAll.size() == 1
        when:
        Material material = materialListAll.get(0)
        List<Material> materialListSelected = tsr.getMaterialList(material.getPathRelativeToTSuiteTimestamp())
        then:
        materialListSelected.size()== 1
    }

    def testGetTCaseResult() {
        when:
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TSuiteResult tsr = mri_.getTSuiteResult(tsri)
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        then:
        tcr != null
        tcr.getTCaseName() == new TCaseName('Test Cases/main/TC1')
        tcr.getParent() == tsr
    }

    def testGetTCaseResultList() {
        when:
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130604'))
        TSuiteResult tsr = mri_.getTSuiteResult(tsri)
        List<TCaseResult> tCaseResults = tsr.getTCaseResultList()
        then:
        tCaseResults != null
        tCaseResults.size() == 2
    }

    def testAddTCaseResult() {
        when:
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130604'))
        TSuiteResult tsr = mri_.getTSuiteResult(tsri)
        TCaseName tcn = new TCaseName('TSX')
        TCaseResult tcr = TCaseResult.newInstance(tcn).setParent(tsr)
        // attention!
        tsr.addTCaseResult(tcr)
        TCaseResult tcr2 = tsr.getTCaseResult(tcn)
        then:
        tcr2 != null
        tcr2.getParent() == tsr
        tcr == tcr2
    }

    def testAddTCaseResult_parentIsNotSet() {
        when:
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130604'))
        TSuiteResult tsr = mri_.getTSuiteResult(tsri)
        //TCaseResult tcr = new TCaseResult(new TCaseName('TSX')).setParent(tsr)
        TCaseResult tcr = TCaseResult.newInstance(new TCaseName('TSX'))
        tsr.addTCaseResult(tcr)
        then:
        thrown(IllegalArgumentException)
    }

    def test_ensureTCaseResult() {
        when:
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130604'))
        TSuiteResult tsr = mri_.getTSuiteResult(tsri)
        // attention!
        TCaseResult tcr = tsr.ensureTCaseResultPresent(new TCaseName('TSX'))
        then:
        tcr != null
        tcr.getParent() == tsr
        when:
        TCaseResult tcr2 = tsr.ensureTCaseResultPresent(new TCaseName('TSX'))
        then:
        tcr2 != null
        tcr2.getParent() == tsr
        //
        tcr == tcr2
    }

    def testGetTSuiteNameDirectory() {
        when:
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TSuiteResult tsr = mri_.getTSuiteResult(tsri)
        Path dir = tsr.getTSuiteNameDirectory()
        then:
        dir.getFileName().toString() == 'main.TS1'
    }

    def testGetTSuiteTimestampDirectory() {
        when:
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TSuiteResult tsr = mri_.getTSuiteResult(tsri)
        Path dir = tsr.getTSuiteTimestampDirectory()
        then:
        dir.getFileName().toString() == '20180530_130419'
    }

    def testEquals() {
        when:
        RepositoryRoot repoRoot = mri_.getRepositoryRoot()
        TSuiteName tsn = new TSuiteName('Test Suites/main/TS1')
        TExecutionProfile tep = new TExecutionProfile("CURA_ProductionEnv")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance('20180530_130419')
        TSuiteResult tsr = repoRoot.getTSuiteResult(tsn, tep, tst)
        TSuiteResult other = TSuiteResult.newInstance(tsn, tep, tst).setParent(repoRoot)
        then:
        tsr == other
        when:
        TSuiteResult more = TSuiteResult.newInstance(
                new TSuiteName('Test Suites/main/TS2') ,
                new TExecutionProfile("CURA_ProductionEnv"),
                tst).setParent(repoRoot)
        then:
        tsr != more
    }

    def testHashCode() {
        when:
        RepositoryRoot repoRoot = mri_.getRepositoryRoot()
        TSuiteName tsn = new TSuiteName('Test Suites/main/TS1')
        TExecutionProfile tep = new TExecutionProfile("CURA_ProductionEnv")
        TSuiteTimestamp tst = TSuiteTimestamp.newInstance('20180530_130419')
        TSuiteResultId tsri = TSuiteResultId.newInstance(tsn, tep, tst)
        TSuiteResult tsr = mri_.getTSuiteResult(tsri)
        TSuiteResult other = TSuiteResult.newInstance(tsn, tep, tst).setParent(repoRoot)
        then:
        tsr.hashCode() == other.hashCode()
    }

    def testToString() {
        setup:
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"),
                TSuiteTimestamp.newInstance('20180530_130419'))
        TSuiteResult tsr = mri_.getTSuiteResult(tsri)
        when:
        TCaseResult tcr = tsr.getTCaseResult(new TCaseName('Test Cases/main/TC1'))
        def s = tsr.toString()
        logger_.debug("#testToJson ${s}")
        logger_.debug("#testToJson ${JsonOutput.prettyPrint(s)}")
        then:
        s.startsWith('{"TSuiteResult":{')
        s.contains('tSuiteName')
        s.contains('TS1')
        s.contains('tCaseName')
        s.contains('TC1')
        s.contains(Helpers.escapeAsJsonText('http://demoaut.katalon.com/'))
        s.endsWith('}}')
    }

    def testTreeviewTitle() {
        setup:
        String tsnStr = 'Test Suites/main/TS1'
        String tepStr = 'CURA_ProductionEnv'
        String tstStr = '20180530_130419'
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName(tsnStr),
                new TExecutionProfile(tepStr),
                TSuiteTimestamp.newInstance(tstStr))
        when:
        TSuiteResult tsr = mri_.getTSuiteResult(tsri)
        then:
        tsr.treeviewTitle().equals('main.TS1/CURA_ProductionEnv/20180530_130419')
    }
        
    def test_TimestampFirstTSuiteResultComparator() {
        setup:
        List<TSuiteResultId> tSuiteResultIdList = mri_.getTSuiteResultIdList(
                new TSuiteName('Test Suites/main/TS1'),
                new TExecutionProfile("CURA_ProductionEnv"))
        List<TSuiteResult> tSuiteResultList = mri_.getTSuiteResultList(tSuiteResultIdList)
        when:
        Collections.sort(tSuiteResultList, new com.kazurayam.materials.TSuiteResult.TimestampFirstTSuiteResultComparator())
        then:
        tSuiteResultList.get(0).getTSuiteTimestamp().equals(new TSuiteTimestamp('20181014_060501'))
        tSuiteResultList.get(1).getTSuiteTimestamp().equals(new TSuiteTimestamp('20181014_060500'))
        tSuiteResultList.get(2).getTSuiteTimestamp().equals(new TSuiteTimestamp('20180805_081908'))
    }

    def test_getTExecutionProfileDirectory() {
        setup:
        String tsnStr = 'Test Suites/main/TS1'
        String tepStr = 'CURA_ProductionEnv'
        String tstStr = '20180530_130419'
        TSuiteResultId tsri = TSuiteResultId.newInstance(
                new TSuiteName(tsnStr),
                new TExecutionProfile(tepStr),
                TSuiteTimestamp.newInstance(tstStr))
        TSuiteResult tsr = mri_.getTSuiteResult(tsri)
        when:
        Path path = tsr.getTExecutionProfileDirectory()
        then:
        path.getFileName().toString() == 'CURA_ProductionEnv'
        path.getParent().getFileName().toString() == 'main.TS1'
    }

    // helper methods
}
