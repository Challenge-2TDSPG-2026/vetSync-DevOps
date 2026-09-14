# VetSync

API REST para continuidade do cuidado e engajamento na jornada de saúde do pet. A solução permite que tutores acompanhem seus pets e eventos de saúde, enquanto veterinários e administradores gerenciam agenda, tratamentos, prescrições, pontos e recompensas.

Projeto do FIAP Challenge 2026 - parceria Clyvo Vet - Java Advanced, 2º ano ADS.

## Integrantes

| Nome | RM | Turma |
|---|---|---|
| Arthur Brito | RM 562085 | 2TDS |
| Luiz Felipe Flosi | RM 563197 | 2TDS |
| Pedro Brum | RM 561780 | 2TDS |

- **Repositório GitHub:** [https://github.com/Challenge-2TDSPG-2026/vetSync-DevOps.git](https://github.com/Challenge-2TDSPG-2026/vetSync-DevOps.git)
- **Vídeo Demonstrativo no YouTube:** `[INSERIR_LINK_DO_YOUTUBE_AQUI]`


## Problema e solução

O acompanhamento da saúde do pet costuma ficar distribuído entre mensagens, anotações e consultas sem histórico centralizado. O VetSync centraliza o cadastro do pet, os eventos de saúde, o atendimento veterinário, os planos de tratamento e o programa de pontos em uma API protegida.

### Benefícios para o negócio

- mantém o histórico de saúde do pet persistido e consultável;
- reduz falhas de comunicação entre tutor e clínica;
- organiza a agenda e evita conflito de horários do veterinário;
- permite acompanhar tratamentos, prescrições e alertas de continuidade do cuidado;
- estimula o cuidado preventivo com pontos e recompensas;
- fornece autenticação, autorização por perfil e controle de posse dos recursos.

## Arquitetura da solução

A entrega utiliza a Opção 2 da disciplina DevOps Tools & Cloud Computing: Azure App Service com banco PaaS. App e banco não são containerizados. Todos os recursos de produção são criados por Azure CLI nos scripts deste repositório.

```text
Tutor / Veterinário / Administrador / Postman / navegador
                         |
                         | HTTPS / REST / JWT
                         v
              Azure App Service Linux
                 Java 17 / Spring Boot
                         |
                         | JDBC + Flyway
                         v
                 Azure SQL Database
                         ^
                         |
       Azure Key Vault <- Managed Identity
          (senha do banco)
```

Fluxo de publicação:

```text
1. Clone do GitHub
2. Azure CLI cria Resource Group, Key Vault, App Service e Azure SQL
3. Maven executa build e testes
4. App Service recebe o JAR
5. Spring Boot inicia e Flyway aplica as migrations
6. Usuários acessam a API pública e os dados são persistidos no Azure SQL
```

### Diagrama de arquitetura

O diagrama abaixo apresenta as personas, os componentes da aplicação, os recursos Azure, as relações entre App Service Plan, App Service, SQL Server, Azure SQL Database e Key Vault, além do fluxo de acesso à API.

![Diagrama de arquitetura e infraestrutura do VetSync](docs/images/DevOps-S2Spr3.drawio.png)

## Stack

- Java 17 e Spring Boot 3.3.5;
- Spring Web, Spring Data JPA, Spring Validation, Spring Security e Actuator;
- SQL Server / Azure SQL com Microsoft JDBC Driver;
- Flyway para versionamento do schema;
- JWT para autenticação e autorização;
- Springdoc OpenAPI/Swagger;
- JUnit 5, Spring Security Test, Mockito e JaCoCo;
- H2 somente para os testes automatizados. O banco da aplicação entregue é Azure SQL, nunca H2.

## Estrutura do repositório

```text
.
├── script/
│   ├── 1-system.sh             # Resource Group, Key Vault, App Service e plano
│   ├── 2-sqlserver.sh          # Azure SQL Server, banco e firewall
│   ├── 3-backend-deploy.sh     # Testes, configurações e deploy do JAR
│   └── x-delete.sh             # Exclusão do Resource Group
├── script/Database/
│   └── script_bd.sql           # DDL completo para SQL Server / Azure SQL
├── src/main/java/              # Controllers, serviços, entidades e segurança
├── src/main/resources/
│   ├── application.properties
│   ├── db/migration/            # Flyway V1 a V10
│   └── static/index.html        # Console web/API tester
├── documentos/                 # Coleção Postman e documentação auxiliar
├── pom.xml
└── mvnw
```

## Pré-requisitos

- Java 17 ou superior;
- Azure CLI instalada e autenticada com `az login`;
- assinatura Azure selecionada:

  ```bash
  az account set --subscription "<ID_OU_NOME_DA_ASSINATURA>"
  az account show
  ```

- permissão para criar Resource Group, App Service, Azure SQL e Key Vault;
- `openssl`, `curl`, `git` e `chmod`.

O script usa a região `chilecentral`. Se essa região não estiver disponível na assinatura, altere a variável `LOCATION` nos scripts antes da execução e mantenha a mesma região nos recursos.

## Como Executar o Projeto (Deploy no Azure)

Siga rigorosamente o passo a passo abaixo para reproduzir a infraestrutura e o deploy da aplicação em nuvem:

### 1. Clonar o repositório
```bash
git clone https://github.com/Challenge-2TDSPG-2026/vetSync-DevOps.git
```

### 2. Entrar na pasta do projeto
```bash
cd vetSync-DevOps
```

### 3. Conceder permissão de execução (`chmod`) nos scripts
```bash
chmod +x mvnw script/*.sh
```

### 4. Execução do 1º e 2º script
Execute os dois primeiros scripts para provisionar o Resource Group, Key Vault, App Service, SQL Server, banco de dados `db-vetsync` e as regras de firewall:
```bash
./script/1-system.sh
./script/2-sqlserver.sh
```

### 5. Inserir o `.sql` no Query Editor (Azure Portal)
Para garantir a estrutura de tabelas inicial do banco de dados na nuvem antes do deploy:
1. Acesse o [Portal do Azure](https://portal.azure.com);
2. Navegue até o Resource Group `rg-vetsync` -> Banco de dados SQL `db-vetsync`;
3. No menu lateral esquerdo, clique em **Editor de consultas (versão prévia)** / **Query editor**;
4. Autentique-se com:
   - **Tipo de autorização:** Autenticação do SQL Server
   - **Login:** `vetsync-adm`
   - **Senha:** consulte a senha gerada no Key Vault com o comando:
     ```bash
     az keyvault secret show --vault-name kv-vetsync-rm563197 --name sql-admin-password --query value -o tsv
     ```
5. Abra o arquivo [`script/Database/script_bd.sql`](script/Database/script_bd.sql), copie todo o seu conteúdo DDL, cole no editor e clique em **Executar (Run)** para criar as tabelas e constraints.

### 6. Executar o 3º script
Com o banco preparado, execute o script de validação de testes, configuração de identidade gerenciada e deploy do artefato:
```bash
./script/3-backend-deploy.sh
```

Os scripts criam e configuram, integralmente via Azure CLI (sem containers):

1. **`1-system.sh`**:
   - Resource Group `rg-vetsync` na região `chilecentral`;
   - Azure Key Vault `kv-vetsync-rm563197` com autorização RBAC habilitada;
   - Permissão `Key Vault Secrets Officer` atribuída ao usuário autenticado na CLI;
   - App Service Plan Linux `plan-vetsync` com SKU `B1`;
   - Azure App Service Web App Linux `app-vetsync-rm563197` com runtime `JAVA:17-java17`.

2. **`2-sqlserver.sh`**:
   - Geração de senha criptográfica forte via `openssl`;
   - Armazenamento seguro da senha no Azure Key Vault no segredo `sql-admin-password`;
   - Servidor PaaS `sql-server-vetsync-chilecentral` com usuário administrador `vetsync-adm`;
   - Banco de Dados PaaS `db-vetsync` no tier `Basic` (sem container);
   - Regra de firewall liberando o IP local do desenvolvedor/avaliador (`allow-local-development`);
   - Regras de firewall para todos os possíveis IPs de saída do App Service (`possibleOutboundIpAddresses`);
   - Configuração de connection string no Web App.

3. **`3-backend-deploy.sh`**:
   - Execução do build e dos testes automatizados via `./mvnw clean verify`;
   - Identidade Gerenciada (System-assigned Managed Identity) habilitada no Web App;
   - Atribuição do papel `Key Vault Secrets User` à Managed Identity no escopo do Key Vault;
   - Configuração das Application Settings no Web App com referência segura ao Key Vault (`@Microsoft.KeyVault(...)`) para a senha do banco;
   - Deploy do pacote JAR executável via `az webapp deploy --type jar`.

A URL pública do App Service na nuvem é:

```text
https://app-vetsync-rm563197.azurewebsites.net
```

Após a inicialização do App Service, o Flyway aplica automaticamente as migrations de schema `V1` a `V10`.

Para remover completamente todos os recursos criados após a avaliação:

```bash
./script/x-delete.sh
```

---

## Roteiro sequencial para gravação do vídeo (Passo a passo)

O vídeo é a prova da entrega da Sprint 3 (até 80 pontos). Siga este roteiro rigorosamente:

> [!IMPORTANT]
> **Regras obrigatórias da FIAP para o vídeo:**
> - Qualidade mínima de **720p**, áudio claro com **explicação por voz** (sem legendas);
> - É **proibido** utilizar `localhost` (resulta em nota ZERO);
> - O vídeo deve começar pelo **clone do repositório no GitHub**;
> - **SEM CORTES** durante a demonstração dos testes de CRUD e da persistência no banco de dados;
> - Não expor senhas reais, tokens ou arquivos `.env`.

### Sequência recomendada para a gravação:

1. **Abertura (1 minuto):**
   - Apresente os integrantes do grupo (nome e RM);
   - Explique brevemente o projeto VetSync e a escolha da **Opção 2: Azure App Service com Banco PaaS (sem containers)**.

2. **Clone e Deploy via CLI (Obrigatório começar assim):**
   - Com o terminal aberto e já autenticado na Azure CLI (`az login` e `az account show`), execute:
     ```bash
     # 1. Clone
     git clone https://github.com/Challenge-2TDSPG-2026/vetSync-DevOps.git

     # 2. Entrar na pasta
     cd vetSync-DevOps

     # 3. chmod nos scripts
     chmod +x mvnw script/*.sh

     # 4. Rodagem do 1º e 2º script
     ./script/1-system.sh
     ./script/2-sqlserver.sh
     ```
   - **5. Inserir script_bd.sql no Query Editor:** Acesse o Portal do Azure -> Banco `db-vetsync` -> Query editor, faça login como `vetsync-adm` com a senha do Key Vault, cole o DDL de `script/Database/script_bd.sql` e execute para provisionar o schema.
   - **6. Rodar o 3º script:**
     ```bash
     ./script/3-backend-deploy.sh
     ```
     Comente o que cada script faz enquanto os comandos são executados.

3. **Evidência dos recursos no Portal do Azure:**
   - Acesse o Portal do Azure e abra o Resource Group `rg-vetsync`;
   - Mostre os recursos criados:
     - **App Service Plan:** `plan-vetsync` (Linux, SKU B1);
     - **Web App:** `app-vetsync-rm563197` (Java 17);
     - **Azure Key Vault:** `kv-vetsync-rm563197` com o segredo `sql-admin-password`;
     - **SQL Server:** `sql-server-vetsync-chilecentral`;
     - **Azure SQL Database:** `db-vetsync` (PaaS);
   - Mostre a aba *Configuração / Application Settings* do Web App demonstrando a integração com o Key Vault via Managed Identity.

4. **Health Check da Aplicação Pública:**
   - No navegador, acesse a URL pública:
     ```text
     https://app-vetsync-rm563197.azurewebsites.net/actuator/health
     ```
   - Mostre o status `UP` comprovando que a API está rodando e conectada ao Azure SQL.

5. **Demonstração do CRUD e Persistência no Azure SQL (SEM CORTES):**
   - Mantenha duas abas no navegador abertas lado a lado:
     - **Aba 1 (Aplicação):** Console Web `/index.html` ou Swagger UI `/swagger-ui.html`;
     - **Aba 2 (Banco de Dados):** Portal do Azure -> Banco `db-vetsync` -> **Editor de Consultas (Query Editor)**;
   - Realize as operações do CRUD para **duas linhas de conteúdo significativo** nas tabelas CORE (`TB_PET` e `TB_EVENTO_SAUDE`) conforme o roteiro detalhado na seção a seguir;
   - A cada operação na interface/API, execute o `SELECT` no Editor de Consultas demonstrando a persistência imediata.

6. **Fechamento:**
   - Mencione o script `./script/x-delete.sh` para exclusão dos recursos da Azure e finalize a apresentação.

---

## Banco de dados e migrations

O DDL completo, com tabelas, colunas, chaves estrangeiras, restrições e comentários, está em [`script/Database/script_bd.sql`](script/Database/script_bd.sql). Ele representa o estado final das migrations e deve ser executado somente em um banco SQL Server/Azure SQL vazio quando for necessária a criação manual do schema.

Na execução normal, o schema é criado e versionado automaticamente pelo Flyway em `src/main/resources/db/migration`. O DDL não contém dados de teste nem credenciais.

As tabelas CORE da demonstração são:

- `TB_PET`, que representa o pet acompanhado pelo tutor;
- `TB_EVENTO_SAUDE`, relacionada a `TB_PET` por `TB_EVENTO_SAUDE.id_pet`.

---

## CRUD completo e persistência (Roteiro com 2 linhas)

Para atender à exigência de **pelo menos 2 linhas com conteúdo significativo** manipuladas e comprovadas por `SELECT` no Azure SQL, siga o roteiro abaixo.

### Como acessar o Editor de Consultas no Azure:
1. No Portal do Azure, vá em **Resource Groups** -> `rg-vetsync` -> Banco `db-vetsync`;
2. No menu lateral esquerdo, clique em **Editor de consultas (versão prévia)** (*Query editor*);
3. Em *Tipo de autorização*, escolha **Autenticação do SQL Server**;
4. Usuário: `vetsync-adm`;
5. Senha: A senha gerada no deploy (para consultar no terminal: `az keyvault secret show --vault-name kv-vetsync-rm563197 --name sql-admin-password --query value -o tsv`);
6. Clique em **OK**.

---

### Dados das 2 linhas de negócio para o teste:

* **Tutor de teste:** Login na aplicação com `maria@email.com` / `senha123` (ou cadastro via `/auth/registrar`).
* **Linha 1:**
  - Pet: `Thor` | Espécie: `Cão` | Raça: `Golden Retriever` | Sexo: `M` | Peso: `32.50` kg
  - Evento: `Consulta de rotina` | Veterinário: `Dra. Ana Costa` | Status: `AGENDADO`
* **Linha 2:**
  - Pet: `Luna` | Espécie: `Gato` | Raça: `Siamês` | Sexo: `F` | Peso: `4.20` kg
  - Evento: `Vacina` | Veterinário: `Dra. Ana Costa` | Status: `AGENDADO`

---

### 1. Inclusão (Create)
Cadastre os dois pets e agende um evento para cada um via Console Web (`/index.html`), Swagger UI ou API:

* **Pet 1:** `POST /pets` com `nmPet: "Thor"`, `especie: "CAO"`, `raca: "Golden Retriever"`, `peso: 32.50`, `sexo: "M"`, `dtNascimento: "2023-01-15"`
* **Pet 2:** `POST /pets` com `nmPet: "Luna"`, `especie: "GATO"`, `raca: "Siamês"`, `peso: 4.20`, `sexo: "F"`, `dtNascimento: "2022-06-20"`
* **Evento 1:** `POST /eventos` vinculando ao pet `Thor` com tipo de evento `Consulta de rotina`
* **Evento 2:** `POST /eventos` vinculando ao pet `Luna` com tipo de evento `Vacina`

**Evidência no Azure SQL (rodar no Query Editor):**
```sql
-- Evidência de inclusão das 2 linhas de Pet:
SELECT id_pet, nm_pet, nr_peso_kg, ds_sexo, id_tutor 
FROM TB_PET 
WHERE nm_pet IN ('Thor', 'Luna');

-- Evidência de inclusão dos 2 Eventos relacionados:
SELECT id_evento, id_pet, id_tipo_evento, id_veterinario, ds_status, dt_evento, hr_evento 
FROM TB_EVENTO_SAUDE;
```

---

### 2. Consulta (Read)
Consulte os registros na aplicação e confira com os dados retornados no banco:

* **Pela Aplicação:**
  - `GET /pets` (ou aba "Meus pets" na console web) -> lista `Thor` e `Luna`;
  - `GET /eventos` (ou aba "Meus eventos") -> lista os eventos agendados de cada pet.
* **No Banco:** Os registros batem exatamente com o resultado da consulta `SELECT` acima.

---

### 3. Alteração (Update)
Altere um dado do Pet 1 e conclua o Evento 1:

* **Alterar Pet 1:** `PUT /pets/{idPet1}` -> atualizar o peso de `32.50` para `34.00` kg (ex: ganho de peso pós-tratamento).
* **Concluir Evento 1:** `PATCH /eventos/{idEvento1}/concluir` -> com `vlCusto: 150.00` e `dsObservacao: "Consulta concluída, animal saudável e vacinas em dia."`.

**Evidência no Azure SQL (rodar no Query Editor):**
```sql
-- Evidência da alteração do Pet (novo peso persistido):
SELECT id_pet, nm_pet, nr_peso_kg 
FROM TB_PET 
WHERE nm_pet = 'Thor';

-- Evidência da alteração do Evento (status CONCLUIDO, custo e observação):
SELECT id_evento, id_pet, ds_status, vl_custo, ds_observacao 
FROM TB_EVENTO_SAUDE 
WHERE id_evento = 1; -- (ou ID do evento de Thor)
```

---

### 4. Exclusão (Delete)
Exclua o Evento 2 e em seguida o Pet 2 para demonstrar o ciclo de exclusão com integridade referencial:

* **Excluir Evento 2:** `DELETE /eventos/{idEvento2}`
* **Excluir Pet 2:** `DELETE /pets/{idPet2}`

**Evidência no Azure SQL (rodar no Query Editor):**
```sql
-- Evidência da exclusão do Evento 2 (retorna 0 linhas):
SELECT id_evento, ds_status 
FROM TB_EVENTO_SAUDE 
WHERE id_pet = (SELECT id_pet FROM TB_PET WHERE nm_pet = 'Luna');

-- Evidência da exclusão do Pet 2 (retorna 0 linhas):
SELECT id_pet, nm_pet 
FROM TB_PET 
WHERE nm_pet = 'Luna';

-- Confirmação final das tabelas mantendo apenas o Pet 1 e seu Evento 1 concluído:
SELECT id_pet, nm_pet, nr_peso_kg FROM TB_PET;
SELECT id_evento, id_pet, ds_status, vl_custo FROM TB_EVENTO_SAUDE;
```

Com esta sequência, demonstra-se:
1. Inclusão de 2 linhas significativas em ambas as tabelas;
2. Consulta de ambas as linhas via API e via SQL;
3. Atualização com persistência comprovada no banco;
4. Exclusão com integridade referencial demonstrada;
5. Persistência real em banco PaaS na nuvem, sem uso de localhost.


## Configuração

Em produção, os valores são Application Settings do App Service. A senha utiliza referência do Key Vault.

| Variável | Finalidade |
|---|---|
| `SPRING_DATASOURCE_URL` | URL JDBC do Azure SQL |
| `SPRING_DATASOURCE_USERNAME` | usuário do Azure SQL |
| `DB_PASSWORD` | senha referenciada pelo Key Vault |
| `SPRING_FLYWAY_ENABLED` | habilita as migrations |
| `JWT_SECRET` | chave de assinatura dos tokens |
| `ADMIN_BOOTSTRAP_KEY` | chave para criar o primeiro administrador |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | configuração opcional de e-mail |

Para desenvolvimento local, não use os valores padrão de demonstração em um ambiente compartilhado. Exporte valores próprios apenas na sessão do terminal:

```bash
export SPRING_DATASOURCE_URL='jdbc:sqlserver://<HOST>:1433;databaseName=<BANCO>;encrypt=true;trustServerCertificate=false'
export SPRING_DATASOURCE_USERNAME='<USUARIO>'
export DB_PASSWORD='<SENHA>'
export JWT_SECRET='<CHAVE_LONGA>'
export ADMIN_BOOTSTRAP_KEY='<CHAVE_DE_BOOTSTRAP>'
./mvnw spring-boot:run
```

## Execução e acesso

Para testar localmente:

```bash
./mvnw spring-boot:run
```

Para a correção da Sprint 3, use a URL pública do Azure, não `localhost`.

| Recurso | Local | Azure |
|---|---|---|
| API | `http://localhost:8080` | `https://app-vetsync-rm563197.azurewebsites.net` |
| Console web | `/index.html` | `/index.html` |
| Swagger UI | `/swagger-ui.html` | `/swagger-ui.html` |
| OpenAPI | `/v3/api-docs` | `/v3/api-docs` |
| Health check | `/actuator/health` | `/actuator/health` |

## Autenticação, perfis e fluxos

O primeiro administrador é criado por `POST /admins/bootstrap`, usando `ADMIN_BOOTSTRAP_KEY`. Depois, use `POST /auth/login` para obter o JWT e envie-o como `Authorization: Bearer <TOKEN>`.

A aplicação possui perfis com permissões diferentes e protege as rotas com Spring Security. Os principais fluxos funcionais, além do CRUD, são:

1. tutor agenda um evento de saúde para seu pet e o veterinário conclui ou o tutor cancela;
2. veterinário solicita/gerencia um plano de tratamento e seus itens;
3. eventos ou planos geram pontos que podem ser usados no fluxo de recompensas.

Endpoints disponíveis e modelos de requisição/resposta:

| Recurso | Rotas principais |
|---|---|
| Autenticação | `/auth/login`, `/auth/registrar`, `/auth/logout`, `/auth/me` |
| Administradores | `/admins/bootstrap`, `/admins` |
| Pets | `/pets`, `/pets/{id}`, `/pets/tutor/{idTutor}` |
| Eventos | `/eventos`, `/eventos/{id}`, `/eventos/{id}/concluir`, `/eventos/{id}/cancelar` |
| Tratamentos | `/planos`, `/planos/{id}` |
| Prescrições | `/prescricoes`, `/prescricoes/{id}` |
| Veterinários e agenda | `/veterinarios`, `/veterinarios/{id}/disponibilidade`, `/veterinarios/{id}/bloqueios` |
| Pontos e recompensas | `/pontos`, `/recompensas`, `/recompensas/{id}/resgatar` |

Consulte a documentação completa no Swagger, que é a referência dos contratos atuais da API, e a coleção [`documentos/JornadaPet_Postman_Collection.json`](documentos/JornadaPet_Postman_Collection.json).

## Testes automatizados

Os testes seguem a estrutura do projeto Spring e cobrem controllers, serviços, repositórios, segurança, validações e fluxos de integração. Execute:

```bash
./mvnw clean verify
```

O comando executa build, testes e relatório JaCoCo. Para gerar o relatório diretamente:

```bash
./mvnw test jacoco:report
```

Os testes usam H2 em memória apenas como apoio automatizado. Isso não substitui o teste de integração, a persistência e os `SELECT`s no Azure SQL exigidos na demonstração.

---

## Entrega obrigatória da Sprint 3 (Orientações do PDF da FIAP)

Conforme os critérios de avaliação e penalidades da disciplina (DevOps Tools & Cloud Computing):

> [!CAUTION]
> **Formato restrito do arquivo PDF de entrega:**
> - O grupo deve submeter no portal da FIAP um **único arquivo PDF**;
> - O conteúdo do PDF deve conter **APENAS e EXCLUSIVAMENTE**:
>   1. **Nome completo e RM** de todos os integrantes do grupo;
>   2. **Link do repositório** público no GitHub (`https://github.com/Challenge-2TDSPG-2026/vetSync-DevOps.git`);
>   3. **Link do vídeo** demonstrativo no YouTube (não-listado ou público);
> - **Atenção:** Não coloque mais nada no PDF. Todo o detalhamento técnico e instruções devem permanecer aqui no repositório GitHub e no README.
> - *Penalidade no edital se faltar o PDF com esses dados: -30 pontos.*

### Checklist final da equipe antes do envio:
- [x] Código-fonte e scripts versionados no GitHub
- [x] Nenhuma credencial ou dado sensível exposto em código ou `.env`
- [x] Scripts Azure CLI testados (`1-system.sh`, `2-sqlserver.sh`, `3-backend-deploy.sh`)
- [x] Aplicação provisionada em Azure App Service e Azure SQL PaaS (sem containers)
- [x] Diagrama de arquitetura oficial Azure incluído no repositório
- [x] Arquivo DDL [`script/Database/script_bd.sql`](script/Database/script_bd.sql) versionado com comentários
- [ ] Vídeo gravado sem cortes no CRUD/SELECT, áudio com voz clara, qualidade >= 720p
- [ ] Vídeo publicado no YouTube (público ou não-listado)
- [ ] PDF gerado e validado com os 3 itens obrigatórios

---

*VetSync - FIAP 2026 | Challenge Clyvo Vet | 2º Ano ADS*

