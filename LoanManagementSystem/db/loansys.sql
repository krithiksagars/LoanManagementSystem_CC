-- Create Customer table
CREATE TABLE Customer (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    address TEXT NOT NULL,
    credit_score INT NOT NULL
);

-- Create Loan table
CREATE TABLE Loan (
    loan_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    principal_amount DECIMAL(15,2) NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    loan_term INT NOT NULL,
    loan_type ENUM('HOME', 'CAR') NOT NULL,
    loan_status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL,
    remaining_amount DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

-- Create HomeLoan table
CREATE TABLE HomeLoan (
    loan_id INT PRIMARY KEY,
    property_address TEXT NOT NULL,
    property_value DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (loan_id) REFERENCES Loan(loan_id)
);

-- Create CarLoan table
CREATE TABLE CarLoan (
    loan_id INT PRIMARY KEY,
    car_model VARCHAR(100) NOT NULL,
    car_value DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (loan_id) REFERENCES Loan(loan_id)
);