package tn.esprit.stagetest.service;

import tn.esprit.stagetest.entity.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectService {
    List<Project> getAllProjects();
    List<Project> searchProjects(String keyword);
    Optional<Project> getProjectById(Long id);
    Project saveProject(Project project);
    void deleteProject(Long id);
    long count();
}
