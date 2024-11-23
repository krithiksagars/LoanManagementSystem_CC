package com.java.loan.model;

public class HomeLoan extends Loan 
{
    private String propertyAddress;
    private double propertyValue;
    
	public String getPropertyAddress() {
		return propertyAddress;
	}
	public void setPropertyAddress(String propertyAddress) {
		this.propertyAddress = propertyAddress;
	}
	public double getPropertyValue() {
		return propertyValue;
	}
	public void setPropertyValue(double propertyValue) {
		this.propertyValue = propertyValue;
	}
	
	@Override
	public String toString() {
		return "HomeLoan [propertyAddress=" + propertyAddress + ", propertyValue=" + propertyValue + "]";
	}
	
	public HomeLoan(int loanId, Customer customer, double principalAmount, double interestRate, int loanTerm,
			LoanType loanType, LoanStatus loanStatus, double remainingAmount, String propertyAddress,
			double propertyValue) {
		super(loanId, customer, principalAmount, interestRate, loanTerm, loanType, loanStatus, remainingAmount);
		this.propertyAddress = propertyAddress;
		this.propertyValue = propertyValue;
	}
	
	public HomeLoan() {
		super();
		setLoanType(LoanType.HOME); 
		// TODO Auto-generated constructor stub
	}
	
	public HomeLoan(int loanId, Customer customer, double principalAmount, double interestRate, int loanTerm,
			LoanType loanType, LoanStatus loanStatus, double remainingAmount) {
		super(loanId, customer, principalAmount, interestRate, loanTerm, loanType, loanStatus, remainingAmount);
		// TODO Auto-generated constructor stub
	}

    
}