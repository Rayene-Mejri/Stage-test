-- Flyway migration script V2: Add organization tables

CREATE TABLE IF NOT EXISTS `departments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `code` VARCHAR(50) NOT NULL UNIQUE,
    `location` VARCHAR(255),
    `budget` DOUBLE,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `employees` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `first_name` VARCHAR(255) NOT NULL,
    `last_name` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `job_title` VARCHAR(255),
    `salary` DOUBLE,
    `hire_date` DATE,
    `department_id` BIGINT,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_employee_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `projects` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `description` TEXT,
    `status` VARCHAR(50) NOT NULL,
    `start_date` DATE,
    `end_date` DATE,
    `department_id` BIGINT,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_project_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `equipments` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `serial_number` VARCHAR(255) NOT NULL UNIQUE,
    `category` VARCHAR(100),
    `purchase_date` DATE,
    `employee_id` BIGINT,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_equipment_employee` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Sample Seed Data
INSERT IGNORE INTO `departments` (`id`, `name`, `code`, `location`, `budget`) VALUES
(1, 'Engineering', 'ENG', 'Building A - 3rd Floor', 250000.00),
(2, 'Human Resources', 'HR', 'Building B - 1st Floor', 80000.00),
(3, 'Finance', 'FIN', 'Building A - 2nd Floor', 120000.00);

INSERT IGNORE INTO `employees` (`id`, `first_name`, `last_name`, `email`, `job_title`, `salary`, `hire_date`, `department_id`) VALUES
(1, 'Alex', 'Morgan', 'alex.morgan@organization.com', 'Senior Software Engineer', 85000.00, '2023-01-15', 1),
(2, 'Sarah', 'Connor', 'sarah.connor@organization.com', 'HR Lead', 65000.00, '2022-05-10', 2),
(3, 'Michael', 'Scott', 'michael.scott@organization.com', 'Financial Analyst', 72000.00, '2021-11-01', 3);

INSERT IGNORE INTO `projects` (`id`, `name`, `description`, `status`, `start_date`, `end_date`, `department_id`) VALUES
(1, 'Cloud Migration', 'Migrate core application infrastructure to cloud provider', 'IN_PROGRESS', '2026-02-01', '2026-09-30', 1),
(2, 'Employee Onboarding Portal', 'Redesign candidate and onboarding workflow', 'PLANNING', '2026-06-01', '2026-11-15', 2);

INSERT IGNORE INTO `equipments` (`id`, `name`, `serial_number`, `category`, `purchase_date`, `employee_id`) VALUES
(1, 'MacBook Pro 16"', 'MBP-2026-001', 'Laptop', '2023-01-10', 1),
(2, 'Dell UltraSharp 27"', 'DEL-2026-042', 'Monitor', '2022-05-12', 2);
