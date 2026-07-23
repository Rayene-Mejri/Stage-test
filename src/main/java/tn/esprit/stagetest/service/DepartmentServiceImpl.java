package tn.esprit.stagetest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.stagetest.entity.Department;
import tn.esprit.stagetest.repository.DepartmentRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Department> searchDepartments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllDepartments();
        }
        return departmentRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(keyword.trim(), keyword.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    @Override
    public Department saveDepartment(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean codeExists(String code) {
        return departmentRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean codeExistsForOther(String code, Long id) {
        return departmentRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return departmentRepository.count();
    }
}
