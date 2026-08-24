package pe.edu.unmsm.fisi.gestiondocente.person.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.unmsm.fisi.gestiondocente.person.entity.Person;
import pe.edu.unmsm.fisi.gestiondocente.person.service.PersonService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class PersonController {

    private final PersonService personService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String getUserById(@PathVariable Long id){
        return "Autorizado como Admin";
    }
//    public Person getUserById(@PathVariable Long id){
//        return personService.getUser(id);
//    }

}
