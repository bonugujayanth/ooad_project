package com.healthcare.controller;

import com.healthcare.model.User;
import com.healthcare.pattern.creational.UserFactory;
import com.healthcare.model.enums.Role;
import com.healthcare.repository.IAuditLogService;

import java.util.UUID;

/**
 * MVC Controller - Authentication
 * Member: Kireeti Reddy P (PES1UG23CS307)
 */
public class AuthController {
    private final IAuditLogService auditLog;

    public AuthController(IAuditLogService auditLog) { this.auditLog = auditLog; }

    public boolean login(User user, String password) {
        boolean ok = user.authenticate(password);
        auditLog.log(ok ? "LOGIN_SUCCESS" : "LOGIN_FAILED", user.getUserId());
        return ok;
    }
}
