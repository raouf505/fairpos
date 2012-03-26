/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.discount;


import com.openbravo.format.Formats;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
//import com.openbravo.pos.ticket.JPanelTicket;
//import com.openbravo.pos.ticket.TicketProductInfo; 
import com.openbravo.pos.ticket.TicketTaxInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


/**
 *
 * @author Vilem
 */
public class DiscountManager {
    private float discountVal = 0.00f;
    
    public void setDiscountValue(float val) {
        this.discountVal=val;
    }
    
    public String getDiscountValueText() {
        return (""+ discountVal*100.0f + " %").replaceAll("\\.\\d*", "");
    }
    
    public void discountRowsClear(TicketInfo ticket)
    {
        
        List<TicketLineInfo> lineList = ticket.getLines();        
        Iterator<TicketLineInfo> it = lineList.iterator();
        
        List<Integer> toDelete = new ArrayList();
        
        int j=-1;
        while (it.hasNext()) {
            j++;
            TicketLineInfo line = it.next();
            
            String test;
            test = line.getProductName();
            if (test.startsWith("Discount "))
                //ticket.removeLine(j);
                toDelete.add(j);
        }
        
        for (Integer index: toDelete) {
            ticket.removeLine(index);
        }        
    }
    
    /*
     * Update discount for ticket
     *  - adds row with discount
     *  - called from JPanelTicket.java on such a places that this row is not visible (but it is there & its values are included in price totals)
     *  - deletes old discount lines & creates new
     *  
     */
    
    public void discountRowsUpdate(TicketInfo ticket) {
 
        discountRowsClear(ticket);
        
        
        
        TicketTaxInfo[] taxes = ticket.getTaxLines();  
        
        
        for (int i = 0; i < taxes.length; i++) {  
            TicketTaxInfo taxline = taxes[i];  
            
            ticket.insertLine(ticket.getLinesCount(),
                new TicketLineInfo(
                    "Discount " + discountVal + " of " + taxline.printSubTotal(),   
                    taxline.getTaxInfo().getTaxCategoryID(),          
                    1.0, 
                    -taxline.getSubTotal() * discountVal,
                    taxline.getTaxInfo()));  
        } 
        
        
        //sales.setSelectedIndex(ticket.getLinesCount() - 1);
        
    
    }    
    
    
}

/*

import com.openbravo.format.Formats;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.ticket.TicketProductInfo; 
import java.util.Properties;

discountrate = 0.5;  

total = ticket.getTotal();  
if (total > 0.0) {  
    sdiscount = Formats.PERCENT.formatValue(discountrate);  
 
    taxes = ticket.getTaxLines();  
    for (int i = 0; i < taxes.length; i++) {  
        taxline = taxes[i];  
        ticket.insertLine(ticket.getLinesCount(),
            new TicketLineInfo(
                "Discount " + sdiscount + " of " + taxline.printSubTotal(),   
                taxline.getTaxInfo().getTaxCategoryID(),          
                1.0, 
                -taxline.getSubTotal() * discountrate,
                taxline.getTaxInfo()));  
    }  
    sales.setSelectedIndex(ticket.getLinesCount() - 1);
} else {  
    java.awt.Toolkit.getDefaultToolkit().beep();  
}*/