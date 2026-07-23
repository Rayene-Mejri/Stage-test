package tn.esprit.stagetest.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import tn.esprit.stagetest.entity.Department;
import tn.esprit.stagetest.entity.Employee;
import tn.esprit.stagetest.entity.Equipment;
import tn.esprit.stagetest.entity.Project;
import tn.esprit.stagetest.service.DepartmentService;
import tn.esprit.stagetest.service.EmployeeService;
import tn.esprit.stagetest.service.EquipmentService;
import tn.esprit.stagetest.service.ProjectService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationControllersTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private ProjectService projectService;

    @Mock
    private EquipmentService equipmentService;

    @InjectMocks
    private EmployeeController employeeController;

    @InjectMocks
    private ProjectController projectController;

    @InjectMocks
    private EquipmentController equipmentController;

    private Model model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    @Test
    void testSaveEmployeeWithUnselectedDepartment() {
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .department(new Department()) // department with id = null
                .build();

        BindingResult bindingResult = new BeanPropertyBindingResult(employee, "employee");
        when(employeeService.emailExists("john.doe@example.com")).thenReturn(false);

        String view = employeeController.saveEmployee(employee, bindingResult, model, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/employees");
        assertThat(employee.getDepartment()).isNull();
        verify(employeeService, times(1)).saveEmployee(employee);
    }

    @Test
    void testSaveEmployeeWithValidDepartment() {
        Department dept = Department.builder().id(10L).name("HR").build();
        Department departmentParam = Department.builder().id(10L).build();

        Employee employee = Employee.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .department(departmentParam)
                .build();

        BindingResult bindingResult = new BeanPropertyBindingResult(employee, "employee");
        when(departmentService.getDepartmentById(10L)).thenReturn(Optional.of(dept));
        when(employeeService.emailExists("jane.doe@example.com")).thenReturn(false);

        String view = employeeController.saveEmployee(employee, bindingResult, model, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/employees");
        assertThat(employee.getDepartment()).isNotNull();
        assertThat(employee.getDepartment().getName()).isEqualTo("HR");
        verify(employeeService, times(1)).saveEmployee(employee);
    }

    @Test
    void testSaveProjectWithUnselectedDepartment() {
        Project project = Project.builder()
                .name("Test Project")
                .status("PLANNING")
                .department(new Department())
                .build();

        BindingResult bindingResult = new BeanPropertyBindingResult(project, "project");

        String view = projectController.saveProject(project, bindingResult, model, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/projects");
        assertThat(project.getDepartment()).isNull();
        verify(projectService, times(1)).saveProject(project);
    }

    @Test
    void testSaveEquipmentWithUnselectedEmployee() {
        Equipment equipment = Equipment.builder()
                .name("Laptop")
                .serialNumber("SN-999")
                .employee(new Employee())
                .build();

        BindingResult bindingResult = new BeanPropertyBindingResult(equipment, "equipment");
        when(equipmentService.serialNumberExists("SN-999")).thenReturn(false);

        String view = equipmentController.saveEquipment(equipment, bindingResult, model, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/equipments");
        assertThat(equipment.getEmployee()).isNull();
        verify(equipmentService, times(1)).saveEquipment(equipment);
    }
}
