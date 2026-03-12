# Co-Workers Cloud — Backend API

API REST del marketplace SaaS para la gestión de la economía circular en Colombia.  
Desarrollado por **R&R Kelab S.A.S**.

---

## Stack

- **Java 17** + **Spring Boot 3.x**
- Spring Security + JWT
- Spring Data JPA + Hibernate
- MySQL 8+
- Lombok

---

## Ejecutar localmente

```bash
# Configurar base de datos en application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/kelab_cloud
spring.datasource.username=root
spring.datasource.password=secret

jwt.secret=tu_clave_secreta
jwt.expiration=86400000

spring.jpa.hibernate.ddl-auto=update

# Levantar
./mvnw spring-boot:run
```

API disponible en `http://localhost:8080`

---

## Módulos

| Módulo      | Descripción                  |
| ----------- | ---------------------------- |
| `auth`      | Registro y login con JWT     |
| `user`      | Perfil, roles y actores      |
| `store`     | Tiendas por actor            |
| `product`   | Catálogo de productos        |
| `inventory` | Stock, movimientos y alertas |

---

## Licencia

Copyright © 2024-2025 **R&R Kelab S.A.S** — Todos los derechos reservados.  
Consulta el archivo [`LICENSE`](../LICENSE) para más detalles.
