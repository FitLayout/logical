/**
 * LogicalArea.java
 *
 * Created on 19. 3. 2015, 13:48:01 by burgetr
 */
package org.fit.layout.logical;

import org.fit.layout.impl.DefaultLogicalArea;
import org.fit.layout.model.Area;

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
    
}
