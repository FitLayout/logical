/**
 * LogicalTreeBuilder.java
 *
 * Created on 19. 3. 2015, 13:54:48 by burgetr
 */
package org.fit.layout.logical;

import org.fit.layout.impl.BaseLogicalTreeProvider;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.LogicalArea;
import org.fit.layout.model.LogicalAreaTree;
import org.fit.layout.model.Rectangular;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 * @author burgetr
 */
public class LogicalTreeBuilder extends BaseLogicalTreeProvider
{
    private static Logger log = LoggerFactory.getLogger(LogicalTreeBuilder.class);
    
    protected VisualFeatureExtractor fa;
    protected LayoutAnalyzer la;
    
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
        PresentationLogicalTree ltree = new PresentationLogicalTree(areaTree);
        
        fa = new VisualFeatureExtractor();
        fa.setTree(areaTree.getRoot());
        la = new LayoutAnalyzer(areaTree);
        computeAreaMarkedness(areaTree.getRoot());
        final LogicalArea root = recursiveCreateLogicalStructure(areaTree.getRoot());
        ltree.setRoot(root);
        return ltree;
    }
    
    //====================================================================================
    
    /**
     * Creates the logical structure tree from the source area tree recursively.
     * @param src the area tree root node
     * @return the root of the new tree
     */
    protected LogicalAreaImpl recursiveCreateLogicalStructure(Area src)
    {
        if (src.getChildCount() == 0)
            return new LogicalAreaImpl(src);
        else
        {
            //System.out.println("Processing: " + src);
            LogicalAreaImpl newroot = new LogicalAreaImpl(src);
            LogicalAreaImpl firstnode = recursiveCreateLogicalStructure(src.getChildArea(0));
            newroot.add(firstnode);
            
            TreeCreationStatus curstat = new TreeCreationStatus();
            curstat.node = firstnode;
            curstat.level = getMarkedness(curstat.node); //current markedness level
            curstat.pos = curstat.node.getFirstArea().getBounds();
            
            for (int i = 1; i < src.getChildCount(); i++)
            {
                Area child = src.getChildArea(i);
                
                if (!child.isSeparator()) //skip areas used for separation only
                {
                    TreeCreationStatus substat = new TreeCreationStatus();
                    substat.node = recursiveCreateLogicalStructure(child);
                    substat.level = getMarkedness(substat.node);
                    substat.pos = substat.node.getFirstArea().getBounds();
                    
                    //find the appropriate parent
                    LogicalAreaImpl candParent = findParentForNode(curstat, substat, newroot);
                    candParent.add(substat.node);
                    curstat.replaceWith(substat);
                }
            }
            
            //collapse the logical node if it is too simple (no internal structure)
            if (newroot.getLeafCount() == 1)
            {
                newroot.setContentTree((LogicalAreaImpl) newroot.getChildArea(0));
                newroot.removeAllChildren();
            }
            
            return newroot;
        }
    }

    /**
     * Locates an appropriate parent in the current tree for the new node
     * @param curstat Current position in the tree
     * @param substat The new candidate position
     * @param root the root node of the current logical subtree used as "reset" parent in case of detected flow break
     * @return the parent node where the candidate should be added to
     */
    protected LogicalAreaImpl findParentForNode(TreeCreationStatus curstat, TreeCreationStatus substat, LogicalAreaImpl root)
    {
        //TODO: should the children be sorted in any way?
        //TODO: better flow break detection?
        if (substat.pos.getY2() < curstat.pos.getY1() - 10)  //flow break - go to the parent, 10 pixels tollerance for now
        {
            System.out.println("break: 1=" + curstat.node + " 2=" + substat.node);
            System.out.println("  pos: 1=" + curstat.pos + " 2=" + substat.pos);
            return root;
        }
        else //normal flow, compare the levels
        {
            LogicalAreaImpl candParent;
            
            LogicalAreaImpl cparent = (LogicalAreaImpl) curstat.node.getParentArea();
            double plevel = getMarkedness(cparent);
            double pcur = Math.abs(substat.level - curstat.level); //price for going up (to the parent)
            double ppar = Math.abs(substat.level - plevel); //price of remaining here or going down
            
            if (pcur <= ppar) //remain here or go down
            {
                int c = compareMarkedness(substat.level, curstat.level);
                if (c < 0) //substat.level < clevel
                {
                    candParent = curstat.node;
                }
                else //if (c >= 0) //substat.level == clevel
                {
                    LogicalAreaImpl parent = (LogicalAreaImpl) curstat.node.getParentArea();
                    if (parent != null)
                    {
                        candParent = parent;
                    }
                    else
                    {
                        System.err.println("ERROR: LogicalTree: no parent");
                        candParent = curstat.node;
                    }
                }
            }
            else //parent is closer, search for the topmost applicable parent
            {
                LogicalAreaImpl parent = curstat.node;
                while (compareMarkedness(getMarkedness(parent), substat.level) <= 0 && parent.getParentArea() != null)
                {
                    parent = (LogicalAreaImpl) parent.getParentArea();
                }
                candParent = parent;
            }
            
            return candParent;
        }
    }
    
    protected double getMarkedness(LogicalAreaImpl node)
    {
        if (node == null)
            return 0;
        else
        {
            final AreaAttributes attr = node.getFirstArea().getUserAttribute(AreaAttributes.class);
            if (attr != null)
                return attr.getMarkedness();
            else
                return 0;
        }
    }
    
    /**
     * @return 0 when equal (in the given threshold), 1 when m2>m1, -1 when m2<m1
     */
    protected int compareMarkedness(double m1, double m2)
    {
        double dif = m2 - m1;
        if (Math.abs(dif) < VisualFeatureExtractor.MIN_MARKEDNESS_DIFFERENCE)
            return 0;
        else
            return (dif > 0) ? -1 : 1;
    }
    
    /**
     * Recomputes the markedness in all the nodes of an area tree.
     * @param root the root of the tree to be recomputed
     */
    protected void computeAreaMarkedness(Area root)
    {
        root.addUserAttribute(new AreaAttributes(fa.getMarkedness(root), la.detectLayoutType(root)));
        //root.addAttribute(new AreaAttributes(fa.getMarkedness(root), LayoutAnalyzer.LayoutType.NORMAL));
        for (int i = 0; i < root.getChildCount(); i++)
            computeAreaMarkedness(root.getChildArea(i));
    }
    
    
    //====================================================================================
    
    protected class TreeCreationStatus
    {
        public LogicalAreaImpl node;
        public double level;
        public Rectangular pos;
        
        public void replaceWith(TreeCreationStatus other)
        {
            node = other.node;
            level = other.level;
            pos = other.pos;
        }
    }

}
