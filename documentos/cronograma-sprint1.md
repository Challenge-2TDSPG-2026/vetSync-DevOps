# Cronograma de Desenvolvimento — JornadaPet
## Sprint 1 | FIAP 2026 — Java Advanced

**Projeto:** JornadaPet — API REST para continuidade do cuidado e engajamento na jornada de saúde do pet  
**Empresa parceira:** Clyvo Vet  
**Prazo de entrega:** 24/05/2026

---

## Equipe

| Nome | RM |
|---|---|
| Arthur Brito | RM 562085 |
| Luiz Felipe Flosi | RM 563197 |
| Pedro Brum | RM 561780 |

---

## Distribuição de Responsabilidades

| Responsável | Atividade | Status |
|---|---|---|
| Arthur Brito | Modelagem das entidades (Tutor, Pet, EventoSaude) e mapeamento JPA | ✅ Concluído |
| Arthur Brito | Implementação do TutorController e TutorService (CRUD completo) | ✅ Concluído |
| Arthur Brito | Configuração do SecurityConfig e autenticação JWT (JwtService, JwtFilter) | ✅ Concluído |
| Luiz Felipe Flosi | Implementação do PetController e PetService (CRUD + paginação + filtro por espécie) | ✅ Concluído |
| Luiz Felipe Flosi | Implementação do EventoController e EventoService (CRUD + pendentes/atrasados) | ✅ Concluído |
| Luiz Felipe Flosi | Configuração do Swagger (SpringDoc OpenAPI) e documentação dos endpoints | ✅ Concluído |
| Pedro Brum | Implementação do AuthController (register/login com JWT) | ✅ Concluído |
| Pedro Brum | Configuração do banco de dados (H2 in-memory + SQL Server/Azure SQL) e MockData | ✅ Concluído |
| Pedro Brum | DER, Diagrama de Classes e documentação técnica da pasta /documentos | ✅ Concluído |
| Todos | Testes dos endpoints via Postman e exportação da coleção | ✅ Concluído |
| Todos | README com descrição, rotas, instruções de execução e integrantes | ✅ Concluído |

---

## Cronograma por Semana

### Semana 1 — 16/04 a 22/04 (Kickoff)
| Atividade | Responsável | Prazo | Status |
|---|---|---|---|
| Kickoff do Challenge com a Clyvo | Todos | 16/04 | ✅ Concluído |
| Definição do escopo e divisão de tarefas | Todos | 18/04 | ✅ Concluído |
| Criação do repositório GitHub e estrutura base do projeto Spring Boot | Pedro Brum | 20/04 | ✅ Concluído |
| Modelagem inicial das entidades (Tutor, Pet, EventoSaude) | Arthur Brito | 22/04 | ✅ Concluído |

---

### Semana 2 — 23/04 a 29/04 (Desenvolvimento Core)
| Atividade | Responsável | Prazo | Status |
|---|---|---|---|
| Implementação das entidades JPA com Bean Validation | Arthur Brito | 24/04 | ✅ Concluído |
| Configuração do banco H2 e SQL Server/Azure SQL no application.properties | Pedro Brum | 24/04 | ✅ Concluído |
| CRUD de Tutor (Controller + Service + Repository) | Arthur Brito | 26/04 | ✅ Concluído |
| CRUD de Pet com paginação e filtro por espécie | Luiz Felipe Flosi | 28/04 | ✅ Concluído |
| MockData para popular o banco na inicialização | Pedro Brum | 29/04 | ✅ Concluído |

---

### Semana 3 — 30/04 a 06/05 (Funcionalidades Avançadas)
| Atividade | Responsável | Prazo | Status |
|---|---|---|---|
| CRUD de EventoSaude com status PENDENTE/REALIZADO/ATRASADO | Luiz Felipe Flosi | 02/05 | ✅ Concluído |
| Endpoints de eventos pendentes e atrasados | Luiz Felipe Flosi | 03/05 | ✅ Concluído |
| Implementação de autenticação JWT (JwtService + JwtFilter) | Arthur Brito | 04/05 | ✅ Concluído |
| AuthController (register/login) | Pedro Brum | 05/05 | ✅ Concluído |
| Configuração do Spring Security | Arthur Brito | 06/05 | ✅ Concluído |

---

### Semana 4 — 07/05 a 13/05 (Qualidade e Documentação)
| Atividade | Responsável | Prazo | Status |
|---|---|---|---|
| Configuração do Swagger/SpringDoc OpenAPI | Luiz Felipe Flosi | 08/05 | ✅ Concluído |
| Tratamento de exceções (404, 409, 401) | Arthur Brito | 09/05 | ✅ Concluído |
| Cache com @Cacheable nos serviços | Luiz Felipe Flosi | 10/05 | ✅ Concluído |
| Testes manuais de todos os endpoints via Postman | Todos | 12/05 | ✅ Concluído |
| Exportação da coleção Postman (.json) | Pedro Brum | 13/05 | ✅ Concluído |

---

### Semana 5 — 14/05 a 20/05 (Documentação e Entrega)
| Atividade | Responsável | Prazo | Status |
|---|---|---|---|
| Criação do Diagrama de Classes | Arthur Brito | 15/05 | ✅ Concluído |
| Criação do DER (Diagrama Entidade-Relacionamento) | Pedro Brum | 16/05 | ✅ Concluído |
| README completo com rotas, como rodar e integrantes | Todos | 18/05 | ✅ Concluído |
| Organização da pasta /documentos no repositório | Pedro Brum | 19/05 | ✅ Concluído |
| Revisão final e ajustes de código | Todos | 20/05 | ✅ Concluído |

---

### Semana 6 — 21/05 a 24/05 (Entrega Final)
| Atividade | Responsável | Prazo | Status |
|---|---|---|---|
| Revisão final do repositório GitHub | Todos | 22/05 | ✅ Concluído |
| Submissão no portal FIAP (Sprint 1 e Sprint 2) | Arthur Brito | 24/05 | ✅ Concluído |

---

## Resumo das Entregas da pasta /documentos

| Arquivo | Descrição | Responsável |
|---|---|---|
| `JornadaPet_Postman_Collection.json` | Coleção Postman com todas as requisições da API | Pedro Brum |
| `diagrama-classes.png` | Diagrama de Classes — entidades, repositórios, serviços e controllers | Arthur Brito |
| `der.png` | Diagrama Entidade-Relacionamento (DER) — tabelas e relacionamentos | Pedro Brum |
| `cronograma-sprint1.md` | Este documento — cronograma e divisão de tarefas | Todos |
| `cronograma.xlsx` | Versão em planilha do cronograma | Pedro Brum |

---

*JornadaPet — FIAP 2026 | Challenge Clyvo Vet | 2º Ano ADS — Turmas de Fevereiro*
