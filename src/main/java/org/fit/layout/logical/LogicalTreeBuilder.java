/**
 * LogicalTreeBuilder.java
 *
 * Created on 19. 3. 2015, 13:54:48 by burgetr
 */
package org.fit.layout.logical;

import org.fit.layout.impl.BaseLogicalTreeProvider;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.LogicalAreaTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 * @author burgetr
 */
public class LogicalTreeBuilder extends BaseLogicalTreeProvider
{
    private static Logger log = LoggerFactory.getLogger(LogicalTreeBuilder.class);
    
    @Override
    public String getId()
    {
        return "FitLayout.Logical";
    }

    @Override
    public String getName()
    {
        return "FitLayout presentation-based logical tree bulder";
    }

    @Override
    public String getDescription()
    {
        return "Logical structure builder based on visual presentation of the content elements";
    }

    @Override
    public String[] getParamNames()
    {
        return new String[0];
    }

    @Override
    public ValueType[] getParamTypes()
    {
        return new ValueType[0];
    }

    //====================================================================================
    
    @Override
    public LogicalAreaTree createLogicalTree(AreaTree areaTree)
    {
        LogicalAreaTree ltree = new PresentationLogicalTree(areaTree);
        
        
        return ltree;
    }
    
}
