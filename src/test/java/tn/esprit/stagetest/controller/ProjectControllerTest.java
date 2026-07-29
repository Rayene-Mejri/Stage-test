package tn.esprit.stagetest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.stagetest.entity.Department;
import tn.esprit.stagetest.entity.Project;
import tn.esprit.stagetest.service.DepartmentService;
import tn.esprit.stagetest.service.ProjectService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private DepartmentService departmentService;

    @Test
    void testListProjects() throws Exception {
        when(projectService.searchProjects("Alpha")).thenReturn(List.of(new Project()));

        mockMvc.perform(get("/projects").param("keyword", "Alpha"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("projects"))
                .andExpect(model().attribute("keyword", "Alpha"))
                .andExpect(view().name("projects/list"));
    }

    @Test
    void testShowCreateForm() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(List.of(new Department()));

        mockMvc.perform(get("/projects/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attribute("pageTitle", "Create Project"))
                .andExpect(view().name("projects/form"));
    }

    @Test
    void testSaveProject_SuccessNew() throws Exception {
        mockMvc.perform(post("/projects/save")
                        .param("name", "Project Alpha")
                        .param("description", "A test project")
                        .param("status", "PLANNING")
                        .param("department.id", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Project saved successfully!"))
                .andExpect(redirectedUrl("/projects"));

        verify(projectService, times(1)).saveProject(any(Project.class));
    }

    @Test
    void testSaveProject_SuccessWithDepartment() throws Exception {
        Department dept = new Department();
        dept.setId(1L);
        when(departmentService.getDepartmentById(1L)).thenReturn(Optional.of(dept));

        mockMvc.perform(post("/projects/save")
                        .param("name", "Project Alpha")
                        .param("description", "A test project")
                        .param("status", "PLANNING")
                        .param("department.id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        verify(projectService, times(1)).saveProject(any(Project.class));
    }

    @Test
    void testSaveProject_SuccessWithInvalidDepartment() throws Exception {
        when(departmentService.getDepartmentById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/projects/save")
                        .param("name", "Project Alpha")
                        .param("description", "A test project")
                        .param("status", "PLANNING")
                        .param("department.id", "99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        verify(projectService, times(1)).saveProject(argThat(proj -> proj.getDepartment() == null));
    }

    @Test
    void testSaveProject_ValidationErrors() throws Exception {
        mockMvc.perform(post("/projects/save")
                        .param("name", "") // Invalid
                        .param("description", ""))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("pageTitle", "Create Project"))
                .andExpect(view().name("projects/form"));

        verify(projectService, never()).saveProject(any());
    }

    @Test
    void testSaveProject_ValidationErrorsEdit() throws Exception {
        mockMvc.perform(post("/projects/save")
                        .param("id", "1")
                        .param("name", "")) // Invalid
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("pageTitle", "Edit Project"))
                .andExpect(view().name("projects/form"));

        verify(projectService, never()).saveProject(any());
    }

    @Test
    void testShowEditForm_Success() throws Exception {
        Project project = new Project();
        project.setId(1L);
        when(projectService.getProjectById(1L)).thenReturn(Optional.of(project));
        when(departmentService.getAllDepartments()).thenReturn(List.of());

        mockMvc.perform(get("/projects/edit/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attribute("pageTitle", "Edit Project"))
                .andExpect(view().name("projects/form"));
    }

    @Test
    void testShowEditForm_NotFound() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/projects/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Project not found."))
                .andExpect(redirectedUrl("/projects"));
    }

    @Test
    void testDeleteProject_Success() throws Exception {
        doNothing().when(projectService).deleteProject(1L);

        mockMvc.perform(get("/projects/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Project deleted successfully!"))
                .andExpect(redirectedUrl("/projects"));
    }

    @Test
    void testDeleteProject_Exception() throws Exception {
        doThrow(new RuntimeException("In use")).when(projectService).deleteProject(1L);

        mockMvc.perform(get("/projects/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Error deleting project: In use"))
                .andExpect(redirectedUrl("/projects"));
    }
}
