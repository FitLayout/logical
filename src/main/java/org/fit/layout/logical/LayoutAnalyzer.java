/**
 * LayoutAnalyzer.java
 *
 * Created on 29. 11. 2013, 13:48:02 by burgetr
 */
package org.fit.layout.logical;

import java.util.List;

import org.fit.layout.classify.NodeStyle;
import org.fit.layout.model.Area;
import org.fit.layout.model.AreaTopology;
import org.fit.layout.model.AreaTree;
import org.fit.layout.model.Rectangular;

/**
 * 
 * @author burgetr
 */
public class LayoutAnalyzer
{
    protected AreaTree atree;

    public LayoutAnalyzer(AreaTree atree)
    {
        this.atree = atree;
    }
    
    /**
     * Detects the layout type of the given area.
     * @param area
     * @return
     */
    public LayoutType detectLayoutType(Area area)
    {
        if (area.getChildCount() > 1)
        {
            if (isTable(area, area.getChildAreas(), 0))
                return LayoutType.TABLE;
            else if (isList(area, area.getChildAreas(), 0))
                return LayoutType.LIST;
            else
                return LayoutType.NORMAL;
        }
        else
            return LayoutType.NORMAL;
    }
    
    //==== TABLES ========================================================================
    
    /**
     * If a table starts at the specified child index of the area, tries to detect
     * the end of the table.
     * @param area The area where the table is placed.
     * @param nodes The child areas of the area that should be taken into account.
     * @param startchild The first child index that forms the table.
     * @return the last child that forms the table.
     */
    public int findTableEnd(Area area, List<Area> nodes, int startchild)
    {
        TableInfo stat = collectTableStats(area, nodes, startchild, true);
        if (stat.lastgood == startchild)
        {
            return startchild; //no table found
        }
        else if (stat.lastgood == area.getChildCount() - 1) //the whole area belongs to the table
        {
            return stat.lastgood; //do not perform more checks
        }
        else //not the whole area
        {
            //find the last child in the table so that the table is rectangular
            int failY = area.getTopology().getPosition(area.getChildArea(stat.lastgood + 1)).getY1(); //Y coordinate of the first failed node
            for (int i = 0; i < stat.lastgood; i++)
            {
                if (area.getTopology().getPosition(area.getChildArea(i)).getY1() >= failY)
                    return i-1; //last area above the failY
            }
            return stat.lastgood; //all areas above the failY
        }
    }
    
    protected boolean isTable(Area area, List<Area> nodes, int startchild)
    {
        AreaTopology grid = area.getTopology();
        if (grid.getTopologyWidth() >= 2 && grid.getTopologyHeight() >= 2)
        {
            TableInfo stat = collectTableStats(area, nodes, startchild, false);
            if (stat.isValidTable())
            {
                int[] pos = stat.findTableGridPositions();
                System.out.print(area + " positions :");
                for (int i : pos)
                    System.out.print(" " + i);
                System.out.println();
            }
            return stat.isValidTable();
        }
        else
            return false;
    }
    
    protected TableInfo collectTableStats(Area parent, List<Area> nodes, int startchild, boolean check)
    {
        TableInfo stat = new TableInfo(parent.getTopology().getTopologyWidth(), parent.getTopology().getTopologyHeight());
        stat.lastgood = startchild; 
       /* if (nodes.get(startchild).getId() == 1327)
            System.out.println("jo!");
        else
            return stat;*/
        
        //gather the statistics about the grid positions
        for (int cur = startchild; cur < nodes.size(); cur++)
        {
            System.out.println("Trying " + cur + " " + nodes.get(cur));
            Area area = nodes.get(cur);
            boolean fits = stat.putToGrid(area);
            if (check)
            {
                if (fits)
                {
                    System.out.println("fits");
                    if (stat.getRowCount() > 1)
                    {
                        if (stat.getColCount() > 1 && stat.isValidTableStart())
                            stat.lastgood = cur;
                    }
                }
                else
                {
                    break; //table no more valid, stop it
                }
            }
        }
        if (!stat.isValidTable())
            stat.lastgood = startchild; //not a valid table; revert
        return stat;
    }

    
    //==== LISTS =========================================================================
    
    
    /**
     * If a list starts at the specified child index of the area, tries to detect
     * the end of the list.
     * @param area The area where the list is placed.
     * @param nodes The child areas of the area that should be taken into account.
     * @param startchild The first child index that forms the list.
     * @return the last child that forms the list.
     */
    public int findListEnd(Area area, List<Area> nodes, int startchild)
    {
        ListInfo stat = new ListInfo(area.getTopology().getTopologyHeight());
        if (nodes.size() > 0 && nodes.get(0).toString().contains("Home"))
            System.out.println("jo!");
        
        //check indentation
        for (int i = startchild; i < nodes.size(); i++)
        {
            Area sub = nodes.get(i);
            //if (sub.getTopology().getPreviousOnLine() == null) //TODO first nodes on the line
            //{
                Rectangular gp = area.getTopology().getPosition(sub);
                int x1 = gp.getX1();
                int y1 = gp.getY1();
                if (stat.fitsCols(x1))
                {
                    stat.updateY1(y1);
                    stat.lastgood = i;
                }
                else
                    break;
            //}
        }
        //check the rows and styles
        ListInfo newstat = new ListInfo(area.getTopology().getTopologyHeight());
        for (int i = startchild; i < nodes.size(); i++)
        {
            Area sub = nodes.get(i);
            Rectangular gp = area.getTopology().getPosition(sub);
            int x1 = gp.getX1();
            int y1 = gp.getY1();
            if (x1 == stat.x1) //main nodes (first column)
            {
                if (isListNode(sub)) //node text is acceptable for list
                {
                    //the first column list item elements must have a consistent style
                    NodeStyle style = new NodeStyle(sub);
                    if (newstat.updateStyles(style) && newstat.stylesAcceptable())
                    {
                        stat.updateY1(y1);
                        newstat.lastgood = i;
                    }
                    else
                        break;
                }
                else
                    break;
            }
            else //other nodes (not the first column)
            {
                if (stat.rows[y1] != 0) //other nodes must fit the rows
                {
                    stat.updateY1(y1);
                    newstat.lastgood = i;
                }
                else
                    break;
            }
        }
        return newstat.lastgood;
    }
    
