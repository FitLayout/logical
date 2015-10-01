/**
 * LogicalStructurePlugin.java
 *
 * Created on 30. 9. 2015, 23:03:40 by burgetr
 */
package org.fit.layout.logical.gui;

import java.awt.Font;
import java.util.Locale;

import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.fit.layout.gui.AreaSelectionListener;
import org.fit.layout.gui.Browser;
import org.fit.layout.gui.BrowserPlugin;
import org.fit.layout.logical.AreaAttributes;
import org.fit.layout.model.Area;

/**
 * 
 * @author burgetr
 */
public class LogicalStructurePlugin implements BrowserPlugin, AreaSelectionListener
{
    private Browser browser;
    
    private JTextField markednessText;

    @Override
    public boolean init(Browser browser)
    {
        this.browser = browser;
        this.browser.addInfoPanel(getMarkednessText(), 0.0);
        this.browser.addAreaSelectionListener(this);
        return true;
    }
    
    //=================================================================
    
    private JTextField getMarkednessText() 
    {
        if (markednessText == null) 
        {
            markednessText = new JTextField();
            markednessText.setHorizontalAlignment(SwingConstants.CENTER);
            markednessText.setText("---");
            markednessText.setFont(new Font("Dialog", Font.PLAIN, 18));
            markednessText.setEditable(false);
            markednessText.setColumns(10);
        }
        return markednessText;
    }

    //=================================================================
    
    @Override
    public void areaSelected(Area area)
    {
        final AreaAttributes attrs = area.getAttribute(AreaAttributes.class);
        if (attrs != null)
        {
            final String mtext = String.format(Locale.US, "%1.2f", attrs.getMarkedness());
            getMarkednessText().setText(mtext + " / " + attrs.getLayoutType());
        }
        else
            getMarkednessText().setText("---");
    }
    
}
