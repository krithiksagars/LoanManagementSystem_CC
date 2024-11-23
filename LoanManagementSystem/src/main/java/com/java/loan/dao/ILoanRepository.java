package com.java.loan.dao;

import com.java.loan.exception.InvalidLoanException;
import com.java.loan.model.Loan;
import java.util.List;

public interface ILoanRepository {
    boolean applyLoan(Loan loan);
    
    double calculateInterest(int loanId) throws InvalidLoanException;
    double calculateInterest(double principal, double rate, int term);
    
    boolean loanStatus(int loanId) throws InvalidLoanException;
    
    double calculateEMI(int loanId) throws InvalidLoanException;
    double calculateEMI(double principal, double rate, int term);
    
    boolean loanRepayment(int loanId, double amount) throws InvalidLoanException;
    
    List<Loan> getAllLoan();
    
    Loan getLoanById(int loanId) throws InvalidLoanException;
}