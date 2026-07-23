package tn.esprit.stagetest.service;

import tn.esprit.stagetest.entity.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    List<Employee> getAllEmployees();
    List<Employee> searchEmployees(String keyword);
    Optional<Employee> getEmployeeById(Long id);
    Employee saveEmployee(Employee employee);
    void deleteEmployee(Long id);
    boolean emailExists(String email);
    boolean emailExistsForOther(String email, Long id);
    long count();
}
