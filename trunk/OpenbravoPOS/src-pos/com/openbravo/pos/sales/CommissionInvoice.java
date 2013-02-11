/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openbravo.pos.sales;

import com.openbravo.pos.ticket.TicketInfo;

/**
 *
 * @author Semilogic
 */
public class CommissionInvoice {
      
    static private TicketInfo ticketSource = null;
    static private TicketInfo ticketRefund = null;
    
    public static void setSource(TicketInfo ticket) {
        CommissionInvoice.ticketSource = ticket;
    }
    
    public static void setRefund(TicketInfo ticket) {
        CommissionInvoice.ticketRefund = ticket;
    }
    
    public static TicketInfo getSource() {
        return ticketSource;
    }
    
    public static TicketInfo getRefund() {
        return ticketRefund;
    }
    //CommissionInvoice() {
    //}
    
}
