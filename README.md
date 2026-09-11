# VetSync

API REST para continuidade do cuidado e engajamento na jornada de saúde do pet.

Projeto do FIAP Challenge 2026 — parceria Clyvo Vet — Java Advanced, 2º ano ADS.

## Integrantes

| Nome | RM |
|---|---|
| Arthur Brito | RM 562085 |
| Luiz Felipe Flosi | RM 563197 |
| Pedro Brum | RM 561780 |

## Sobre o projeto

O VetSync permite que tutores acompanhem pets e eventos de saúde, enquanto veterinários e administradores controlam agenda, tratamentos, prescrições, pontos e recompensas.

A API possui autenticação JWT e autorização por perfil e por posse do recurso. O projeto também inclui um console web em `/index.html` e documentação Swagger.

## Arquitetura de produção

```text
Cliente / navegador / Postman
             |
             | HTTPS
             v
Azure App Service (Java 17 / Spring Boot)
       |                    |
       | JDBC               | Managed Identity
       v                    v
Azure SQL Database     Azure Key Vault
                       (senha do banco)
```

A solução utiliza o modelo Azure App Service + Azure SQL Database. Não há containerização no deployment oficial.

## Estrutura relevante

```text
.
├── script/
│   ├── 1-system.sh             # Resource Group, Key Vault, App Service e plano
│   ├── 2-sqlserver.sh          # Azure SQL Server, banco e regras de firewall
│   ├── 3-backend-deploy.sh     # Testes, configuração e deploy do JAR
│   └── x-delete.sh             # Exclusão do Resource Group
├── script/Database/
│   └── script_bd.sql           # DDL completo para SQL Server / Azure SQL
├── src/main/java/              # Controllers, services, entidades e segurança
├── src/main/resources/
│   ├── application.properties
│   ├── db/migration/            # Migrations Flyway V1 a V10
│   └── static/index.html        # Console web/API tester
├── documentos/                 # Coleção Postman e documentação auxiliar
├── pom.xml
└── mvnw
```

## Stack

- Java 17 e Spring Boot 3.3.5
- Spring Web, Data JPA, Security, Validation e Actuator
- SQL Server / Azure SQL com driver Microsoft JDBC
- Flyway para versionamento do schema
- JWT, Springdoc OpenAPI, Spring Mail e Lombok
- JUnit 5, Spring Security Test, H2 para os testes automatizados e JaCoCo

## Pré-requisitos

- Java 17+
- Azure CLI autenticada com `az login`
- Assinatura Azure selecionada com `az account set --subscription <ID_OU_NOME>`
- Permissões para criar App Service, Azure SQL e Key Vault
- `openssl` e `curl`

## Deploy no Azure

Clone o repositório e execute os scripts na ordem indicada:

```bash
git clone <URL_DO_REPOSITORIO>
cd <DIRETORIO_DO_REPOSITORIO>
chmod +x script/*.sh

./script/1-system.sh
./script/2-sqlserver.sh
./script/3-backend-deploy.sh
```

Os scripts criam os seguintes recursos:

1. Resource Group `rg-vetsync`, plano Linux F1, Web App Java 17 e Key Vault.
2. Azure SQL Server, banco `db-vetsync`, senha administrativa armazenada no Key Vault e regras de firewall para o computador local e o App Service.
3. Identidade gerenciada para o Web App, permissão de leitura no Key Vault, variáveis da aplicação, execução dos testes e publicação do JAR.

Ao final, a aplicação ficará disponível em:

```text
https://app-vetsync-rm563197.azurewebsites.net
```

O primeiro deploy aplica automaticamente as migrations Flyway. A senha do banco não deve ser colocada no código, no README ou no vídeo; ela é recuperada pelo App Service por referência ao Key Vault.

Para excluir os recursos criados pelo exercício:

```bash
./script/x-delete.sh
```

Esse comando remove todo o Resource Group `rg-vetsync`.

## Banco de dados

O DDL completo está em [`script/Database/script_bd.sql`](script/Database/script_bd.sql). Ele deve ser executado em um banco SQL Server/Azure SQL vazio quando for necessário criar o schema manualmente.

Na execução normal da aplicação, o Flyway utiliza as migrations em `src/main/resources/db/migration`. O `script_bd.sql` representa o estado final dessas migrations e não contém dados de teste nem credenciais.

## Configuração

Em produção, configure os valores sensíveis como Application Settings do App Service ou como referências do Key Vault:

| Variável | Finalidade |
|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC do Azure SQL |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco |
| `DB_PASSWORD` | Senha do banco, referenciada pelo Key Vault |
| `SPRING_FLYWAY_ENABLED` | Habilita as migrations |
| `JWT_SECRET` | Chave de assinatura dos tokens |
| `ADMIN_BOOTSTRAP_KEY` | Chave para criar o primeiro administrador |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | Configuração opcional de e-mail |

Não publique senhas, tokens, chaves reais ou arquivos `.env` no GitHub.

## Execução local

Para executar localmente, é necessário ter uma instância SQL Server acessível e informar as variáveis de conexão:

```bash
export SPRING_DATASOURCE_URL='jdbc:sqlserver://<HOST>:1433;databaseName=<BANCO>;encrypt=true;trustServerCertificate=false'
export SPRING_DATASOURCE_USERNAME='<USUARIO>'
export DB_PASSWORD='<SENHA>'
export JWT_SECRET='<CHAVE_LONGA>'
export ADMIN_BOOTSTRAP_KEY='<CHAVE_DE_BOOTSTRAP>'

./mvnw spring-boot:run
```

URLs úteis:

| Recurso | URL |
|---|---|
| API | `http://localhost:8080` |
| Console web | `http://localhost:8080/index.html` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Health check | `http://localhost:8080/actuator/health` |

Os endereços `localhost` acima são somente para desenvolvimento local. A demonstração da Sprint 3 deve utilizar a URL pública do Azure.

## Fluxo inicial e CRUD

O primeiro administrador é criado por `POST /admins/bootstrap`, usando a chave configurada em `ADMIN_BOOTSTRAP_KEY`. Depois, o login é realizado em `POST /auth/login`.

Para a evidência da Sprint 3, demonstre operações persistidas em duas tabelas relacionadas, por exemplo:

- criar e consultar um pet em `TB_PET`;
- criar, atualizar, consultar e excluir um evento relacionado em `TB_EVENTO_SAUDE`;
- executar `SELECT` no Azure SQL depois das operações para comprovar a persistência.

Os endpoints disponíveis estão documentados no Swagger. A coleção Postman está em [`documentos/JornadaPet_Postman_Collection.json`](documentos/JornadaPet_Postman_Collection.json).

## Testes

Execute a suíte automatizada com:

```bash
./mvnw clean verify
```

Os testes usam H2 em memória e não substituem a validação da conexão com o Azure SQL. O relatório JaCoCo pode ser gerado com:

```bash
./mvnw test jacoco:report
```

## Entrega

Antes da submissão, confira se o repositório contém o código-fonte, os scripts de provisionamento, `script/Database/script_bd.sql` e este README. O PDF final deve conter somente os nomes/RMs, o link do GitHub e o link do vídeo solicitado pela atividade.

*VetSync — FIAP 2026 | Challenge Clyvo Vet | 2º Ano ADS*
