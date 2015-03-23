package com.askokov.controller;

import com.askokov.model.Ping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class is controller for ping
 */
@RestController
public class PingController {

    @RequestMapping(value = "/ping", method = RequestMethod.GET)
    public Ping ping() throws Exception {
        return new Ping(Ping.OK);
    }
}