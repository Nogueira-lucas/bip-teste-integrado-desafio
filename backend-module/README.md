# backend-module

API REST Spring Boot para gerenciamento de benefícios. Expõe endpoints CRUD e transferência de saldo, delegando a lógica de negócio ao `ejb-module`.

---

## Tecnologias

| Dependência | Versão | Finalidade |
|---|---|---|
| Spring Boot | 3.2.5 | Framework principal |
| Spring Data JPA | (BOM) | Persistência via repositório |
| PostgreSQL | (BOM) | Banco de dados em produção |
| H2 | (BOM) | Banco em memória para testes |
| springdoc-openapi | 2.5.0 | Swagger UI / OpenAPI 3 |
| Log4j2 | (BOM) | Logging (substitui Logback) |
| Lombok | (BOM) | Redução de boilerplate |
| ejb-module | 0.0.1-SNAPSHOT | Entidade `Beneficio` e serviço EJB |

---

## Estrutura de pacotes

```
src/main/java/com/lucasnogueira/backend/
├── BackendApplication.java          # Ponto de entrada Spring Boot
├── config/
│   └── EjbConfig.java               # Registra BeneficioEjbService como bean Spring
├── controller/
│   └── BeneficioController.java     # Endpoints REST /api/v1/beneficios
├── handler/
│   └── GlobalExceptionHandler.java  # Tratamento centralizado de exceções (ProblemDetail)
├── repository/
│   └── BeneficioRepository.java     # JpaRepository<Beneficio, Long>
└── service/
    └── BeneficioService.java        # Lógica de aplicação + delegação ao ejb-module
```

---

## Endpoints

Base URL: `http://localhost:8080/api/v1/beneficios`

| Método | Caminho | Descrição | Status de sucesso |
|--------|---------|-----------|-------------------|
| `GET` | `/` | Lista todos os benefícios ativos | `200 OK` |
| `GET` | `/{id}` | Busca benefício por ID | `200 OK` |
| `POST` | `/` | Cria novo benefício | `201 Created` + header `Location` |
| `PUT` | `/{id}` | Atualiza benefício existente | `200 OK` |
| `DELETE` | `/{id}` | Inativa benefício (soft delete) | `200 OK` |
| `POST` | `/transferencia` | Transfere saldo entre dois benefícios | `200 OK` |

### Corpo da transferência

```json
{
  "fromId": 1,
  "toId": 2,
  "valor": "300.00"
}
```

### Tratamento de erros

As exceções são tratadas globalmente via `GlobalExceptionHandler` e retornam o formato `ProblemDetail` (RFC 9457):

| Exceção | Status HTTP | Título |
|---------|-------------|--------|
| `NoSuchElementException` | `404 Not Found` | Recurso não encontrado |
| `IllegalArgumentException` | `400 Bad Request` | Erro de validação |
| `Exception` (genérica) | `500 Internal Server Error` | Erro interno |

---

## Configuração

Arquivo: `src/main/resources/application.properties`

```properties
# DataSource
spring.datasource.url=jdbc:postgresql://localhost:5432/bipdb
spring.datasource.username=bip
spring.datasource.password=bip123

# JPA — valida schema sem alterar o banco
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

O banco deve estar rodando antes de iniciar a aplicação. Veja a seção de banco no [README raiz](../README.md#banco-de-dados).

---

## Integração com ejb-module

`EjbConfig` registra `BeneficioEjbService` como bean Spring. O `PersistenceAnnotationBeanPostProcessor` detecta a anotação `@PersistenceContext` na classe e injeta o `EntityManager` gerenciado pelo Spring — garantindo que as operações do EJB participem da transação aberta via `@Transactional` no `BeneficioService`.

```
BeneficioController
    └── BeneficioService (@Transactional)
            ├── BeneficioRepository (Spring Data JPA)
            └── BeneficioEjbService (bean via EjbConfig)
                    └── EntityManager (injetado pelo Spring)
```

---

## Swagger UI

Com a aplicação rodando, acesse:

```
http://localhost:8080/swagger-ui.html
```

A especificação OpenAPI em JSON fica disponível em:

```
http://localhost:8080/v3/api-docs
```

---

## Executando

### Pré-requisitos

- Java 17+
- Banco PostgreSQL rodando (via `docker compose up -d` na raiz)
- `ejb-module` instalado no repositório local Maven

### Build e execução

```bash
# Na raiz do projeto — builda ejb-module e backend-module em ordem
mvn clean install

# Executar o fat JAR
java -jar backend-module/target/backend-module-0.0.1-SNAPSHOT.jar
```

---

## Testes

Os testes rodam sem banco de dados real — usam Mockito para isolar dependências.

| Classe | Estratégia | O que cobre |
|--------|-----------|-------------|
| `BeneficioServiceTest` | Mockito (unitário) | CRUD: listar, buscar, criar, atualizar, inativar, transferir |
| `BeneficioControllerTest` | MockMvc standalone | Status HTTP, corpo JSON, header `Location` |

### Executar os testes

```bash
# Apenas este módulo (ejb-module já deve estar instalado)
mvn test -pl backend-module

# Com build do ejb-module antes
mvn install -pl ejb-module && mvn test -pl backend-module

# Classe específica
mvn test -pl backend-module -Dtest=BeneficioServiceTest

# Método específico
mvn test -pl backend-module -Dtest="BeneficioServiceTest#deletar_deveLancarExcecao_quandoNaoExistir"
```

### Relatório de cobertura (JaCoCo)

```bash
mvn install -pl ejb-module && mvn verify -pl backend-module
```

Relatório HTML: `backend-module/target/site/jacoco/index.html`
