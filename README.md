# bip-teste-integrado

Projeto multi-módulo composto por uma API REST (Spring Boot), serviço EJB (Jakarta EE), banco de dados relacional e frontend Angular.

## Módulos

| Módulo | Tecnologia | Descrição |
|--------|-----------|-----------|
| `backend-module` | Spring Boot 3.2.5 / Java 17 | API REST para gerenciamento de benefícios |
| `ejb-module` | Jakarta EE (EJB + JPA) | Serviço de negócio com lógica transacional |
| `db` | SQL (PostgreSQL) | Scripts de criação e carga inicial do banco |
| `frontend` | Angular | Interface web (a ser implementada) |

---

## Pré-requisitos

- Java 17+
- Maven 3.8+
- Docker

---

## Build

O projeto usa Maven multi-módulo. O build **deve ser executado sempre a partir da raiz** do projeto.

Ao rodar da raiz, o Maven processa o POM pai primeiro (instalando-o em `~/.m2`), depois builda `ejb-module` e `backend-module` na ordem declarada em `<modules>`. Rodar `mvn` de dentro de um submódulo diretamente causa erro porque o POM pai ainda não está no repositório local.

### Usando o script de build (recomendado)

```bash
chmod +x build.sh
./build.sh
```

### Comando direto (equivalente)

```bash
# Na raiz do projeto
mvn clean install
```

### Executando o backend

```bash
java -jar backend-module/target/backend-module-0.0.1-SNAPSHOT.jar
```

A API ficará disponível em `http://localhost:8080/api/v1/beneficios`.

---

## Como o ejb-module é importado no backend-module

O `ejb-module` é empacotado como um JAR Maven comum. Após o `mvn install`, ele fica disponível no repositório local (`~/.m2`) e o `backend-module` o declara como dependência no seu `pom.xml`:

```xml
<dependency>
    <groupId>com.lucasnogueira</groupId>
    <artifactId>ejb-module</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Com isso, as classes `Beneficio` (entidade JPA) e `BeneficioEjbService` ficam disponíveis no classpath do Spring Boot. O Spring gerencia o ciclo de vida e as transações — a anotação `@Stateless` serve como documentação de intenção, mas quem provê o contexto transacional é o Spring (`@Transactional`).

> **Observação:** o `pom.xml` original estava localizado dentro de `src/main/java/com/lucasnogueira/backend/`, que não é o local padrão do Maven. O arquivo correto está agora em `backend-module/pom.xml`.

---

## Banco de Dados

O banco de dados é provisionado via Docker usando PostgreSQL 16.

### Pré-requisitos

- [Docker](https://www.docker.com/) instalado e em execução

### Subindo o banco

Na raiz do projeto, execute:

```bash
docker compose up -d
```

Na primeira execução, o PostgreSQL irá:
1. Criar o banco `bipdb`
2. Executar `db/schema.sql` — cria a tabela `BENEFICIO`
3. Executar `db/seed.sql` — insere os dados iniciais

### Parando o banco

```bash
docker compose down
```

> Para remover também os dados persistidos, adicione a flag `-v`:
> ```bash
> docker compose down -v
> ```

### Dados de conexão

| Parâmetro | Valor |
|-----------|-------|
| Host | `localhost` |
| Porta | `5432` |
| Banco | `bipdb` |
| Usuário | `bip` |
| Senha | `bip123` |
| JDBC URL | `jdbc:postgresql://localhost:5432/bipdb` |
