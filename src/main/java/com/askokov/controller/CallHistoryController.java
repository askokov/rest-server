package uss.coi.cop.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uss.coi.cop.foundation.models.CallHistory;
import uss.coi.cop.foundation.models.Employee;
import uss.coi.cop.foundation.models.helpers.IConstrains;
import uss.coi.cop.foundation.repositories.CallHistoryRepository;
import uss.coi.cop.foundation.security.SessionManager;

import java.util.List;

@RestController
@RequestMapping("/callHistory")
public class CallHistoryController extends CRUDRest<CallHistoryRepository, CallHistory> implements IRestDAO<CallHistory> {

    @Autowired
    private CallHistoryRepository repository;

    @Autowired
    private SessionManager sessionManager;

    protected CrudRepository<CallHistory, String> getRepository() {
        return this.repository;
    }

    @RequestMapping(value = "/income", method = RequestMethod.GET)
    public List<CallHistory> getIncomeCallByEmployee() {
        return this.repository.findByToAndCallStatus(this.sessionManager.getCurrentEmployee(), IConstrains.CallStatus.INITIATED);
    }

    @RequestMapping(value = "/participated", method = RequestMethod.GET)
    public List<CallHistory> getParticipatedCalls() {
        Employee current = this.sessionManager.getCurrentEmployee();
        return this.repository.findByFromOrTo(current, current);
    }
}