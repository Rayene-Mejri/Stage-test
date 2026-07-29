package tn.esprit.stagetest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.stagetest.entity.Department;
import tn.esprit.stagetest.entity.Employee;
import tn.esprit.stagetest.service.DepartmentService;
import tn.esprit.stagetest.service.EmployeeService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private DepartmentService departmentService;

    @Test
    void testListEmployees() throws Exception {
        when(employeeService.searchEmployees("John")).thenReturn(List.of(new Employee()));

        mockMvc.perform(get("/employees").param("keyword", "John"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attribute("keyword", "John"))
                .andExpect(view().name("employees/list"));
    }

    @Test
    void testShowCreateForm() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(List.of(new Department()));

        mockMvc.perform(get("/employees/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attribute("pageTitle", "Create Employee"))
                .andExpect(view().name("employees/form"));
    }

    @Test
    void testSaveEmployee_SuccessNew() throws Exception {
        when(employeeService.emailExists(anyString())).thenReturn(false);

        mockMvc.perform(post("/employees/save")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@test.com")
                        .param("department.id", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Employee saved successfully!"))
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService, times(1)).saveEmployee(any(Employee.class));
    }

    @Test
    void testSaveEmployee_SuccessWithDepartment() throws Exception {
        when(employeeService.emailExists(anyString())).thenReturn(false);
        Department dept = new Department();
        dept.setId(1L);
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(dept));

        mockMvc.perform(post("/employees/save")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@test.com")
                        .param("department.id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService, times(1)).saveEmployee(any(Employee.class));
    }

    @Test
    void testSaveEmployee_SuccessWithInvalidDepartment() throws Exception {
        when(employeeService.emailExists(anyString())).thenReturn(false);
        when(departmentService.getDepartmentById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/employees/save")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@test.com")
                        .param("department.id", "99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService, times(1)).saveEmployee(argThat(emp -> emp.getDepartment() == null));
    }

    @Test
    void testSaveEmployee_SuccessEdit() throws Exception {
        when(employeeService.emailExistsForOther(anyString(), anyLong())).thenReturn(false);

        mockMvc.perform(post("/employees/save")
                        .param("id", "1")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService, times(1)).saveEmployee(any(Employee.class));
    }

    @Test
    void testSaveEmployee_ValidationErrors() throws Exception {
        mockMvc.perform(post("/employees/save")
                        .param("firstName", "") // Invalid
                        .param("lastName", ""))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("pageTitle", "Create Employee"))
                .andExpect(view().name("employees/form"));

        verify(employeeService, never()).saveEmployee(any());
    }

    @Test
    void testSaveEmployee_NewEmailExists() throws Exception {
        when(employeeService.emailExists("john@test.com")).thenReturn(true);

        mockMvc.perform(post("/employees/save")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@test.com"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("employee", "email", "error.employee"))
                .andExpect(model().attribute("pageTitle", "Create Employee"))
                .andExpect(view().name("employees/form"));
    }

    @Test
    void testSaveEmployee_EditEmailExistsForOther() throws Exception {
        when(employeeService.emailExistsForOther("john@test.com", 1L)).thenReturn(true);

        mockMvc.perform(post("/employees/save")
                        .param("id", "1")
                        .param("firstName", "John")
                        .param("lastName", "Doe")
                        .param("email", "john@test.com"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("employee", "email", "error.employee"))
                .andExpect(model().attribute("pageTitle", "Edit Employee"))
                .andExpect(view().name("employees/form"));
    }

    @Test
    void testShowEditForm_Success() throws Exception {
        Employee employee = new Employee();
        employee.setId(1L);
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(employee));
        when(departmentService.getAllDepartments()).thenReturn(List.of());

        mockMvc.perform(get("/employees/edit/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("employee"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attribute("pageTitle", "Edit Employee"))
                .andExpect(view().name("employees/form"));
    }

    @Test
    void testShowEditForm_NotFound() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/employees/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Employee not found."))
                .andExpect(redirectedUrl("/employees"));
    }

    @Test
    void testDeleteEmployee_Success() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(get("/employees/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Employee deleted successfully!"))
                .andExpect(redirectedUrl("/employees"));
    }

    @Test
    void testDeleteEmployee_Exception() throws Exception {
        doThrow(new RuntimeException("In use")).when(employeeService).deleteEmployee(1L);

        mockMvc.perform(get("/employees/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Error deleting employee: In use"))
                .andExpect(redirectedUrl("/employees"));
    }
}
