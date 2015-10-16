/**
 * LayoutSplitOperator.java
 *
 * Created on 16. 10. 2015, 11:29:32 by burgetr
 */
package org.fit.layout.logical.op;

import java.util.Vector;

import org.fit.layout.impl.BaseOperator;
import org.fit.layout.logical.LayoutAnalyzer;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.Rectangular;

/**
 * This operator splits the visual areas that contain different types of layout and creates
 * smaller areas of homogeneous layout types. This means that the detected table and lists
 * will create separate visual areas.
 * 
 * @author burgetr
 */
public class LayoutSplitOperator extends BaseOperator
{

    protected final String[] paramNames = { };
    protected final ValueType[] paramTypes = { };
    
    private LayoutAnalyzer la;
    
    public LayoutSplitOperator()
    {
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Logical.LayoutSplit";
    }
    
    @Override
    public String getName()
    {
        return "Split areas by layout";
    }

    @Override
    public String getDescription()
    {
        return "splits the visual areas that contain different types of layout and creates smaller"
                + "areas of homogeneous layout types. This means that the detected table and lists"
                + " will create separate visual areas.";
    }

    @Override
    public String[] getParamNames()
    {
        return paramNames;
    }

    @Override
    public ValueType[] getParamTypes()
    {
        return paramTypes;
    }
    
    //==============================================================================

    @Override
    public void apply(AreaTree atree)
    {
        apply(atree, atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        la = new LayoutAnalyzer(atree);
        recursiveFindTables((Area) root);
    }
    
    //==============================================================================
    
    private void recursiveFindTables(Area root)
    {
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveFindTables((Area) root.getChildArea(i));
        findTables(root);
    }

    private void findTables(Area root)
    {
        //find table bounds
        Vector<Integer> breaks = new Vector<Integer>();
        for (int i = 0; i < root.getChildCount(); i++)
        {
            int tend = la.findTableEnd(root, root.getChildAreas(), i);
            if (tend > i + 1) //found a table
            {
                //mark the beginning and end
                if (i > 0)
                    breaks.add(i);
                if (tend + 1 < root.getChildCount())
                    breaks.add(tend + 1);
                //skip the table
                i = tend;
            }
            else
            {
                int lend = la.findListEnd(root, root.getChildAreas(), i);
                if (lend > i + 2) //found a list
                {
                    //mark the beginning and end
                    if (i > 0)
                        breaks.add(i);
                    if (lend + 1 < root.getChildCount())
                        breaks.add(lend + 1);
                    //skip the list
                    i = lend;
                }
            }
        }
        //split the area with breaks
        if (!breaks.isEmpty())
        {
            System.out.println(root + " found " + breaks.size() + " breaks");
            Area[][] regions = new Area[breaks.size() + 1][];
            int strt = 0;
            int i = 0;
            for (int end : breaks)
            {
                regions[i] = new Area[end - strt];
                for (int j = 0; j < end - strt; j++)
                    regions[i][j] = root.getChildArea(strt + j);
                strt = end;
                i++;
            }
            int end = root.getChildCount();
            regions[i] = new Area[end - strt];
            for (int j = 0; j < end - strt; j++)
                regions[i][j] = root.getChildArea(strt + j);

            for (Area[] sub : regions)
            {
                createSubArea(root, sub);
            }
        }
    }
    
    private Area createSubArea(Area root, Area[] sub)
    {
        if (sub.length > 1)
        {
            Vector<Area> region = new Vector<Area>(sub.length);
            Rectangular bounds = null;
            
            for (Area area : sub)
            {
                region.add(area);
                if (bounds == null)
                    bounds = new Rectangular(area.getTopology().getPosition());
                else
                    bounds.expandToEnclose(area.getTopology().getPosition());
            }
            
            Area grp = root.createSuperArea(bounds, region, "<areaT>");
            return grp;
        }
        else
            return null;
    }
    
    
}
