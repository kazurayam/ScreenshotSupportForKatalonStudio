package com.kazurayam.materials.stats

import java.nio.file.Path
import java.nio.file.Paths

import org.apache.commons.math3.distribution.TDistribution
import org.apache.commons.math3.stat.interval.ConfidenceInterval
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import com.kazurayam.materials.Helpers

/**
 * referrences:
 * - https://blog.apar.jp/data-analysis/4632/
 * - https://stackoverflow.com/questions/14758509/calculating-t-inverse-in-apache-commons
 *
 */
class MaterialStats {
    
    static Logger logger_ = LoggerFactory.getLogger(MaterialStats.class)
    
    static final MaterialStats NULL = new MaterialStats(null, new ArrayList<ImageDelta>())
    
    static final String CRITERIA_PERCENTAGE_FORMAT = '%1$.2f'
    
    // following default values may be overridden by StorageScanner.Options object
    static final double DEFAULT_FILTER_DATA_LESS_THAN = 1.00
    static final double DEFAULT_PROBABILITY = 0.95
    static final int DEFAULT_MAXIMUM_NUMBER_OF_IMAGEDELTAS = 10
    static final double SUGGESTED_SHIFT_CRITERIA_PERCENTAGE_BY = 0.0
    
    private Path path
    private List<ImageDelta> imageDeltaList
    private double filterDataLessThan
    private double probability
    private double shiftCriteriaPercentageBy
    
    /**
     * 
     * @param path
     * @param imageDeltaList
     */
    MaterialStats(Path path, List<ImageDelta> imageDeltaList) {
        this.path = path
        this.imageDeltaList = imageDeltaList
        this.filterDataLessThan = DEFAULT_FILTER_DATA_LESS_THAN
        this.probability = DEFAULT_PROBABILITY
        this.shiftCriteriaPercentageBy = this.SUGGESTED_SHIFT_CRITERIA_PERCENTAGE_BY
    }

    Path getPath() {
        return path
    }
    
    String getPathAsStringInUNIX() {
        return path.toString().replace('\\', '/')
    }
    
    void setFilterDataLessThan(double value) {
        this.filterDataLessThan = value
    }
    
    void setProbability(double value) {
        this.probability = value
    }
    
    void setShiftCriteriaPercentageBy(double value) {
        this.shiftCriteriaPercentageBy = value
    }
    
    double[] data() {
        List<Double> list = new ArrayList<Double>()
        for (ImageDelta delta : this.getImageDeltaList()) {
            if (delta.getD() >= this.filterDataLessThan) {
                list.add(delta.getD())
            }
        }
        double[] data = list.toArray(new double[list.size()])
        return data
    }
    
    /**
     * number of sample data
     * @return
     */
    int degree() {
        return this.data().length
    }
    
    double sum() {
        double sum = 0.0
        for (int i = 0; i < this.degree(); i++) {
            sum += this.data()[i]
        }
        return sum
    }
    
    double mean() {
        if (this.degree() > 0) {
            double mean = this.sum()/ this.degree()
            return mean
        } else {
            logger_.warn("mean() returned 0 because this.degree() returned 0")
            return 0
        }
    }
    
    double variance() {
        if (this.degree() > 0) {
            // ssum
            double ssum = 0.0
            for (int i = 0; i < this.degree(); i++) {
                ssum += Math.sqrt(Math.abs(this.data()[i] - this.mean()))
            }
            // variance
            double variance = ssum / this.degree()
            return variance
        } else {
            logger_.warn("variance() returned 0 because this.degree() returned 0")
            return 0
        }
    }
    
    double standardDeviation() {
        return Math.sqrt(this.variance())
    }
    
    /**
     * @return calculate t-inverse with this.degree() degrees of FREEDOM %
     */
    double tDistribution() {
        if (this.degree() > 1) {
            TDistribution tdist = new org.apache.commons.math3.distribution.TDistribution(this.degree() - 1)
            return tdist.inverseCumulativeProbability(this.probability)
        } else {
            logger_.warn("tDistribution() returned 0 because this.degree() was ${this.degree()}")
            return 0
        }
    }
    
    ConfidenceInterval getConfidenceInterval() {
        if (this.degree() > 0) {
            double lowerBound = this.mean() - this.tDistribution() * this.standardDeviation() / Math.sqrt(this.degree())
            double upperBound = this.mean() + this.tDistribution() * this.standardDeviation() / Math.sqrt(this.degree())
            double confidenceLevel = this.probability
            return new ConfidenceInterval(lowerBound, upperBound, confidenceLevel)
        } else {
            logger_.warn("getConfidenceInterval() returned meaningless result because this.degree() returned 0")
            return new ConfidenceInterval(0.0, 100.0, 0.999)
        }
    }
    
