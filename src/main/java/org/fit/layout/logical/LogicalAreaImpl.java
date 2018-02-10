/**
 * LogicalArea.java
 *
 * Created on 19. 3. 2015, 13:48:01 by burgetr
 */
package org.fit.layout.logical;

import org.fit.layout.impl.DefaultLogicalArea;
import org.fit.layout.model.Area;
import org.fit.layout.model.LogicalArea;

/**
 * 
 * @author burgetr
 */
public class LogicalAreaImpl extends DefaultLogicalArea
{
    /** Content subtree for collapsed areas */
    private LogicalAreaImpl contentTree;

    
    public LogicalAreaImpl(Area src)
    {
        super(src);
    }

    public LogicalAreaImpl getContentTree()
    {
        return contentTree;
    }

    public void setContentTree(LogicalAreaImpl contentTree)
    {
        this.contentTree = contentTree;
    }
    
    @Override
    public LogicalArea findArea(Area area)
    {
        if (getAreas().contains(area))
            return this; //in our area nodes
        else if (getContentTree() != null && getContentTree().findArea(area) != null)
            return this; //in our content tree
        else //in the subtree
        {
            for (int i = 0; i < getChildCount(); i++)
            {
                final LogicalArea ret = getChildAt(i).findArea(area);
                if (ret != null)
                    return ret;
            }
            return null;
        }
    }

}
