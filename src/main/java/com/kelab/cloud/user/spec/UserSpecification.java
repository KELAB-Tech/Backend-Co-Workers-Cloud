package com.kelab.cloud.user.spec;

import com.kelab.cloud.user.model.ActorType;
import com.kelab.cloud.user.model.TipoPersona;
import com.kelab.cloud.user.model.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> filter(
            String name,
            String email,
            TipoPersona tipoPersona,
            ActorType actorType,
            Boolean enabled) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank())
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + name.trim().toLowerCase() + "%"));

            if (email != null && !email.isBlank())
                predicates.add(cb.like(cb.lower(root.get("email")),
                        "%" + email.trim().toLowerCase() + "%"));

            if (tipoPersona != null)
                predicates.add(cb.equal(root.get("tipoPersona"), tipoPersona));

            if (actorType != null)
                predicates.add(cb.equal(root.get("actorType"), actorType));

            if (enabled != null)
                predicates.add(cb.equal(root.get("enabled"), enabled));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}