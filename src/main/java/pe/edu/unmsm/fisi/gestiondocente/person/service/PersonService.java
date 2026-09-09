package pe.edu.unmsm.fisi.gestiondocente.person.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pe.edu.unmsm.fisi.gestiondocente.person.entity.Person;
import pe.edu.unmsm.fisi.gestiondocente.person.repository.PersonRepository;

@Service
@Profile("!test")
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Person getUser(Long id){
        return personRepository.findById(id).orElse(null);
    }
}
