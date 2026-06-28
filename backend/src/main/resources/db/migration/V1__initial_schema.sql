-- IRS 1099 IRIS Filing Platform - Initial Schema

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    email_verification_expiry DATETIME,
    password_reset_token VARCHAR(255),
    password_reset_expiry DATETIME,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_verification_token (email_verification_token),
    INDEX idx_users_reset_token (password_reset_token)
);

CREATE TABLE business_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    business_name VARCHAR(255) NOT NULL,
    doing_business_as VARCHAR(255),
    ein_encrypted VARCHAR(500) NOT NULL,
    business_type VARCHAR(30),
    business_phone VARCHAR(20),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(2),
    zip_code VARCHAR(10),
    country VARCHAR(50) DEFAULT 'US',
    mailing_address_line1 VARCHAR(255),
    mailing_address_line2 VARCHAR(255),
    mailing_city VARCHAR(100),
    mailing_state VARCHAR(2),
    mailing_zip_code VARCHAR(10),
    tcc VARCHAR(5),
    iris_client_id VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    form_type VARCHAR(20) NOT NULL,
    tax_year INT NOT NULL,
    transmission_type VARCHAR(20) NOT NULL DEFAULT 'ORIGINAL',
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    utid VARCHAR(255),
    receipt_id VARCHAR(255),
    submission_id VARCHAR(255),
    original_unique_submission_id VARCHAR(500),
    cfsf_filing BOOLEAN NOT NULL DEFAULT FALSE,
    submitted_at DATETIME,
    acknowledged_at DATETIME,
    irs_errors TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_submissions_user (user_id),
    INDEX idx_submissions_status (status),
    INDEX idx_submissions_receipt (receipt_id)
);

CREATE TABLE form_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    record_id VARCHAR(255),
    form_type VARCHAR(20) NOT NULL,
    issuer_ein_encrypted VARCHAR(500),
    issuer_name VARCHAR(255),
    issuer_address_line1 VARCHAR(255),
    issuer_address_line2 VARCHAR(255),
    issuer_city VARCHAR(100),
    issuer_state VARCHAR(2),
    issuer_zip_code VARCHAR(10),
    issuer_phone VARCHAR(20),
    recipient_tin_encrypted VARCHAR(500),
    recipient_tin_type VARCHAR(10),
    recipient_first_name VARCHAR(100),
    recipient_middle_name VARCHAR(100),
    recipient_last_name VARCHAR(100),
    recipient_business_name VARCHAR(255),
    recipient_address_line1 VARCHAR(255),
    recipient_address_line2 VARCHAR(255),
    recipient_city VARCHAR(100),
    recipient_state VARCHAR(2),
    recipient_zip_code VARCHAR(10),
    recipient_country VARCHAR(50),
    recipient_account_number VARCHAR(255),
    form_data_json TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    irs_errors TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE CASCADE,
    INDEX idx_records_submission (submission_id)
);

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stripe_payment_intent_id VARCHAR(255),
    stripe_invoice_id VARCHAR(255),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL,
    description VARCHAR(500),
    receipt_url VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_payments_user (user_id),
    INDEX idx_payments_stripe (stripe_payment_intent_id)
);

CREATE TABLE subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    plan_type VARCHAR(20) NOT NULL,
    stripe_subscription_id VARCHAR(255),
    stripe_customer_id VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    start_date DATE,
    end_date DATE,
    forms_included INT NOT NULL DEFAULT 0,
    forms_used INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_subscriptions_user (user_id),
    INDEX idx_subscriptions_stripe (stripe_subscription_id)
);

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT,
    email_sent BOOLEAN NOT NULL DEFAULT FALSE,
    read_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notifications_user (user_id),
    INDEX idx_notifications_unread (user_id, read_at)
);

CREATE TABLE audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    details TEXT,
    ip_address VARCHAR(45),
    timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_timestamp (timestamp)
);
