package com.java.loan.model;

public class CarLoan extends Loan 
{
    private String carModel;
    private double carValue;
    
	public String getCarModel() {
		return carModel;
	}
	public void setCarModel(String carModel) {
		this.carModel = carModel;
	}
	public double getCarValue() {
		return carValue;
	}
	public void setCarValue(double carValue) {
		this.carValue = carValue;
	}
	
	@Override
	public String toString() {
		return "CarLoan [carModel=" + carModel + ", carValue=" + carValue + "]";
	}
	
	public CarLoan(int loanId, Customer customer, double principalAmount, double interestRate, int loanTerm,
			LoanType loanType, LoanStatus loanStatus, double remainingAmount, String carModel, double carValue) {
		super(loanId, customer, principalAmount, interestRate, loanTerm, loanType, loanStatus, remainingAmount);
		this.carModel = carModel;
		this.carValue = carValue;
	}
	
	public CarLoan() {
		super();
		setLoanType(LoanType.CAR);
		// TODO Auto-generated constructor stub
	}
	
	public CarLoan(int loanId, Customer customer, double principalAmount, double interestRate, int loanTerm,
			LoanType loanType, LoanStatus loanStatus, double remainingAmount) {
		super(loanId, customer, principalAmount, interestRate, loanTerm, loanType, loanStatus, remainingAmount);
		// TODO Auto-generated constructor stub
	}

    
}