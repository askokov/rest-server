package uss.coi.cop.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uss.coi.cop.foundation.exception.USSException;
import uss.coi.cop.foundation.models.Ping;
import uss.coi.cop.foundation.repositories.OnlineRepository;
import uss.coi.cop.foundation.security.SessionManager;

/**
 * This class is controller for ping
 */
@RestController
@RequestMapping(value = "/ping")
public class PingController {
    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private OnlineRepository pingRepository;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Ping ping() throws USSException {
        this.pingRepository.setOnline(this.sessionManager.getLogin());
        return new Ping(Ping.OK);
    }
}