package tn.esprit.stagetest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tn.esprit.stagetest.entity.Project;
import tn.esprit.stagetest.service.DepartmentService;
import tn.esprit.stagetest.service.ProjectService;

import java.util.List;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final DepartmentService departmentService;

    @GetMapping
    public String listProjects(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("projects", projectService.searchProjects(keyword));
        model.addAttribute("keyword", keyword);
        return "projects/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("project", new Project());
        model.addAttribute("departments", departmentService.getAllDepartments());
        model.addAttribute("statuses", List.of("PLANNING", "IN_PROGRESS", "COMPLETED", "ON_HOLD"));
        model.addAttribute("pageTitle", "Create Project");
        return "projects/form";
    }

    @PostMapping("/save")
    public String saveProject(@Valid @ModelAttribute("project") Project project,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllDepartments());
            model.addAttribute("statuses", List.of("PLANNING", "IN_PROGRESS", "COMPLETED", "ON_HOLD"));
            model.addAttribute("pageTitle", project.getId() == null ? "Create Project" : "Edit Project");
            return "projects/form";
        }

        projectService.saveProject(project);
        redirectAttributes.addFlashAttribute("successMessage", "Project saved successfully!");
        return "redirect:/projects";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return projectService.getProjectById(id)
                .map(project -> {
                    model.addAttribute("project", project);
                    model.addAttribute("departments", departmentService.getAllDepartments());
                    model.addAttribute("statuses", List.of("PLANNING", "IN_PROGRESS", "COMPLETED", "ON_HOLD"));
                    model.addAttribute("pageTitle", "Edit Project");
                    return "projects/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Project not found.");
                    return "redirect:/projects";
                });
    }

    @GetMapping("/delete/{id}")
    public String deleteProject(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            projectService.deleteProject(id);
            redirectAttributes.addFlashAttribute("successMessage", "Project deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting project: " + e.getMessage());
        }
        return "redirect:/projects";
    }
}
