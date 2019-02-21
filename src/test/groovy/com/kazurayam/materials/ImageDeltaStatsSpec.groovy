package com.kazurayam.materials

import java.nio.file.Path
import java.nio.file.Paths

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.stats.StorageScanner

import groovy.json.JsonOutput
import spock.lang.Specification

class ImageDeltaStatsSpec extends Specification {
    
    static Logger logger_ = LoggerFactory.getLogger(ImageDeltaStatsSpec.class)
    
    // fields
    private static Path workdir_
    private static Path fixture_ = Paths.get("./src/test/fixture")
    private static MaterialStorage ms_
    
    // fixture methods
    def setupSpec() {
        workdir_ = Paths.get("./build/tmp/${Helpers.getClassShortName(ImageDeltaStats.class)}")
        if (!workdir_.toFile().exists()) {
            workdir_.toFile().mkdirs()
        }
        Helpers.copyDirectory(fixture_, workdir_)
        Path storage = workdir_.resolve("Storage")
        ms_ = MaterialStorageFactory.createInstance(storage)
    }
    def setup() {}
    def cleanup() {}
    def cleanupSpec() {}

    /**
     * 
     */
    def testGetDefaultCriteriaPercentage_customizing() {
        setup:
        double value = 0.10
        StorageScanner.Options options = new StorageScanner.Options.Builder().
                                            defaultCriteriaPercentage(value).
                                            build()
        StorageScanner scanner = new StorageScanner(ms_, options)
        when:
        ImageDeltaStats ids = scanner.scan(new TSuiteName("47News_chronos_capture"))
        then:
        ids.getDefaultCriteriaPercentage() == value
    }
    
    /**
     * 
     */
    def testCriteriaPercentage_customizingFilterDataLessThan() {
        setup:
        StorageScanner.Options options = new StorageScanner.Options.Builder().
                                            filterDataLessThan(0.0).  // LOOK HERE
                                            build()
        StorageScanner scanner = new StorageScanner(ms_, options)
        when:
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats ids = scanner.scan(tsn)
        double criteriaPercentage = ids.criteriaPercentage(tsn, Paths.get('main.TC_47News.visitSite/47NEWS_TOP.png'))
        then:
        // criteriaPercentage == 12.767696022300328 
        12.0 < criteriaPercentage
        criteriaPercentage < 13.0
    }
    
    /**
     * 
     */
    def testCriteriaPercentage_customizingProbability() {
        setup:
        StorageScanner.Options options = new StorageScanner.Options.Builder().
                                            probability(0.75).  // LOOK HERE
                                            build()
        StorageScanner scanner = new StorageScanner(ms_, options)
        when:
        TSuiteName tsn = new TSuiteName("47News_chronos_capture")
        ImageDeltaStats ids = scanner.scan(tsn)
        double criteriaPercentage = ids.criteriaPercentage(tsn, Paths.get('main.TC_47News.visitSite/47NEWS_TOP.png'))
        then:
        // criteriaPercentage == 15.197159598135954
        15.00 < criteriaPercentage
        criteriaPercentage < 15.20
    }
    
    /**
     * 
     */
    def testToString() {
        setup:
        StorageScanner scanner = new StorageScanner(ms_)
        ImageDeltaStats ids = scanner.scan(new TSuiteName("47News_chronos_capture"))
        when:
        String s = ImageDeltaStats.ZERO.toString()
        String pp = JsonOutput.prettyPrint(s)
        logger_.debug(ImageDeltaStats.getClass().getName() + ".ZERO:\n${pp}")
        then:
        s.contains("0.0")
    }
}
