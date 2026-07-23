package tn.esprit.stagetest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tn.esprit.stagetest.entity.Employee;
import tn.esprit.stagetest.entity.Equipment;
import tn.esprit.stagetest.service.EmployeeService;
import tn.esprit.stagetest.service.EquipmentService;

@Controller
@RequestMapping("/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final EmployeeService employeeService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String listEquipments(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        model.addAttribute("equipments", equipmentService.searchEquipments(keyword));
        model.addAttribute("keyword", keyword);
        return "equipments/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        Equipment equipment = new Equipment();
        equipment.setEmployee(new Employee());
        model.addAttribute("equipment", equipment);
        model.addAttribute("employees", employeeService.getAllEmployees());
        model.addAttribute("pageTitle", "Create Equipment");
        return "equipments/form";
    }

    @PostMapping("/save")
    public String saveEquipment(@Valid @ModelAttribute("equipment") Equipment equipment,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        if (equipment.getId() == null) {
            if (equipmentService.serialNumberExists(equipment.getSerialNumber())) {
                result.rejectValue("serialNumber", "error.equipment", "Serial number already exists");
            }
        } else {
            if (equipmentService.serialNumberExistsForOther(equipment.getSerialNumber(), equipment.getId())) {
                result.rejectValue("serialNumber", "error.equipment", "Serial number used by another item");
            }
        }

        if (result.hasErrors()) {
            if (equipment.getEmployee() == null) {
                equipment.setEmployee(new Employee());
            }
            model.addAttribute("employees", employeeService.getAllEmployees());
            model.addAttribute("pageTitle", equipment.getId() == null ? "Create Equipment" : "Edit Equipment");
            return "equipments/form";
        }

        if (equipment.getEmployee() != null && equipment.getEmployee().getId() != null) {
            employeeService.getEmployeeById(equipment.getEmployee().getId())
                    .ifPresentOrElse(equipment::setEmployee, () -> equipment.setEmployee(null));
        } else {
            equipment.setEmployee(null);
        }

        equipmentService.saveEquipment(equipment);
        redirectAttributes.addFlashAttribute("successMessage", "Equipment saved successfully!");
        return "redirect:/equipments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return equipmentService.getEquipmentById(id)
                .map(equipment -> {
                    if (equipment.getEmployee() == null) {
                        equipment.setEmployee(new Employee());
                    }
                    model.addAttribute("equipment", equipment);
                    model.addAttribute("employees", employeeService.getAllEmployees());
                    model.addAttribute("pageTitle", "Edit Equipment");
                    return "equipments/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Equipment not found.");
                    return "redirect:/equipments";
                });
    }

    @GetMapping("/delete/{id}")
    public String deleteEquipment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            equipmentService.deleteEquipment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Equipment deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting equipment: " + e.getMessage());
        }
        return "redirect:/equipments";
    }
}
