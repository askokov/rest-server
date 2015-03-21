package uss.coi.cop.foundation.security.rules;

import uss.coi.cop.foundation.models.Employee;

/**
 * This interface describes the rule for grant access
 */
public abstract class GrantRule {

    /**
     * This method returns the key grant
     * @return grant access
     */
    public abstract String getGrant();

    /**
     * This method checks the permission for input action
     * @param employee employee
     * @param target input object
     * @return true if the employee has this permission
     */
    public boolean permission(Employee employee, Object target) {
        return employee.getGrants().contains(getGrant());
    }
}
