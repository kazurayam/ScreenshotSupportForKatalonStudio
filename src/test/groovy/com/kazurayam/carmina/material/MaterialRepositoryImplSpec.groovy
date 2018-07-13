package com.kazurayam.carmina.material

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import spock.lang.Specification

//@Ignore
class MaterialRepositoryImplSpec extends Specification {

    // fields
    static Logger logger_ = LoggerFactory.getLogger(MaterialRepositoryImplSpec.class)

    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture/Materials")
    private static String classShortName_ = Helpers.getClassShortName(MaterialRepositoryImplSpec.class)

    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${classShortName_}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    // feature methods
    def testGetBaseDir() {
        setup:
        Path casedir = workdir_.resolve('testGetBaseDir')
        Helpers.copyDirectory(fixture_, casedir)
        when:
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        then:
        mri.getBaseDir() == casedir
    }


    def testResolveMaterial() {
        setup:
        Path casedir = workdir_.resolve('testResolveMaterial')
        Helpers.copyDirectory(fixture_, casedir)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        mri.putCurrentTestSuite('TS1', '20180530_130604')
        when:
        Path p = mri.resolveMaterial(
            new TCaseName('TC1'),
            new URL('http://demoaut.katalon.com/'),
            Suffix.NULL,
            FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "build/tmp/${classShortName_}/testResolveMaterial/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
    }

    def testResolveMaterialWithSuffix() {
        setup:
        Path casedir = workdir_.resolve('testResolveMaterialWithSuffix')
        Helpers.copyDirectory(fixture_, casedir)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        mri.putCurrentTestSuite('TS1', '20180530_130604')
        when:
        Path p = mri.resolveMaterial(
            new TCaseName('TC1'),
            new URL('http://demoaut.katalon.com/'),
            new Suffix(1),
            FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "build/tmp/${classShortName_}/testResolveMaterialWithSuffix/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png"
    }

    def testResolveMaterial_new() {
        setup:
        Path casedir = workdir_.resolve('testResolveMaterial_new')
        Helpers.copyDirectory(fixture_, casedir)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        mri.putCurrentTestSuite('TS3', '20180614_152000')
        when:
        Path p = mri.resolveMaterial(
            new TCaseName('TC1'),
            new URL('http://demoaut.katalon.com/'),
            Suffix.NULL,
            FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "build/tmp/${classShortName_}/testResolveMaterial_new/TS3/20180614_152000/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
        Files.exists(p.getParent())
    }

    def testResolveMaterialWithSuffix_new() {
        setup:
        Path casedir = workdir_.resolve('testResolveMaterialWithSuffix_new')
        Helpers.copyDirectory(fixture_, casedir)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        mri.putCurrentTestSuite('TS3', '20180614_152000')
        when:
        Path p = mri.resolveMaterial(
            new TCaseName('TC1'),
            new URL('http://demoaut.katalon.com/'),
            new Suffix(1),
            FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') ==
            "build/tmp/${classShortName_}/testResolveMaterialWithSuffix_new/TS3/20180614_152000/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png"
        Files.exists(p.getParent())
    }

    def testResolvePngFilePath() {
        setup:
        Path casedir = workdir_.resolve('testResolvePngFilePath')
        Helpers.copyDirectory(fixture_, casedir)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        mri.putCurrentTestSuite('TS1', '20180530_130604')
        when:
        Path p = mri.resolveMaterial('TC1', 'http://demoaut.katalon.com/', FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') == "build/tmp/${classShortName_}/testResolvePngFilePath/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F.png"
    }

    def testResolvePngFilePathWithSuffix() {
        setup:
        Path casedir = workdir_.resolve('testResolveMaterialFilePathWithSuffix')
        Helpers.copyDirectory(fixture_, casedir)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        mri.putCurrentTestSuite('TS1', '20180530_130604')
        when:
        Path p = mri.resolveMaterial('TC1', 'http://demoaut.katalon.com/', 1, FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') == "build/tmp/${classShortName_}/testResolveMaterialFilePathWithSuffix/TS1/20180530_130604/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png"
    }

    def testResolvePngFilePathBySuitelessTimeless() {
        setup:
        Path casedir = workdir_.resolve('testResolvePngFilePathBySuitelessTimeless')
        Helpers.copyDirectory(fixture_, casedir)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        mri.putCurrentTestSuite(TSuiteName.SUITELESS, TSuiteTimestamp.TIMELESS)
        when:
        Path p = mri.resolveMaterial('TC1', 'http://demoaut.katalon.com/', 1, FileType.PNG)
        then:
        p != null
        p.toString().replace('\\', '/') == "build/tmp/${classShortName_}/testResolvePngFilePathBySuitelessTimeless/_/_/TC1/http%3A%2F%2Fdemoaut.katalon.com%2F(1).png"
    }

    def testToJson() {
        setup:
        Path casedir = workdir_.resolve('testToJson')
        Helpers.copyDirectory(fixture_, casedir)
        MaterialRepositoryImpl mri = new MaterialRepositoryImpl(casedir)
        mri.putCurrentTestSuite('TS1')
        when:
        def str = mri.toJson()
        then:
        str != null
        str.contains('{"MaterialRepositoryImpl":{')
        str.contains(Helpers.escapeAsJsonText(casedir.toString()))
        str.contains('}}')
    }



    // helper methods

}
