package tn.esprit.stagetest.service;

import tn.esprit.stagetest.entity.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    List<Department> getAllDepartments();
    List<Department> searchDepartments(String keyword);
    Optional<Department> getDepartmentById(Long id);
    Department saveDepartment(Department department);
    void deleteDepartment(Long id);
    boolean codeExists(String code);
    boolean codeExistsForOther(String code, Long id);
    long count();
}
