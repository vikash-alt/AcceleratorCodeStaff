/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.staffHL;

/**
 *
 * @author skalidindi
 */
public class EligibilityContext {
private double hlEligibleAmt;
  private double hlUtilized;
  private double hlAvailable;
  private boolean isEligible;
  private String processInstanceId;
  private double propertyPrice;
  private double tax;
  private double insuranceCost;
  private double bookingCost;
  private double otherCost;
  private double totalRequestAmt;
  private double totalCostProperty;
  private double ancillaryCost;
  private double costOfPlot;
  private double costOfConstruction;
  private double maxLoanAsPerLTV;
  private double amtReq;
  private int roi;

    public int getRoi() {
        return roi;
    }

    public void setRoi(int roi) {
        this.roi = roi;
    }

    public double getAmtReq() {
        return amtReq;
    }

    public void setAmtReq(double amtReq) {
        this.amtReq = amtReq;
    }
    public double getMaxLoanAsPerLTV() {
        return maxLoanAsPerLTV;
    }

    public void setMaxLoanAsPerLTV(double maxLoanAsPerLTV) {
        this.maxLoanAsPerLTV = maxLoanAsPerLTV;
    }

    public double getCostOfPlot() {
        return costOfPlot;
    }

    public void setCostOfPlot(double costOfPlot) {
        this.costOfPlot = costOfPlot;
    }

    public double getCostOfConstruction() {
        return costOfConstruction;
    }

    public void setCostOfConstruction(double costOfConstruction) {
        this.costOfConstruction = costOfConstruction;
    }

    public double getAncillaryCost() {
        return ancillaryCost;
    }

    public void setAncillaryCost(double ancillaryCost) {
        this.ancillaryCost = ancillaryCost;
    }
    public double getTotalCostProperty() {
        return totalCostProperty;
    }

    public void setTotalCostProperty(double totalCostProperty) {
        this.totalCostProperty = totalCostProperty;
    }
    public double getTotalRequestAmt() {
        return totalRequestAmt;
    }

    public void setTotalRequestAmt(double totalRequestAmt) {
        this.totalRequestAmt = totalRequestAmt;
    }
    public double getPropertyPrice() {
        return propertyPrice;
    }

    public void setPropertyPrice(double propertyPrice) {
        this.propertyPrice = propertyPrice;
    }

    public double getTax() {
        return tax;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public double getInsuranceCost() {
        return insuranceCost;
    }

    public void setInsuranceCost(double insuranceCost) {
        this.insuranceCost = insuranceCost;
    }

    public double getBookingCost() {
        return bookingCost;
    }

    public void setBookingCost(double bookingCost) {
        this.bookingCost = bookingCost;
    }

    public double getOtherCost() {
        return otherCost;
    }

    public void setOtherCost(double otherCost) {
        this.otherCost = otherCost;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
    public double getHlEligibleAmt() {
        return hlEligibleAmt;
    }

    public void setHlEligibleAmt(double hlEligibleAmt) {
        this.hlEligibleAmt = hlEligibleAmt;
    }

    public double getHlUtilized() {
        return hlUtilized;
    }

    public void setHlUtilized(double hlUtilized) {
        this.hlUtilized = hlUtilized;
    }

    public double getHlAvailable() {
        return hlAvailable;
    }

    public void setHlAvailable(double hlAvailable) {
        this.hlAvailable = hlAvailable;
    }

    public boolean isIsEligible() {
        return isEligible;
    }

    public void setIsEligible(boolean isEligible) {
        this.isEligible = isEligible;
    }
  
}
