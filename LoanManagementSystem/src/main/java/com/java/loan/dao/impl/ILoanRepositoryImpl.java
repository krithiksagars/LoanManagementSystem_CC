package com.java.loan.dao.impl;

import com.java.loan.dao.ILoanRepository;
import com.java.loan.exception.InvalidLoanException;
import com.java.loan.util.DBConnUtil;
import com.java.loan.model.*;
import com.java.loan.util.DBPropertyUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ILoanRepositoryImpl implements ILoanRepository {
	private Connection getConnection() throws SQLException {
	    String connStr = DBPropertyUtil.getConnectionString("db.properties");
	    return DBConnUtil.getConnection(connStr);
	}
	
	private int insertCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO Customer (name, email, phone_number, address, credit_score) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhoneNumber());
            pstmt.setString(4, customer.getAddress());
            pstmt.setInt(5, customer.getCreditScore());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating customer failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int customerId = generatedKeys.getInt(1);
                    customer.setCustomerId(customerId);
                    return customerId;
                } else {
                    throw new SQLException("Creating customer failed, no ID obtained.");
                }
            }
        }
    }

	@Override
    public boolean applyLoan(Loan loan) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Confirm loan application (Yes/No): ");
        if (!scanner.nextLine().equalsIgnoreCase("yes")) {
            return false;
        }

        try {

            int customerId = insertCustomer(loan.getCustomer());
            
            String sql = "INSERT INTO Loan (customer_id, principal_amount, interest_rate, loan_term, loan_type, loan_status, remaining_amount) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                
                pstmt.setInt(1, customerId);
                pstmt.setDouble(2, loan.getPrincipalAmount());
                pstmt.setDouble(3, loan.getInterestRate());
                pstmt.setInt(4, loan.getLoanTerm());
                pstmt.setString(5, loan.getLoanType().toString());
                pstmt.setString(6, LoanStatus.PENDING.toString());
                pstmt.setDouble(7, loan.getPrincipalAmount());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    return false;
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int loanId = generatedKeys.getInt(1);
                        loan.setLoanId(loanId);
                        
                        if (loan instanceof HomeLoan) {
                            insertHomeLoan(loanId, (HomeLoan) loan);
                        } else if (loan instanceof CarLoan) {
                            insertCarLoan(loanId, (CarLoan) loan);
                        }
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void insertHomeLoan(int loanId, HomeLoan loan) throws SQLException {
        String sql = "INSERT INTO HomeLoan (loan_id, property_address, property_value) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, loanId);
            pstmt.setString(2, loan.getPropertyAddress());
            pstmt.setDouble(3, loan.getPropertyValue());
            pstmt.executeUpdate();
        }
    }

    private void insertCarLoan(int loanId, CarLoan loan) throws SQLException {
        String sql = "INSERT INTO CarLoan (loan_id, car_model, car_value) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, loanId);
            pstmt.setString(2, loan.getCarModel());
            pstmt.setDouble(3, loan.getCarValue());
            pstmt.executeUpdate();
        }
    }

    @Override
    public double calculateInterest(int loanId) throws InvalidLoanException {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new InvalidLoanException("Loan not found with ID: " + loanId);
        }
        return calculateInterest(loan.getPrincipalAmount(), loan.getInterestRate(), loan.getLoanTerm());
    }

    @Override
    public double calculateInterest(double principal, double rate, int term) {
        return (principal * rate * term) / 1200; 
    }

    @Override
    public boolean loanStatus(int loanId) throws InvalidLoanException {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new InvalidLoanException("Loan not found with ID: " + loanId);
        }

        boolean isApproved = loan.getCustomer().getCreditScore() > 650;
        LoanStatus newStatus = isApproved ? LoanStatus.APPROVED : LoanStatus.REJECTED;

        String sql = "UPDATE Loan SET loan_status = ? WHERE loan_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus.toString());
            pstmt.setInt(2, loanId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public double calculateEMI(int loanId) throws InvalidLoanException {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new InvalidLoanException("Loan not found with ID: " + loanId);
        }
        return calculateEMI(loan.getPrincipalAmount(), loan.getInterestRate(), loan.getLoanTerm());
    }

    @Override
    public double calculateEMI(double principal, double rate, int term) {
        double monthlyRate = rate / 1200; // Convert annual rate to monthly
        return (principal * monthlyRate * Math.pow(1 + monthlyRate, term)) / 
               (Math.pow(1 + monthlyRate, term) - 1);
    }

    @Override
    public boolean loanRepayment(int loanId, double amount) throws InvalidLoanException {
        Loan loan = getLoanById(loanId);
        if (loan == null) {
            throw new InvalidLoanException("Loan not found with ID: " + loanId);
        }

        double emi = calculateEMI(loanId);
        if (amount < emi) {
            return false;
        }

        int numberOfEmis = (int) (amount / emi);
        double newRemainingAmount = loan.getRemainingAmount() - (emi * numberOfEmis);

        String sql = "UPDATE Loan SET remaining_amount = ? WHERE loan_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newRemainingAmount);
            pstmt.setInt(2, loanId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Loan> getAllLoan() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.*, c.*, " +
                    "h.property_address, h.property_value, " +
                    "ca.car_model, ca.car_value " +
                    "FROM Loan l " +
                    "JOIN Customer c ON l.customer_id = c.customer_id " +
                    "LEFT JOIN HomeLoan h ON l.loan_id = h.loan_id " +
                    "LEFT JOIN CarLoan ca ON l.loan_id = ca.loan_id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                loans.add(createLoanFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    @Override
    public Loan getLoanById(int loanId) throws InvalidLoanException {
        String sql = "SELECT l.*, c.*, " +
                    "h.property_address, h.property_value, " +
                    "ca.car_model, ca.car_value " +
                    "FROM Loan l " +
                    "JOIN Customer c ON l.customer_id = c.customer_id " +
                    "LEFT JOIN HomeLoan h ON l.loan_id = h.loan_id " +
                    "LEFT JOIN CarLoan ca ON l.loan_id = ca.loan_id " +
                    "WHERE l.loan_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, loanId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createLoanFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new InvalidLoanException("Loan not found with ID: " + loanId);
    }

    private Loan createLoanFromResultSet(ResultSet rs) throws SQLException {
        Customer customer = new Customer(
            rs.getInt("customer_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone_number"),
            rs.getString("address"),
            rs.getInt("credit_score")
        );

        LoanType loanType = LoanType.valueOf(rs.getString("loan_type"));
        Loan loan;

        if (loanType == LoanType.HOME) {
            loan = new HomeLoan();
            ((HomeLoan) loan).setPropertyAddress(rs.getString("property_address"));
            ((HomeLoan) loan).setPropertyValue(rs.getDouble("property_value"));
        } else {
            loan = new CarLoan();
            ((CarLoan) loan).setCarModel(rs.getString("car_model"));
            ((CarLoan) loan).setCarValue(rs.getDouble("car_value"));
        }

        loan.setLoanId(rs.getInt("loan_id"));
        loan.setCustomer(customer);
        loan.setPrincipalAmount(rs.getDouble("principal_amount"));
        loan.setInterestRate(rs.getDouble("interest_rate"));
        loan.setLoanTerm(rs.getInt("loan_term"));
        loan.setLoanStatus(LoanStatus.valueOf(rs.getString("loan_status")));
        loan.setRemainingAmount(rs.getDouble("remaining_amount"));

        return loan;
    }
}