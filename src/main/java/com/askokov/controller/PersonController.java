package uss.coi.cop.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.*;
import uss.coi.cop.foundation.models.CrimeCase;
import uss.coi.cop.foundation.models.Person;
import uss.coi.cop.foundation.repositories.CrimeCaseRepository;
import uss.coi.cop.foundation.repositories.PersonRepository;

import java.util.List;

@RestController
@RequestMapping("/person")
public class PersonController extends CRUDRest<PersonRepository, Person> implements IRestDAO<Person> {
    @Autowired
    private PersonRepository repository;

    @Autowired
    private CrimeCaseRepository crimeCaseRepository;


    @Override
    protected CrudRepository<Person, String> getRepository() {
        return this.repository;
    }

    @RequestMapping(value = "/crimeCases/{personId}", method = RequestMethod.GET)
    public List<CrimeCase> getCrimeCases(@PathVariable String personId) {
        return this.crimeCaseRepository.findByMembersPersonKey(new Person(personId));
    }

    @RequestMapping(value = "/convictBy/{personId}", method = RequestMethod.GET)
    public List<CrimeCase> getConvictBy(@PathVariable String personId) {
        return this.crimeCaseRepository.findByConvicts(new Person(personId));
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public List<Person> searchByKey(@RequestParam("key") String key) {
        return this.repository.searchByKey(key);
    }
}