package uss.coi.cop.foundation.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import uss.coi.cop.foundation.models.Employee;
import uss.coi.cop.foundation.repositories.EmployeeRepository;
import uss.coi.cop.foundation.security.rules.GrantRule;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import static uss.coi.cop.foundation.security.rules.GrantRulesImpl.getRules;

@Component
public class BasePermissionEvaluator implements PermissionEvaluator {

    private final Map<String, GrantRule> rules = getRules();

    @Autowired
    private EmployeeRepository employeeRepository;

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasPermission(Authentication authentication, Object target, Object permission) {
        Employee employee = employeeRepository.findByLogin((String) authentication.getPrincipal());
        Collection<String> grants = (Collection<String>) permission;
        boolean hasGrant = false;
        if (employee != null) {
            for (String grant : grants) {
                GrantRule rule = this.rules.get(grant);
                if (rule == null) {
                    throw new UnsupportedOperationException("This grant was not implemented : " + grant);
                }
                hasGrant = rule.permission(employee, target);
            }
        }
        return hasGrant;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        throw new UnsupportedOperationException("The method 'hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission)' was not implemented.");
    }
}