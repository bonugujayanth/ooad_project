package com.healthcare.service;

import com.healthcare.model.*;
import com.healthcare.pattern.behavioral.AllergyCheckerHandler;
import com.healthcare.pattern.behavioral.DrugInteractionHandler;
import com.healthcare.pattern.behavioral.ConflictHandler;
import com.healthcare.repository.IAuditLogService;
import com.healthcare.repository.IPrescriptionRepository;

import java.util.UUID;

/**
 * Member: K Sailakshmi Srinivas (PES1UG23CS271)
 * Uses: Chain of Responsibility (behavioral) for conflict checking
 */
public class PrescriptionServiceImpl implements IPrescriptionService {
    private final IPrescriptionRepository repo;
    private final IAuditLogService auditLog;

    public PrescriptionServiceImpl(IPrescriptionRepository repo, IAuditLogService auditLog) {
        this.repo = repo;
        this.auditLog = auditLog;
    }

    @Override
    public Prescription createRx(Appointment appointment) {
        Prescription rx = new Prescription(UUID.randomUUID(), appointment.getAppointmentId());
        repo.save(rx);
        auditLog.log("CREATE_PRESCRIPTION:" + rx.getRxId(), appointment.getPatientId());
        return rx;
    }

    @Override
    public void issueRx(UUID rxId) {
        Prescription rx = repo.findById(rxId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + rxId));
        rx.issue();
        repo.update(rx);
        auditLog.log("ISSUE_PRESCRIPTION:" + rxId, null);
    }

    @Override
    public ConflictResult checkConflicts(Prescription rx) {
        // Chain of Responsibility: AllergyChecker -> DrugInteractionChecker
        ConflictHandler chain = new AllergyCheckerHandler(new DrugInteractionHandler(null));
        return chain.check(rx);
    }
}