    protected boolean isList(Area area, List<Area> nodes, int startchild)
    {
        int lastchild = findListEnd(area, nodes, startchild);
        return (lastchild >= startchild + 2) && (lastchild == nodes.size() - 1);
    }
    
    private boolean isListNode(Area node)
    {
        //trivial list detection: we require at most ten words in the line and a capital letter in the beginning
        String text = node.getText().trim();
        if (text.length() > 0)
            return startsWithCapital(text) && (getWordCount(text) <= 10);
        else
            return false;
    }
    
    private boolean startsWithCapital(String text)
    {
        return text.length() > 0 && Character.isUpperCase(text.charAt(0));
    }
    
    private int getWordCount(String text)
    {
        String[] w = text.split("\\s+");
        return w.length;
    }
    
    protected class ListInfo
    {
        /** Number of different styles accepted in a list */
        public static final int NSTYLES = 2;
        
        /** used styles */
        public NodeStyle[] styles;
        /** style usage count */
        public int[] stylecnt;
        /** used grid rows */
        public int[] rows;
        /** basic left edge of the list */
        public int x1;
        /** minimal Y1 of the areas */
        public int minY1;
        /** maximal Y1 of the areas */
        public int maxY1;
        /** last child where the table was acceptable */
        public int lastgood;
        
        public ListInfo(int nrows)
        {
            x1 = -1;
            minY1 = -1;
            maxY1 = -1;
            rows = new int[nrows];
            for (int i = 0; i < nrows; i++)
                rows[i] = 0;
            styles = new NodeStyle[NSTYLES];
            stylecnt = new int[NSTYLES];
        }
        
        /**
         * Updates the maximal/minimal Y1 with a new value
         * @param y1 the new value to be considered
         */
        public void updateY1(int y1)
        {
            if (minY1 == -1 || y1 < minY1)
                minY1 = y1;
            if (y1 > maxY1)
                maxY1 = y1;
            rows[y1]++;
        }
        
        /**
         * Checks if the indentation fits to the list and updates the indentation info when necessary.
         * @param x1 start x coordinate of the new area
         */
        public boolean fitsCols(int x)
        {
            if (x1 == -1)
            {
                x1 = x;
                return true;
            }
            else if (x1 == x)
            {
                return true;
            }
            else
                return false;
        }
        
        /**
         * Compares the style with the existing list styles and updates the statistics.
         * @param style
         * @return true when the style can be accepted, false otherwise
         */
        public boolean updateStyles(NodeStyle style)
        {
            for (int i = 0; i < NSTYLES; i++)
            {
                if (styles[i] == null)
                {
                    styles[i] = style;
                    stylecnt[i] = 1;
                    return true;
                }
                else if (styles[i].equals(style))
                {
                    stylecnt[i]++;
                    return true;
                }
            }
            return false; //style not accepted
        }
        
        public boolean stylesAcceptable()
        {
            boolean countok = (stylecnt[0] <= 1) || (stylecnt[1] <= 1);
            boolean sizeok = (stylecnt[1] == 0) || (styles[1].getFontSize() == styles[0].getFontSize());
            return countok && sizeok;
        }
    }
    
    //====================================================================================
    
    public enum LayoutType 
    { 
        /** Normal flow */
        NORMAL("normal"),
        /** Tabular layout */
        TABLE("table"),
        /** A simple list */
        LIST("list");
        
        private String name;
        
        private LayoutType(String name)
        {
            this.name = name;
        }
        
        public String toString()
        {
            return name;
        }
    } 
}
