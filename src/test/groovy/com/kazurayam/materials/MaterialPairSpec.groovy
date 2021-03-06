package com.kazurayam.materials

import com.kazurayam.materials.repository.TreeTrunkScanner

import java.awt.image.BufferedImage
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.impl.MaterialPairImpl

import com.kazurayam.materials.repository.RepositoryRoot

import spock.lang.Specification

class MaterialPairSpec extends Specification {

    static Logger logger_ = LoggerFactory.getLogger(MaterialPairSpec.class)

    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static RepositoryRoot repoRoot_
    private Material expectedMaterial_
    private Material actualMaterial_

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/testOutput/${Helpers.getClassShortName(MaterialPairSpec.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        Path materialsDir = workdir_.resolve('Materials')
        TreeTrunkScanner scanner = new TreeTrunkScanner(materialsDir)
        scanner.scan()
        repoRoot_ = scanner.getRepositoryRoot()
    }
    def setup() {
        TSuiteName tsn = new TSuiteName('Test Suites/main/TS1')
        TExecutionProfile tep = new TExecutionProfile("CURA_ProductionEnv")
        TSuiteResult expectedTsr = repoRoot_.getTSuiteResult(tsn, tep,
                TSuiteTimestamp.newInstance('20180530_130419'))
        TCaseResult expectedTcr = expectedTsr.getTCaseResult(new TCaseName('main.TC1'))
        expectedMaterial_ = expectedTcr.getMaterial(Paths.get('http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
        //
        TSuiteResult actualTsr = repoRoot_.getTSuiteResult(tsn, tep,
                TSuiteTimestamp.newInstance('20180530_130604'))
        TCaseResult actualTcr = expectedTsr.getTCaseResult(new TCaseName('main.TC1'))
        actualMaterial_   = expectedTcr.getMaterial(Paths.get('http%3A%2F%2Fdemoaut.katalon.com%2F.png'))
    }
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testGetLeft() {
        setup:
        MaterialPair mp = MaterialPairImpl.newInstance().setLeft(expectedMaterial_).setRight(actualMaterial_)
        when:
        Material left = mp.getLeft()
        then:
        left == expectedMaterial_
    }

    def testGetRight() {
        setup:
        MaterialPair mp = MaterialPairImpl.newInstance().setLeft(expectedMaterial_).setRight(actualMaterial_)
        when:
        Material right = mp.getRight()
        then:
        right == actualMaterial_
    }

    def testGetExpected() {
        setup:
        MaterialPair mp = MaterialPairImpl.newInstance().setExpected(expectedMaterial_).setActual(actualMaterial_)
        when:
        Material expected = mp.getExpected()
        then:
        expected == expectedMaterial_
    }

    def testGetActual() {
        setup:
        MaterialPair mp = MaterialPairImpl.newInstance().setExpected(expectedMaterial_).setActual(actualMaterial_)
        when:
        Material actual = mp.getActual()
        then:
        actual == actualMaterial_
    }
    
    def testGetExpectedBufferedImage() {
        setup:
        MaterialPair mp = MaterialPairImpl.newInstance().setExpected(expectedMaterial_).setActual(actualMaterial_)
        when:
        BufferedImage buffImg = mp.getExpectedBufferedImage()
        then:
        buffImg != null
    }
    
    def testGetActualBufferedImage() {
        setup:
        MaterialPair mp = MaterialPairImpl.newInstance().setExpected(expectedMaterial_).setActual(actualMaterial_)
        when:
        BufferedImage buffImg = mp.getActualBufferedImage()
        then:
        buffImg != null
    }
    
    def test_hasExpected_truthy() {
        when:
        MaterialPair mp = MaterialPairImpl.newInstance().setExpected(expectedMaterial_).setActual(actualMaterial_)
        then:
        mp.hasExpected()
    }
    
    def test_hasExpected_falsy() {
        when:
        MaterialPair mp = MaterialPairImpl.newInstance()
        then:
        ! mp.hasExpected()
    }
    
    def test_hasActual_truthy() {
        when:
        MaterialPair mp = MaterialPairImpl.newInstance().setExpected(expectedMaterial_).setActual(actualMaterial_)
        then:
        mp.hasActual()
    }
    
    def test_hasActual_falsy() {
        when:
        MaterialPair mp = MaterialPairImpl.newInstance()
        then:
        ! mp.hasActual()
    }

}
