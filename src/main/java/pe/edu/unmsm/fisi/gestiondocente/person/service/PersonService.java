package pe.edu.unmsm.fisi.gestiondocente.person.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.unmsm.fisi.gestiondocente.person.entity.Person;
import pe.edu.unmsm.fisi.gestiondocente.person.repository.PersonRepository;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    public Person getUser(Long id){
        return personRepository.findById(id).orElse(null);
    }
}
