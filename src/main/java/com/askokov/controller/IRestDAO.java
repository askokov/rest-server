package uss.coi.cop.app.controllers;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uss.coi.cop.foundation.form.FilterDTO;
import uss.coi.cop.foundation.exception.USSException;
import uss.coi.cop.foundation.form.PagingDTO;
import uss.coi.cop.foundation.models.helpers.Base;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
public interface IRestDAO<T extends Base> {

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    T getById(String id) throws USSException;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    List<T> getAll();

    @RequestMapping(value = "/", method = RequestMethod.POST)
    T create(T model) throws USSException;

    @RequestMapping(value = "/**", method = {RequestMethod.PUT} )
    T update(HttpServletRequest request) throws USSException, IOException;


    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    void delete(String id) throws USSException;

    @RequestMapping(value = "/load", method = RequestMethod.GET)
    PagingDTO<T> load(@ModelAttribute FilterDTO form);

    @RequestMapping(value = "/versions/{id}", method = RequestMethod.GET)
    PagingDTO<T> versions(String id, HttpServletRequest request) throws USSException;

    @RequestMapping(value = "/version/{modelId}/{versionId}", method = RequestMethod.GET)
    T version(String modelId, String versionId) throws USSException;
}