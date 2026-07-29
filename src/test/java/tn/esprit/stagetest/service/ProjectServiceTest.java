package tn.esprit.stagetest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.stagetest.entity.Project;
import tn.esprit.stagetest.repository.ProjectRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project project;

    @BeforeEach
    void setUp() {
        project = Project.builder()
                .id(1L)
                .name("App Redesign")
                .description("Redesign main web portal")
                .status("IN_PROGRESS")
                .build();
    }

    @Test
    void testGetAllProjects() {
        when(projectRepository.findAll()).thenReturn(List.of(project));

        List<Project> result = projectService.getAllProjects();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("App Redesign");
    }

    @Test
    void testSearchProjects_NullKeyword() {
        when(projectRepository.findAll()).thenReturn(List.of(project));
        List<Project> result = projectService.searchProjects(null);
        assertThat(result).hasSize(1);
        verify(projectRepository, never()).findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void testSearchProjects_EmptyKeyword() {
        when(projectRepository.findAll()).thenReturn(List.of(project));
        List<Project> result = projectService.searchProjects("   ");
        assertThat(result).hasSize(1);
        verify(projectRepository, never()).findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(anyString(), anyString());
    }

    @Test
    void testSearchProjects_ValidKeyword() {
        when(projectRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase("App", "App")).thenReturn(List.of(project));
        List<Project> result = projectService.searchProjects(" App ");
        assertThat(result).hasSize(1);
    }

    @Test
    void testGetProjectById_Found() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        Optional<Project> result = projectService.getProjectById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("App Redesign");
    }

    @Test
    void testGetProjectById_NotFound() {
        when(projectRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<Project> result = projectService.getProjectById(2L);
        assertThat(result).isEmpty();
    }

    @Test
    void testSaveProject() {
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project saved = projectService.saveProject(project);

        assertThat(saved).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void testDeleteProject() {
        doNothing().when(projectRepository).deleteById(1L);
        projectService.deleteProject(1L);
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    void testCount() {
        when(projectRepository.count()).thenReturn(7L);
        long count = projectService.count();
        assertThat(count).isEqualTo(7L);
    }
}
