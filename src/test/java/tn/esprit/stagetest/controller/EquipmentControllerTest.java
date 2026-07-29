package tn.esprit.stagetest.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.esprit.stagetest.entity.Employee;
import tn.esprit.stagetest.entity.Equipment;
import tn.esprit.stagetest.service.EmployeeService;
import tn.esprit.stagetest.service.EquipmentService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EquipmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class EquipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EquipmentService equipmentService;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    void testListEquipments() throws Exception {
        when(equipmentService.searchEquipments("Laptop")).thenReturn(List.of(new Equipment()));

        mockMvc.perform(get("/equipments").param("keyword", "Laptop"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("equipments"))
                .andExpect(model().attribute("keyword", "Laptop"))
                .andExpect(view().name("equipments/list"));
    }

    @Test
    void testShowCreateForm() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(new Employee()));

        mockMvc.perform(get("/equipments/new"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("equipment"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attribute("pageTitle", "Create Equipment"))
                .andExpect(view().name("equipments/form"));
    }

    @Test
    void testSaveEquipment_SuccessNew() throws Exception {
        when(equipmentService.serialNumberExists(anyString())).thenReturn(false);

        mockMvc.perform(post("/equipments/save")
                        .param("name", "Dell Laptop")
                        .param("serialNumber", "SN12345")
                        .param("type", "LAPTOP")
                        .param("employee.id", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Equipment saved successfully!"))
                .andExpect(redirectedUrl("/equipments"));

        verify(equipmentService, times(1)).saveEquipment(any(Equipment.class));
    }

    @Test
    void testSaveEquipment_SuccessWithEmployee() throws Exception {
        when(equipmentService.serialNumberExists(anyString())).thenReturn(false);
        Employee emp = new Employee();
        emp.setId(1L);
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(emp));

        mockMvc.perform(post("/equipments/save")
                        .param("name", "Dell Laptop")
                        .param("serialNumber", "SN12345")
                        .param("type", "LAPTOP")
                        .param("employee.id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipments"));

        verify(equipmentService, times(1)).saveEquipment(any(Equipment.class));
    }

    @Test
    void testSaveEquipment_SuccessWithInvalidEmployee() throws Exception {
        when(equipmentService.serialNumberExists(anyString())).thenReturn(false);
        when(employeeService.getEmployeeById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/equipments/save")
                        .param("name", "Dell Laptop")
                        .param("serialNumber", "SN12345")
                        .param("type", "LAPTOP")
                        .param("employee.id", "99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipments"));

        verify(equipmentService, times(1)).saveEquipment(argThat(eq -> eq.getEmployee() == null));
    }

    @Test
    void testSaveEquipment_ValidationErrors() throws Exception {
        mockMvc.perform(post("/equipments/save")
                        .param("name", "") // Invalid
                        .param("serialNumber", ""))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("pageTitle", "Create Equipment"))
                .andExpect(view().name("equipments/form"));

        verify(equipmentService, never()).saveEquipment(any());
    }
    
    @Test
    void testSaveEquipment_ValidationErrorsWithNullEmployee() throws Exception {
        mockMvc.perform(post("/equipments/save")
                        .param("name", "")) // Invalid
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("pageTitle", "Create Equipment"))
                .andExpect(view().name("equipments/form"));

        verify(equipmentService, never()).saveEquipment(any());
    }
    
    @Test
    void testSaveEquipment_ValidationErrorsEdit() throws Exception {
        mockMvc.perform(post("/equipments/save")
                        .param("id", "1")
                        .param("name", "")) // Invalid
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attribute("pageTitle", "Edit Equipment"))
                .andExpect(view().name("equipments/form"));

        verify(equipmentService, never()).saveEquipment(any());
    }

    @Test
    void testSaveEquipment_NewSerialNumberExists() throws Exception {
        when(equipmentService.serialNumberExists("SN12345")).thenReturn(true);

        mockMvc.perform(post("/equipments/save")
                        .param("name", "Dell Laptop")
                        .param("serialNumber", "SN12345")
                        .param("type", "LAPTOP"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("equipment", "serialNumber", "error.equipment"))
                .andExpect(model().attribute("pageTitle", "Create Equipment"))
                .andExpect(view().name("equipments/form"));
    }

    @Test
    void testSaveEquipment_EditSerialNumberExistsForOther() throws Exception {
        when(equipmentService.serialNumberExistsForOther("SN12345", 1L)).thenReturn(true);

        mockMvc.perform(post("/equipments/save")
                        .param("id", "1")
                        .param("name", "Dell Laptop")
                        .param("serialNumber", "SN12345")
                        .param("type", "LAPTOP"))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrorCode("equipment", "serialNumber", "error.equipment"))
                .andExpect(model().attribute("pageTitle", "Edit Equipment"))
                .andExpect(view().name("equipments/form"));
    }

    @Test
    void testShowEditForm_Success() throws Exception {
        Equipment equipment = new Equipment();
        equipment.setId(1L);
        when(equipmentService.getEquipmentById(1L)).thenReturn(Optional.of(equipment));
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/equipments/edit/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("equipment"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attribute("pageTitle", "Edit Equipment"))
                .andExpect(view().name("equipments/form"));
    }
    
    @Test
    void testShowEditForm_SuccessWithEmployeeNull() throws Exception {
        Equipment equipment = new Equipment();
        equipment.setId(1L);
        equipment.setEmployee(null); // Explicit null
        when(equipmentService.getEquipmentById(1L)).thenReturn(Optional.of(equipment));
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/equipments/edit/1"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("equipment"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attribute("pageTitle", "Edit Equipment"))
                .andExpect(view().name("equipments/form"));
    }

    @Test
    void testShowEditForm_NotFound() throws Exception {
        when(equipmentService.getEquipmentById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/equipments/edit/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Equipment not found."))
                .andExpect(redirectedUrl("/equipments"));
    }

    @Test
    void testDeleteEquipment_Success() throws Exception {
        doNothing().when(equipmentService).deleteEquipment(1L);

        mockMvc.perform(get("/equipments/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("successMessage", "Equipment deleted successfully!"))
                .andExpect(redirectedUrl("/equipments"));
    }

    @Test
    void testDeleteEquipment_Exception() throws Exception {
        doThrow(new RuntimeException("In use")).when(equipmentService).deleteEquipment(1L);

        mockMvc.perform(get("/equipments/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMessage", "Error deleting equipment: In use"))
                .andExpect(redirectedUrl("/equipments"));
    }
}
