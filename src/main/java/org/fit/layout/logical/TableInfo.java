/**
 * TableInfo.java
 *
 * Created on 4. 12. 2015, 13:41:02 by burgetr
 */
package org.fit.layout.logical;

import org.fit.layout.model.Area;
import org.fit.layout.model.Rectangular;

/**
 * Holds a statistical information about a table found (or being discovered) in the layout.
 * 
 * @author burgetr
 */
public class TableInfo
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
        Rectangular gp = area.getParent().getTopology().getPosition(area);
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
        //increase the number of items starting at this position
        cols[x1]++;
        //disable all positions that are covered by this child
        for (int j = x1 + 1; j < x2; j++)
        {
            if (cols[j] > 0)
                return false;
        }
        return true;
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
    
    /**
     * Checks if the whole table is acceptable.
     * @param stat
     * @return
     */
    public boolean isValidTable()
    {
        int yspan = getRowCount();
        int cols = countValidColumns(2, 2);
        return yspan >= 3 && cols >= 2;
    }
    
    /**
     * Checks if this is an acceptable beginning of a table
     * @param stat
     * @return
     */
    public boolean isValidTableStart()
    {
        int c1 = countValidColumns(1, 1); //at least two columns with one occurence
        int c2 = countValidColumns(1, 2); //one of them (and not the first one) should have at least 2 occurences
        return (c1 >= 2 && c2 >= 2); 
    }
    
    /**
     * Counts valid columns detected in the table. The valid column must contain at least a specified number of occurences.
     * @param min1 Minimal number of occurences to consider the first column to be valid
     * @param min2 Minimal number of occurences to consider the remaining columns to be valid
     * @return Number of valid columns
     */
    public int countValidColumns(int min1, int min2)
    {
        int found = 0;
        for (int i = 0; i < cols.length; i++)
        {
            if ((found == 0 && cols[i] >= min1) || (found > 0 && cols[i] >= min2))
                found++;
        }
        return found;
    }
    
    public int[] findTableGridPositions()
    {
        //count the positions where there are more items and they're not disabled
        int found = countValidColumns(2, 2);
        //return the indices of the columns found
        int[] ret = new int[found];
        int f = 0;
        for (int i = 0; i < cols.length; i++)
        {
            if (cols[i] >= 2)
                ret[f++] = i;
        }
        
        return ret;
    }
    

}