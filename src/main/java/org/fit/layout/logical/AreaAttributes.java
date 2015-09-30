/**
 * AreaAttributes.java
 *
 * Created on 30. 9. 2015, 14:28:28 by burgetr
 */
package org.fit.layout.logical;

/**
 * This area represents the attributes added to the source areas during the logical structure construction.
 * @author burgetr
 */
public class AreaAttributes
{
    private double markedness;
    private LayoutAnalyzer.LayoutType layoutType;
    
    
    public AreaAttributes(double markedness, LayoutAnalyzer.LayoutType layoutType)
    {
        this.markedness = markedness;
        this.layoutType = layoutType;
    }

    public double getMarkedness()
    {
        return markedness;
    }

    public void setMarkedness(double markedness)
    {
        this.markedness = markedness;
    }

    public LayoutAnalyzer.LayoutType getLayoutType()
    {
        return layoutType;
    }

    public void setLayoutType(LayoutAnalyzer.LayoutType layoutType)
    {
        this.layoutType = layoutType;
    }
    
}
