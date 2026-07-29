package tn.esprit.stagetest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.stagetest.entity.Department;
import tn.esprit.stagetest.repository.DepartmentRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;

    @BeforeEach
    void setUp() {
        department = Department.builder()
                .id(1L)
                .name("Engineering")
                .code("ENG")
                .location("Building A")
                .budget(200000.00)
                .build();
    }

    @Test
    void testGetAllDepartments() {
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        List<Department> result = departmentService.getAllDepartments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Engineering");
        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    void testSearchDepartments_NullKeyword_ReturnsAll() {
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        List<Department> result = departmentService.searchDepartments(null);

        assertThat(result).hasSize(1);
        verify(departmentRepository, times(1)).findAll();
        verify(departmentRepository, never()).findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void testSearchDepartments_EmptyKeyword_ReturnsAll() {
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        List<Department> result = departmentService.searchDepartments("   ");

        assertThat(result).hasSize(1);
        verify(departmentRepository, times(1)).findAll();
        verify(departmentRepository, never()).findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void testSearchDepartments_ValidKeyword_ReturnsFiltered() {
        when(departmentRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase("ENG", "ENG")).thenReturn(List.of(department));

        List<Department> result = departmentService.searchDepartments(" ENG ");

        assertThat(result).hasSize(1);
        verify(departmentRepository, times(1)).findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase("ENG", "ENG");
    }

    @Test
    void testGetDepartmentById_Found() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        Optional<Department> result = departmentService.getDepartmentById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("ENG");
    }

    @Test
    void testGetDepartmentById_NotFound() {
        when(departmentRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Department> result = departmentService.getDepartmentById(2L);

        assertThat(result).isEmpty();
    }

    @Test
    void testSaveDepartment() {
        when(departmentRepository.save(any(Department.class))).thenReturn(department);

        Department saved = departmentService.saveDepartment(department);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(1L);
    }

    @Test
    void testDeleteDepartment() {
        doNothing().when(departmentRepository).deleteById(1L);

        departmentService.deleteDepartment(1L);

        verify(departmentRepository, times(1)).deleteById(1L);
    }

    @Test
    void testCodeExists() {
        when(departmentRepository.existsByCode("ENG")).thenReturn(true);
        boolean exists = departmentService.codeExists("ENG");
        assertThat(exists).isTrue();
    }

    @Test
    void testCodeExistsForOther() {
        when(departmentRepository.existsByCodeAndIdNot("ENG", 1L)).thenReturn(false);
        boolean exists = departmentService.codeExistsForOther("ENG", 1L);
        assertThat(exists).isFalse();
    }

    @Test
    void testCount() {
        when(departmentRepository.count()).thenReturn(5L);
        long count = departmentService.count();
        assertThat(count).isEqualTo(5L);
    }
}
