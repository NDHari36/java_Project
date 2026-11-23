/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.model;

import java.math.BigDecimal;

/**
 *
 * @author PC
 */
public class ShiftSummary {
    private BigDecimal openingCash;
    private BigDecimal cashRevenue;
    private BigDecimal transferRevenue;
    private BigDecimal cardRevenue;
    private BigDecimal totalRevenue;
    private BigDecimal expectedCash;
    private BigDecimal closingCash;
    private BigDecimal difference;

    public ShiftSummary() {
    }

    
    public ShiftSummary(BigDecimal openingCash, BigDecimal cashRevenue, BigDecimal transferRevenue, BigDecimal cardRevenue, BigDecimal totalRevenue, BigDecimal expectedCash, BigDecimal closingCash, BigDecimal difference) {
        this.openingCash = openingCash;
        this.cashRevenue = cashRevenue;
        this.transferRevenue = transferRevenue;
        this.cardRevenue = cardRevenue;
        this.totalRevenue = totalRevenue;
        this.expectedCash = expectedCash;
        this.closingCash = closingCash;
        this.difference = difference;
    }

    
    
    public BigDecimal getOpeningCash() {
        return openingCash;
    }

    public void setOpeningCash(BigDecimal openingCash) {
        this.openingCash = openingCash;
    }

    public BigDecimal getCashRevenue() {
        return cashRevenue;
    }

    public void setCashRevenue(BigDecimal cashRevenue) {
        this.cashRevenue = cashRevenue;
    }

    public BigDecimal getTransferRevenue() {
        return transferRevenue;
    }

    public void setTransferRevenue(BigDecimal transferRevenue) {
        this.transferRevenue = transferRevenue;
    }

    public BigDecimal getCardRevenue() {
        return cardRevenue;
    }

    public void setCardRevenue(BigDecimal cardRevenue) {
        this.cardRevenue = cardRevenue;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getExpectedCash() {
        return expectedCash;
    }

    public void setExpectedCash(BigDecimal expectedCash) {
        this.expectedCash = expectedCash;
    }

    public BigDecimal getClosingCash() {
        return closingCash;
    }

    public void setClosingCash(BigDecimal closingCash) {
        this.closingCash = closingCash;
    }

    public BigDecimal getDifference() {
        return difference;
    }

    public void setDifference(BigDecimal difference) {
        this.difference = difference;
    }
}
