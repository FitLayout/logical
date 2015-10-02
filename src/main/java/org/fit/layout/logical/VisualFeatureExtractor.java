/**
 * FeatureAnalyzer.java
 *
 * Created on 6.5.2011, 14:48:51 by burgetr
 */
package org.fit.layout.logical;

import org.fit.layout.classify.articles.ArticleFeatureExtractor;

/**
 * The feature extractor used for computing the markedness.
 * 
 * @author burgetr
 */
public class VisualFeatureExtractor extends ArticleFeatureExtractor
{
    
    public VisualFeatureExtractor()
    {
        super();
        //setWeights(new double[]{1000.0, 2.0, 0.5, 0.0, 0.0, 1.0, 0.5, 100.0});
        setWeights(new double[]{1000.0, 2.0, 0.5, 0.0, 0.0, 0.0, 0.0, 100.0});
    }
    
}
