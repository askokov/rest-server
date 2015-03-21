package uss.coi.cop.foundation.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uss.coi.cop.foundation.exception.USSException;
import uss.coi.cop.foundation.models.Employee;
import uss.coi.cop.foundation.repositories.EmployeeRepository;

@Component
public class SessionManager {
    @Autowired
    private EmployeeRepository employeeRepository;

    public Employee getCurrentEmployee() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? this.employeeRepository.findByLogin(auth.getName()) : null;
    }

    public String getLogin() throws USSException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            return auth.getName();
        } else {
            throw new USSException("The current user is not found or not authenticated");
        }
    }
}
