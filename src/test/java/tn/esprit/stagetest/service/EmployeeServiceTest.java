package tn.esprit.stagetest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.stagetest.entity.Employee;
import tn.esprit.stagetest.repository.EmployeeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .jobTitle("Developer")
                .salary(75000.0)
                .hireDate(LocalDate.of(2023, 1, 15))
                .build();
    }

    @Test
    void testGetAllEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<Employee> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void testSearchEmployees_NullKeyword() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        List<Employee> result = employeeService.searchEmployees(null);
        assertThat(result).hasSize(1);
        verify(employeeRepository, never()).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(anyString(), anyString(), anyString());
    }

    @Test
    void testSearchEmployees_EmptyKeyword() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));
        List<Employee> result = employeeService.searchEmployees("   ");
        assertThat(result).hasSize(1);
        verify(employeeRepository, never()).findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(anyString(), anyString(), anyString());
    }

    @Test
    void testSearchEmployees_ValidKeyword() {
        when(employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase("John", "John", "John"))
                .thenReturn(List.of(employee));
        
        List<Employee> result = employeeService.searchEmployees(" John ");
        assertThat(result).hasSize(1);
    }

    @Test
    void testGetEmployeeById_Found() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Optional<Employee> result = employeeService.getEmployeeById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void testGetEmployeeById_NotFound() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<Employee> result = employeeService.getEmployeeById(2L);
        assertThat(result).isEmpty();
    }

    @Test
    void testSaveEmployee() {
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        Employee saved = employeeService.saveEmployee(employee);
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(1L);
    }

    @Test
    void testDeleteEmployee() {
        doNothing().when(employeeRepository).deleteById(1L);
        employeeService.deleteEmployee(1L);
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    void testEmailExists() {
        when(employeeRepository.existsByEmail("john.doe@example.com")).thenReturn(true);
        boolean exists = employeeService.emailExists("john.doe@example.com");
        assertThat(exists).isTrue();
    }

    @Test
    void testEmailExistsForOther() {
        when(employeeRepository.existsByEmailAndIdNot("john.doe@example.com", 2L)).thenReturn(false);
        boolean exists = employeeService.emailExistsForOther("john.doe@example.com", 2L);
        assertThat(exists).isFalse();
    }

    @Test
    void testCount() {
        when(employeeRepository.count()).thenReturn(10L);
        long count = employeeService.count();
        assertThat(count).isEqualTo(10L);
    }
}
