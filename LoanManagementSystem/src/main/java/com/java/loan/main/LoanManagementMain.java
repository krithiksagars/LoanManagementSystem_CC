package com.java.loan.main;

import com.java.loan.dao.ILoanRepository;
import com.java.loan.dao.impl.ILoanRepositoryImpl;
import com.java.loan.exception.InvalidLoanException;
import com.java.loan.model.*;

import java.util.List;
import java.util.Scanner;

public class LoanManagementMain {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ILoanRepository loanRepository = new ILoanRepositoryImpl();

    public static void main(String[] args) {
        while (true) {
            displayMenu();
            int choice = getChoice();

            try {
                switch (choice) {
                    case 1:
                        applyNewLoan();
                        break;
                    case 2:
                        getAllLoans();
                        break;
                    case 3:
                        getLoanDetails();
                        break;
                    case 4:
                        processLoanRepayment();
                        break;
                    case 5:
                        System.out.println("Thank you for using Loan Management System!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void displayMenu() {
        System.out.println("\n=== Loan Management System ===");
        System.out.println("1. Apply for New Loan");
        System.out.println("2. View All Loans");
        System.out.println("3. View Loan Details");
        System.out.println("4. Make Loan Repayment");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
    }

    private static int getChoice() {
        while (!scanner.hasNextInt()) {
            System.out.println("Please enter a valid number.");
            scanner.next();
        }
        return scanner.nextInt();
    }

    private static void applyNewLoan() {
        scanner.nextLine(); 
        
        System.out.println("\n=== New Loan Application ===");
        
        Customer customer = new Customer();
        System.out.print("Enter customer name: ");
        customer.setName(scanner.nextLine());
        System.out.print("Enter email: ");
        customer.setEmail(scanner.nextLine());
        System.out.print("Enter phone number: ");
        customer.setPhoneNumber(scanner.nextLine());
        System.out.print("Enter address: ");
        customer.setAddress(scanner.nextLine());
        System.out.print("Enter credit score: ");
        customer.setCreditScore(scanner.nextInt());
        scanner.nextLine(); 

        System.out.print("Enter loan type (1 for Home Loan, 2 for Car Loan): ");
        int loanTypeChoice = scanner.nextInt();
        scanner.nextLine(); 

        System.out.print("Enter principal amount: ");
        double principal = scanner.nextDouble();
        System.out.print("Enter interest rate: ");
        double rate = scanner.nextDouble();
        System.out.print("Enter loan term (in months): ");
        int term = scanner.nextInt();
        scanner.nextLine(); 

        Loan loan;
        if (loanTypeChoice == 1) {
            HomeLoan homeLoan = new HomeLoan();
            System.out.print("Enter property address: ");
            homeLoan.setPropertyAddress(scanner.nextLine());
            System.out.print("Enter property value: ");
            homeLoan.setPropertyValue(scanner.nextDouble());
            loan = homeLoan;
        } else {
            CarLoan carLoan = new CarLoan();
            System.out.print("Enter car model: ");
            carLoan.setCarModel(scanner.nextLine());
            System.out.print("Enter car value: ");
            carLoan.setCarValue(scanner.nextDouble());
            loan = carLoan;
        }

        loan.setCustomer(customer);
        loan.setPrincipalAmount(principal);
        loan.setInterestRate(rate);
        loan.setLoanTerm(term);
        loan.setLoanStatus(LoanStatus.PENDING);

        if (loanRepository.applyLoan(loan)) {
            System.out.println("Loan application submitted successfully!");

            try {
                loanRepository.loanStatus(loan.getLoanId());
                System.out.println("Loan status updated based on credit score.");
            } catch (InvalidLoanException e) {
                System.out.println("Error updating loan status: " + e.getMessage());
            }
        } else {
            System.out.println("Failed to submit loan application.");
        }
    }

    private static void getAllLoans() {
        List<Loan> loans = loanRepository.getAllLoan();
        if (loans.isEmpty()) {
            System.out.println("No loans found.");
            return;
        }

        System.out.println("\n=== All Loans ===");
        for (Loan loan : loans) {
            displayLoanDetails(loan);
        }
    }

    private static void getLoanDetails() {
        System.out.print("Enter loan ID: ");
        int loanId = scanner.nextInt();

        try {
            Loan loan = loanRepository.getLoanById(loanId);
            displayLoanDetails(loan);
            
            System.out.printf("Monthly EMI: %.2f%n", loanRepository.calculateEMI(loanId));
            System.out.printf("Total Interest: %.2f%n", loanRepository.calculateInterest(loanId));
        } catch (InvalidLoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void processLoanRepayment() {
        System.out.print("Enter loan ID: ");
        int loanId = scanner.nextInt();
        System.out.print("Enter repayment amount: ");
        double amount = scanner.nextDouble();

        try {
            if (loanRepository.loanRepayment(loanId, amount)) {
                System.out.println("Repayment processed successfully!");
            } else {
                System.out.println("Repayment failed. Amount should be at least one EMI.");
            }
        } catch (InvalidLoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void displayLoanDetails(Loan loan) {
        System.out.println("\n-----------------");
        System.out.println("Loan ID: " + loan.getLoanId());
        System.out.println("Customer: " + loan.getCustomer().getName());
        System.out.println("Loan Type: " + loan.getLoanType());
        System.out.println("Principal Amount: " + loan.getPrincipalAmount());
        System.out.println("Interest Rate: " + loan.getInterestRate() + "%");
        System.out.println("Loan Term: " + loan.getLoanTerm() + " months");
        System.out.println("Status: " + loan.getLoanStatus());
        System.out.println("Remaining Amount: " + loan.getRemainingAmount());

        if (loan instanceof HomeLoan) {
            HomeLoan homeLoan = (HomeLoan) loan;
            System.out.println("Property Address: " + homeLoan.getPropertyAddress());
            System.out.println("Property Value: " + homeLoan.getPropertyValue());
        } else if (loan instanceof CarLoan) {
            CarLoan carLoan = (CarLoan) loan;
            System.out.println("Car Model: " + carLoan.getCarModel());
            System.out.println("Car Value: " + carLoan.getCarValue());
        }
    }
}