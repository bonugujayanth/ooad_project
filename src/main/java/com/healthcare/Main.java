package com.healthcare;

import com.healthcare.controller.*;
import com.healthcare.db.DatabaseConnection;
import com.healthcare.db.SchemaInitializer;
import com.healthcare.model.*;
import com.healthcare.model.dto.PatientDTO;
import com.healthcare.model.enums.Role;
import com.healthcare.pattern.creational.UserFactory;
import com.healthcare.pattern.structural.ClinicFacade;
import com.healthcare.repository.*;
import com.healthcare.service.*;
import com.healthcare.view.ConsoleView;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Entry point — wires all layers together (manual DI, no framework).
 * Healthcare Appointment and Patient Record Manager
 * Team E12
 */
public class Main {
    public static void main(String[] args) {
        // 1. DB init (Singleton pattern kicks in here)
        SchemaInitializer.initialize();

        // 2. Build dependency graph manually
        IApptRepository      apptRepo   = new ApptRepositoryImpl();
        IPatientRepository   patRepo    = new PatientRepositoryImpl();
        IPrescriptionRepository rxRepo  = new PrescriptionRepositoryImpl();
        IAuditLogService     auditLog   = new AuditLogServiceImpl();

        INotificationService notifSvc   = new EmailSMSNotifService("SMTP-Client", "SMS-Gateway");

        IPatientService      patSvc     = new PatientServiceImpl(patRepo, auditLog);
        IAppointmentService  apptSvc    = new AppointmentServiceImpl(apptRepo, auditLog, notifSvc);
        IPrescriptionService rxSvc      = new PrescriptionServiceImpl(rxRepo, auditLog);
        ClinicAdminServiceImpl adminSvc = new ClinicAdminServiceImpl(auditLog, notifSvc);

        // 3. MVC Controllers
        PatientController      patCtrl  = new PatientController(patSvc);
        AppointmentController  apptCtrl = new AppointmentController(apptSvc);
        PrescriptionController rxCtrl   = new PrescriptionController(rxSvc);
        AdminController        adminCtrl= new AdminController(adminSvc);
        AuthController         authCtrl = new AuthController(auditLog);

        // 4. View
        ConsoleView view = new ConsoleView();

        // ---- Demo flow ----
        view.showMessage("=== Healthcare System Boot ===");

        // Register patient (Builder pattern under the hood)
        PatientDTO dto = new PatientDTO("jane.doe@email.com", "pass123",
                LocalDate.of(1990, 5, 12), "INS-001", List.of("penicillin"));
        Patient patient = patCtrl.register(dto);
        view.showPatient(patient);

        // Factory pattern: create a clinician user object
        User clinician = UserFactory.createUser(Role.CLINICIAN, UUID.randomUUID(),
                "dr.smith@clinic.com", "docpass", "LIC-999", "Cardiology");
        view.showMessage("Clinician created: " + clinician.getEmail());

        // Schedule appointment (Command + Notification triggered inside)
        Appointment appt = apptCtrl.scheduleAppointment(
                patient.getUserId(), clinician.getUserId(), "2025-09-15T10:30:00");
        view.showAppointment(appt);

        // Create and check prescription (Chain of Responsibility)
        Prescription rx = rxCtrl.createPrescription(appt);
        ConflictResult cr = rxCtrl.checkConflicts(rx);
        view.showConflict(cr);

        // Facade: combined workflow
        ClinicFacade facade = new ClinicFacade(patSvc, apptSvc, rxSvc);
        view.showMessage("Facade ready.");

        // Admin audit logs
        List<String> logs = adminCtrl.getAuditLogs(patient.getUserId());
        view.showAuditLogs(logs);

        // Shutdown
        DatabaseConnection.getInstance().close();
        view.showMessage("=== System shutdown ===");
    }
}
