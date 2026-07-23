package tn.esprit.stagetest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tn.esprit.stagetest.entity.Department;
import tn.esprit.stagetest.service.DepartmentService;

@Controller
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public String listDepartments(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("departments", departmentService.searchDepartments(keyword));
        model.addAttribute("keyword", keyword);
        return "departments/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("department", new Department());
        model.addAttribute("pageTitle", "Create Department");
        return "departments/form";
    }

    @PostMapping("/save")
    public String saveDepartment(@Valid @ModelAttribute("department") Department department,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        if (department.getId() == null) {
            if (departmentService.codeExists(department.getCode())) {
                result.rejectValue("code", "error.department", "Department code already exists");
            }
        } else {
            if (departmentService.codeExistsForOther(department.getCode(), department.getId())) {
                result.rejectValue("code", "error.department", "Department code already exists on another department");
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", department.getId() == null ? "Create Department" : "Edit Department");
            return "departments/form";
        }

        departmentService.saveDepartment(department);
        redirectAttributes.addFlashAttribute("successMessage", "Department saved successfully!");
        return "redirect:/departments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return departmentService.getDepartmentById(id)
                .map(department -> {
                    model.addAttribute("department", department);
                    model.addAttribute("pageTitle", "Edit Department");
                    return "departments/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Department not found.");
                    return "redirect:/departments";
                });
    }

    @GetMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            departmentService.deleteDepartment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Department deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting department: " + e.getMessage());
        }
        return "redirect:/departments";
    }
}
