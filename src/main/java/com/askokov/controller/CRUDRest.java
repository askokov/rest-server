package uss.coi.cop.app.controllers;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import uss.coi.cop.foundation.form.FilterDTO;
import uss.coi.cop.foundation.exception.USSException;
import uss.coi.cop.foundation.form.PagingDTO;
import uss.coi.cop.foundation.models.helpers.Base;
import uss.coi.cop.foundation.models.helpers.Document;
import uss.coi.cop.foundation.repositories.FinderRepository;
import uss.coi.cop.foundation.repositories.versions.api.IVersionRepository;
import uss.coi.cop.foundation.security.SessionManager;

import static org.apache.commons.collections.IteratorUtils.toList;
import static uss.coi.cop.foundation.utils.JsonUtils.convert2Object;
import static uss.coi.cop.foundation.utils.JsonUtils.getFieldsFromClass;
import static uss.coi.cop.foundation.utils.JsonUtils.merge;
import static uss.coi.cop.foundation.utils.StringUtils.convert2String;
import static uss.coi.cop.foundation.utils.StringUtils.getIntegerOrNull;

public abstract class CRUDRest<E extends CrudRepository<T, String>, T extends Base> implements IRestDAO<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CRUDRest.class);

    @Autowired
    private FinderRepository finder;

    @Autowired
    protected SessionManager sessionManager;

    protected abstract CrudRepository<T, String> getRepository();

    private final Class<T> cl;

    @SuppressWarnings("unchecked")
    public CRUDRest() {
        this.cl = ((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1]);
    }

    public T getById(@PathVariable String id) throws USSException {
        return finder.findOne(id, cl);
    }

    @SuppressWarnings("unchecked")
    public List<T> getAll() {
        return toList(getRepository().findAll().iterator());
    }

    @SuppressWarnings("unchecked")
    public T create(@RequestBody T model) throws USSException {
        //skip the client side id
        model.setId(null);
        if (getRepository() instanceof IVersionRepository) {
            ((IVersionRepository) getRepository()).create((Document) model, null, this.sessionManager.getCurrentEmployee());
        } else {
            getRepository().save(model);
        }
        return model;
    }

    @SuppressWarnings("unchecked")
    public T update(HttpServletRequest request) throws USSException, IOException {
        String json = convert2String(request.getReader());
        T updater = convert2Object(json, this.cl);
        updater = merge(finder.findOne(updater.getId(), cl), updater, json);
        if (getRepository() instanceof IVersionRepository) {
            ((IVersionRepository) getRepository()).update((Document) updater, null, this.sessionManager.getCurrentEmployee());
        } else {
            getRepository().save(updater);
        }
        return updater;
    }

    public void delete(@PathVariable String id) throws USSException {
        getRepository().delete(id);
    }

    @SuppressWarnings("unchecked")
    public PagingDTO<T> load(@ModelAttribute FilterDTO form) {
        long total = this.getRepository().count();
        List<T> lists = finder.findByFilter(form, this.cl);
        List<String> rows = (form.getFields() == null || form.getFields().length == 0) ?  getFieldsFromClass(this.cl) : Arrays.asList(form.getFields());
        return new PagingDTO<T>(new PagingDTO.MetaData("results", "rows", "id", rows), total, lists, form.getLimit(), form.getStart());
    }

    @SuppressWarnings("unchecked")
    public PagingDTO<T> versions(@PathVariable String id, HttpServletRequest request) throws USSException {
        List<T> lists;
        Integer limit = null, start = null, total = 0;
        if (getRepository() instanceof IVersionRepository) {
            lists = ((IVersionRepository) this.getRepository()).getVersionById(id).getVersions();
            total = lists.size();
            limit = getIntegerOrNull(request.getParameter("limit"));
            start = getIntegerOrNull(request.getParameter("start"));
            if (limit != null && start != null && total >= (start + limit)){
                lists = lists.subList(start, start + limit);
            }
        } else {
            lists = new ArrayList<>();
        }
        return new PagingDTO<T>(new PagingDTO.MetaData("results", "rows", "id", getFieldsFromClass(this.cl)), total, lists, limit, start);
    }

    public T version(@PathVariable String modelId, @PathVariable String versionId) throws USSException {
        if (getRepository() instanceof IVersionRepository) {
            for (T version : (List<T>) ((IVersionRepository) this.getRepository()).getVersionById(modelId).getVersions()) {
                if (versionId.equals(version.getId())) {
                    return version;
                }
            }
            throw new USSException("The version " + cl + "[id="+versionId+"] could not be found.");
        } else {
            throw new USSException("The model " + cl + " is not supported versions");
        }
    }
}