    /**
     * 
     * @return ConfidenceInterval.upperBound + shiftCriteriaPercentage
     */
    double getCriteriaPercentage() {
        if (this.degree() > 0) {
            return this.getConfidenceInterval().getUpperBound() + this.shiftCriteriaPercentageBy
        } else {
            return this.shiftCriteriaPercentageBy
        }
    }
   
    String getCriteriaPercentageAsString(String fmt = CRITERIA_PERCENTAGE_FORMAT) {
        return String.format(CRITERIA_PERCENTAGE_FORMAT, this.getCriteriaPercentage())
    }
    
    List<ImageDelta> getImageDeltaList() {
        return imageDeltaList
    }
    
    /**
     * FIXME: too simple? should consider data?
     */
    @Override
    boolean equals(Object obj) {
        if (!(obj instanceof MaterialStats))
            return false
        MaterialStats other = (MaterialStats)obj
        return this.getPath().equals(other.getPath())
    }
    
    @Override
    public int hashCode() {
        return this.getPath().hashCode()
    }
    
    @Override
    String toString() {
        return this.toJson()
    }

    String toJson() {
        StringBuilder sb = new StringBuilder()
        sb.append("{")
        sb.append("\"path\":")
        sb.append("\"${Helpers.escapeAsJsonText(this.getPathAsStringInUNIX())}\",")
        sb.append("\"degree\":${this.degree().toString()},")
        sb.append("\"sum\":${this.sum()},")
        sb.append("\"mean\":${this.mean()},")
        sb.append("\"variance\":${this.variance()},")
        sb.append("\"standardDeviation\":${this.standardDeviation()},")
        sb.append("\"tDistribution\":${this.tDistribution()},")
        sb.append("\"confidenceInterval\":{")
        sb.append("\"lowerBound\":")
        sb.append(this.getConfidenceInterval().getLowerBound())
        sb.append(",\"upperBound\":")
        sb.append(this.getConfidenceInterval().getUpperBound())
        sb.append("},")
        sb.append("\"criteriaPercentage\":")
        sb.append(this.getCriteriaPercentageAsString())
        sb.append(",")
        sb.append("\"data\":${this.data().toString()},")
        sb.append("\"imageDeltaList\":[")
        int count = 0
        for (ImageDelta id : this.getImageDeltaList()) {
            if (count > 0) {
                sb.append(",")
            }
            sb.append(id.toJson())
            count += 1
        }
        sb.append("]")
        sb.append("}")
        return sb.toString()
    }
    
    /**
     * <PRE>
     * {
                    "path": "main.TC_47News.visitSite/47NEWS_TOP.png",
                    "degree": 5,
                    "sum": 68.17,
                    "mean": 13.634,
                    "variance": 2.6882191428856,
                    "standardDeviation": 1.6395789529283424,
                    "tDistribution": 2.1318467859510317,
                    "confidenceInterval": {
                        "lowerBound": 12.070840401864046,
                        "upperBound": 15.197159598135954
                    },
                    "criteriaPercentage": 40.20,
                    "data": [
                        16.86,
                        4.53,
                        2.83,
                        27.85,
                        16.1
                    ],
                    "imageDeltaList": [
                        // list of ImageDelta objects
                    ]
                }
     * </PRE>
     * @param json
     * @return
     */
    static MaterialStats fromJsonObject(Object jsonObject) {
        Objects.requireNonNull(jsonObject, "jsonObject must not be null")
        if (jsonObject instanceof Map) {
            Map materialStatsJsonObject = (Map)jsonObject
            logger_.debug("#deserialize json=${materialStatsJsonObject.toString()}")
            if (materialStatsJsonObject.path == null) {
                throw new IllegalArgumentException("json.path must not be null")
            }
            if (materialStatsJsonObject.imageDeltaList == null) {
                throw new IllegalArgumentException("json.imageDeltaList must not be null")
            }
            List<ImageDelta> imageDeltas = new ArrayList<ImageDelta>()
            for (Map imageDeltaJsonObj : (List)materialStatsJsonObject.imageDeltaList) {
                ImageDelta deserialized = ImageDelta.fromJsonObject(imageDeltaJsonObj)
                imageDeltas.add(deserialized)
            }
            MaterialStats materialStats = new MaterialStats(Paths.get(materialStatsJsonObject.path), imageDeltas)
            if (materialStatsJsonObject.filterDataLessThan != null) {
                materialStats.setFilterDataLessThan(materialStatsJsonObject.filterDataLessThan)
            }
            if (materialStatsJsonObject.probability != null) {
                materialStats.setProbability(materialStatsJsonObject.probability)
            }
            if (materialStatsJsonObject.shiftCriteriaPercentageBy != null) {
                materialStats.setShiftCriteriaPercentageBy(materialStatsJsonObject.shiftCriteriaPercentageBy)
            }
            return materialStats
        } else {
                throw new IllegalArgumentException("jsonObject should be an instance of Map but was ${jsonObject.class.getName()}")
        }
    }
}
