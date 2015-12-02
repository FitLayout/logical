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
            int failY = area.getChildArea(stat.lastgood + 1).getTopology().getPosition().getY1(); //Y coordinate of the first failed node
            for (int i = 0; i < stat.lastgood; i++)
            {
                if (area.getChildArea(i).getTopology().getPosition().getY1() >= failY)
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
            if (isValidTable(stat))
            {
                int[] pos = findTableGridPositions(stat);
                System.out.print(area + " positions :");
                for (int i : pos)
                    System.out.print(" " + i);
                System.out.println();
            }
            return isValidTable(stat);
        }
        else
            return false;
    }
    
    protected int[] findTableGridPositions(TableInfo stat)
    {
        //count the positions where there are more items and they're not disabled
        int found = countValidColumns(stat.cols, 2, 2);
        //return the indices of the columns found
        int[] ret = new int[found];
        int f = 0;
        for (int i = 0; i < stat.cols.length; i++)
        {
            if (stat.cols[i] >= 2)
                ret[f++] = i;
        }
        
        return ret;
    }
    
    protected TableInfo collectTableStats(Area parent, List<Area> nodes, int startchild, boolean check)
    {
        TableInfo stat = new TableInfo(parent.getTopology().getTopologyWidth(), parent.getTopology().getTopologyHeight());
        stat.lastgood = startchild; 
        if (nodes.get(startchild).getId() == 1327)
            System.out.println("jo!");
        else
            return stat;
        
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
                        System.out.println("valid");
                        stat.lastgood = cur;
                    }
                }
                else
                {
                    System.out.println("invalid");
                    break; //table no more valid, stop it
                }
            }
        }
        return stat;
    }

    /**
     * Checks if the whole table is acceptable.
     * @param stat
     * @return
     */
    protected boolean isValidTable(TableInfo stat)
    {
        int yspan = stat.getRowCount();
        int cols = countValidColumns(stat.cols, 2, 2);
        return yspan >= 3 && cols >= 2;
    }
    
    /**
     * Checks if this is an acceptable beginning of a table
     * @param stat
     * @return
     */
    protected boolean isValidTableStart(TableInfo stat)
    {
        int c1 = countValidColumns(stat.cols, 1, 1); //at least two columns with one occurence
        int c2 = countValidColumns(stat.cols, 1, 2); //one of them (and not the first one) should have at least 2 occurences
        return (c1 >= 2 && c2 >= 2); 
    }
    
    /**
     * Counts valid columns detected in the table. The valid column must contain at least a specified number of occurences.
     * @param cols The column occurence counts
     * @param min1 Minimal number of occurences to consider the first column to be valid
     * @param min2 Minimal number of occurences to consider the remaining columns to be valid
     * @return Number of valid columns
     */
    protected int countValidColumns(int[] cols, int min1, int min2)
    {
        int found = 0;
        for (int i = 0; i < cols.length; i++)
        {
            if ((found == 0 && cols[i] >= min1) || (found > 0 && cols[i] >= min2))
                found++;
        }
        return found;
    }
    
    //===============================================================================
    
    protected class TableInfo
    {
        /** numbers of areas in grid columns */
        private int[] cols;
        /** numbers of areas in grid rows */
        private int[] rows;
        /** last child where the table was acceptable */
        public int lastgood;
        
        public TableInfo(int numcols, int numrows)
        {
            cols = new int[numcols];
            for (int i = 0; i < numcols; i++)
                cols[i] = 0;
            rows = new int[numrows];
            for (int i = 0; i < numrows; i++)
                rows[i] = 0;
        }
        
        public int getRowCount()
        {
            int cnt = 0;
            for (int i = 0; i < rows.length; i++)
            {
                if (rows[i] > 0)
                    cnt++;
            }
            return cnt;
        }
        
        public int getColCount()
        {
            int cnt = 0;
            for (int i = 0; i < cols.length; i++)
            {
                if (cols[i] > 0)
                    cnt++;
            }
            return cnt;
        }
        
        public boolean putToGrid(Area area)
        {
            Rectangular gp = area.getTopology().getPosition();
            int x1 = gp.getX1();
            int x2 = gp.getX2();
            int y1 = gp.getY1();
            int y2 = gp.getY2();
            
            return updateCols(x1, x2) && updateRows(y1, y2);
        }
        
        /**
         * Updates the column info with the new area span
         * @param x1 start x coordinate of the new area
         * @param x2 end x coordinate of the new area
         */
        private boolean updateCols(int x1, int x2)
        {
            boolean ret = true;
            //increase the number of items starting at this position (when not disabled)
            if (cols[x1] != -1)
                cols[x1]++;
            else
                ret = false;
            //disable all positions that are covered by this child
            for (int j = x1 + 1; j < x2; j++)
                cols[j] = -1;
            return ret;
        }
        
        /**
         * Updates the maximal/minimal Y1 with a new value
         * @param y1 the new value to be considered
         */
        private boolean updateRows(int y1, int y2)
        {
            boolean ret = true;
            //increase the number of items starting at this position (when not disabled)
            if (rows[y1] != -1)
                rows[y1]++;
            else
                ret = false;
            //disable all positions that are covered by this child
            for (int j = y1 + 1; j < y2; j++)
                rows[j] = -1;
            return ret;
        }
        
        
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
            if (sub.getTopology().getPreviousOnLine() == null) //first nodes on the line
            {
                Rectangular gp = sub.getTopology().getPosition();
                int x1 = gp.getX1();
                int y1 = gp.getY1();
                if (stat.fitsCols(x1))
                {
                    stat.updateY1(y1);
                    stat.lastgood = i;
                }
                else
                    break;
            }
        }
        //check the rows and styles
        ListInfo newstat = new ListInfo(area.getTopology().getTopologyHeight());
        for (int i = startchild; i < nodes.size(); i++)
        {
            Area sub = nodes.get(i);
            Rectangular gp = sub.getTopology().getPosition();
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
