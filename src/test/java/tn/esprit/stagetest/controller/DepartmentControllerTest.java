package tn.esprit.stagetest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.stagetest.entity.Department;
import tn.esprit.stagetest.service.DepartmentService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DepartmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentService departmentService;

    @Test
    void testListDepartments() throws Exception {
        when(departmentService.searchDepartments("IT")).thenReturn(List.of(new Department()));

        mockMvc.perform(get("/departments").param("keyword", "IT"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attribute("keyword", "IT"))
                .andExpect(view().name("departments/list"));
    }

    @Test
    void testShowCreateForm() throws Exception {
        mockMvc.perform(get("/departments/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attribute("pageTitle", "Create Department"))
                .andExpect(view().name("departments/form"));
    }

    @Test
    void testSaveDepartment_SuccessNew() throws Exception {
        when(departmentService.codeExists(anyString())).thenReturn(false);

        mockMvc.perform(post("/departments/save")
                        .param("code", "D01")
                        .param("name", "HR Department"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Department saved successfully!"))
                .andExpect(redirectedUrl("/departments"));

        verify(departmentService, times(1)).saveDepartment(any(Department.class));
    }

    @Test
    void testSaveDepartment_SuccessEdit() throws Exception {
        when(departmentService.codeExistsForOther(anyString(), anyLong())).thenReturn(false);

        mockMvc.perform(post("/departments/save")
                        .param("id", "1")
                        .param("code", "D01")
                        .param("name", "HR Department"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Department saved successfully!"))
                .andExpect(redirectedUrl("/departments"));

        verify(departmentService, times(1)).saveDepartment(any(Department.class));
    }

    @Test
    void testSaveDepartment_ValidationErrors() throws Exception {
        mockMvc.perform(post("/departments/save")
                        .param("code", "") // Invalid
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("pageTitle", "Create Department"))
                .andExpect(view().name("departments/form"));
                
        verify(departmentService, never()).saveDepartment(any());
    }

    @Test
    void testSaveDepartment_NewCodeExists() throws Exception {
        when(departmentService.codeExists("D01")).thenReturn(true);

        mockMvc.perform(post("/departments/save")
                        .param("code", "D01")
                        .param("name", "HR Department"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("department", "code", "error.department"))
                .andExpect(model().attribute("pageTitle", "Create Department"))
                .andExpect(view().name("departments/form"));
    }

    @Test
    void testSaveDepartment_EditCodeExistsForOther() throws Exception {
        when(departmentService.codeExistsForOther("D01", 1L)).thenReturn(true);

        mockMvc.perform(post("/departments/save")
                        .param("id", "1")
                        .param("code", "D01")
                        .param("name", "HR Department"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("department", "code", "error.department"))
                .andExpect(model().attribute("pageTitle", "Edit Department"))
                .andExpect(view().name("departments/form"));
    }

    @Test
    void testShowEditForm_Success() throws Exception {
        Department department = new Department();
        department.setId(1L);
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(department));

        mockMvc.perform(get("/departments/edit/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("department"))
                .andExpect(model().attribute("pageTitle", "Edit Department"))
                .andExpect(view().name("departments/form"));
    }

    @Test
    void testShowEditForm_NotFound() throws Exception {
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/departments/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Department not found."))
                .andExpect(redirectedUrl("/departments"));
    }

    @Test
    void testDeleteDepartment_Success() throws Exception {
        doNothing().when(departmentService).deleteDepartment(1L);

        mockMvc.perform(get("/departments/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Department deleted successfully!"))
                .andExpect(redirectedUrl("/departments"));
    }

    @Test
    void testDeleteDepartment_Exception() throws Exception {
        doThrow(new RuntimeException("In use")).when(departmentService).deleteDepartment(1L);

        mockMvc.perform(get("/departments/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Error deleting department: In use"))
                .andExpect(redirectedUrl("/departments"));
    }
}
