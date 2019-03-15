package com.kazurayam.materials.imagedifference

import com.kazurayam.materials.Material
import com.kazurayam.materials.TCaseName

interface ImageCollectionProcessingContentHandler {

    void endImageCollection(TCaseName tCaseName) throws ImageDifferenceException
    
    void endMaterialPair(EvaluationResult evalResult) throws ImageDifferenceException
    
    void startImageCollection(TCaseName tCaseName) throws ImageDifferenceException
    
    EvaluationResult startMaterialPair( TCaseName tCaseName,
                                        Material expectedMaterial,
                                        Material actualMaterial,
                                        double criteriaPercentage) throws ImageDifferenceException
}
