package com.kelab.cloud.store.spec;

import com.kelab.cloud.store.model.Store;
import com.kelab.cloud.store.model.StoreStatus;
import com.kelab.cloud.user.model.ActorType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class StoreSpecification {

    public static Specification<Store> filter(
            String city,
            String name,
            StoreStatus status,
            ActorType actorType,
            Boolean active) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (city != null && !city.isBlank())
                predicates.add(cb.equal(cb.lower(root.get("city")), city.trim().toLowerCase()));

            if (name != null && !name.isBlank())
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + name.trim().toLowerCase() + "%"));

            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));

            if (actorType != null)
                predicates.add(cb.equal(root.get("owner").get("actorType"), actorType));

            if (active != null)
                predicates.add(cb.equal(root.get("active"), active));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}