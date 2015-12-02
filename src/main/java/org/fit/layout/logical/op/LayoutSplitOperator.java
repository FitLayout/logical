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
        Vector<StructInfo> parts = new Vector<StructInfo>();
        int lastpos = -1;
        int i;
        for (i = 0; i < root.getChildCount(); i++)
        {
            int tend = la.findTableEnd(root, root.getChildAreas(), i);
            if (tend > i + 1) //found a table
            {
                System.out.println("found a table " + (i) + ".." + tend);
                //close previous sequence (if any)
                if (i > lastpos + 1)
                    parts.add(new StructInfo(lastpos + 1, i, 'N'));
                //mark the beginning and end
                parts.add(new StructInfo(i, tend + 1, 'T'));
                //skip the table
                i = tend;
                lastpos = i;
            }
            else
            {
                int lend = la.findListEnd(root, root.getChildAreas(), i);
                if (lend > i + 2) //found a list
                {
                    //close previous sequence (if any)
                    if (i > lastpos + 1)
                        parts.add(new StructInfo(lastpos + 1, i, 'N'));
                    //mark the beginning and end
                    parts.add(new StructInfo(i, lend + 1, 'L'));
                    //skip the list
                    i = lend;
                    lastpos = i;
                }
            }
        }
        //close last sequence (if any)
        if (i > lastpos + 1)
            parts.add(new StructInfo(lastpos + 1, i, 'N'));
        //split the area with breaks
        if (parts.size() > 1)
        {
            System.out.println(root + " found " + parts.size() + " structures");
            //collect areas
            for (StructInfo part : parts)
            {
                for (int ai = 0; ai < part.length(); ai++)
                    part.areas[ai] = root.getChildArea(ai + part.start);  
            }
            //create super areas
            for (StructInfo part : parts)
            {
                createSubArea(root, part.areas, part.type);
            }
        }
    }
    
    private Area createSubArea(Area root, Area[] sub, char type)
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
            
            Area grp = root.createSuperArea(bounds, region, "<area" + type +">");
            return grp;
        }
        else
            return null;
    }
    
    class StructInfo
    {
        public int start;
        public int end;
        public char type;
        public Area[] areas;
        
        public StructInfo(int start, int end, char type)
        {
            this.start = start;
            this.end = end;
            this.type = type;
            areas = new Area[end - start];
        }
        
        public int length()
        {
            return end - start;
        }
        
        @Override
        public String toString()
        {
            return "<" + start + ", " + end  + " " + type + ">";
        }
    }
    
}
