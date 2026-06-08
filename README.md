# Job Shortlisting & Matchmaking Application

A dual-portal desktop application built in Java that connects recruiters and candidates, featuring real-time data persistence, automated parameter-based profile filtering, and live application status synchronization.

## Key Features
* **Dual-Portal User Journeys:** Features separate, custom-designed UI frames for Recruiters (to post open roles and track matching applicants) and Candidates (to build credential profiles and discover open vacancies) using Java Swing.
* **Automated Screening Engine:** Programmed backend filter queries that automatically cross-reference candidate qualifications against posted position parameters, dynamically hiding underqualified profiles from recruiter views to automate top-of-funnel screening.
* **Real-Time Data Persistence:** Utilizes Java Database Connectivity (JDBC) paired with a local MySQL server to manage concurrent data synchronization pipelines securely.
* **Interactive Request-and-Approval Pipeline:** Connects relational data entities across candidate application events, enabling real-time status updates (*Pending*, *Approved*, etc.) upon action dispatch.

## Tech Stack
* **Language:** Java 
* **Database Management System:** MySQL (via XAMPP)
* **Libraries & Drivers:** JDBC (mysql-connector-j), Java Swing, Java AWT
* **Core Architectures:** Object-Oriented Programming (OOPs), Relational Database Design (RDBMS)

## Database Configuration & Schema Setup
To replicate and run this database ecosystem locally:
1. Initialize the **Apache** and **MySQL** modules via your local **XAMPP Control Panel**.
2. Navigate to `http://localhost/phpmyadmin` on your browser.
3. Open the **SQL editor tab** and execute the full setup script below to define your schemas and insert testing data:

```sql
CREATE DATABASE job_portal;
USE job_portal;

CREATE TABLE recruiter_users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50) NOT NULL
);

CREATE TABLE candidate_users (
    username VARCHAR(50) PRIMARY KEY,
    password VARCHAR(50) NOT NULL,
    experience INT NOT NULL,
    skills VARCHAR(255) NOT NULL,
    other_info TEXT
);

CREATE TABLE jobs (
    job_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    req_experience INT NOT NULL,
    req_skill VARCHAR(50) NOT NULL
);

CREATE TABLE applications (
    app_id INT AUTO_INCREMENT PRIMARY KEY,
    job_id INT,
    candidate_username VARCHAR(50),
    FOREIGN KEY (job_id) REFERENCES jobs(job_id),
    FOREIGN KEY (candidate_username) REFERENCES candidate_users(username)
);

-- Insert dummy jobs for testing
INSERT INTO jobs (title, req_experience, req_skill) VALUES 
('Java Developer', 2, 'Java'),
('Python Intern', 0, 'Python'),
('Database Administrator', 4, 'SQL');
