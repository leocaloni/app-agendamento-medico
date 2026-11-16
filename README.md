# Tervis — Agendamento de Consultas Médicas

Sistema de agendamento de consultas: o paciente busca médicos, marca consultas, avalia os atendimentos e guarda seus exames; o médico gerencia o perfil, a agenda e os pacientes.

Projeto acadêmico (PII — Instituto Mauá de Tecnologia).

## Stack

| Camada | Tecnologias |
|---|---|
| Backend | Java 21, Spring Boot 4.1 (Web, Data JPA, Security, Validation), JWT (jjwt 0.12), Lombok, springdoc-openapi |
| Banco | PostgreSQL 16 (Docker) |
| Frontend | React 19 + Vite |

## Funcionalidades da API

O que o backend oferece hoje. O que já tem tela está descrito na seção [Frontend](#frontend).

- **Autenticação:** cadastro de paciente, login com JWT e controle de acesso por perfil (PATIENT, DOCTOR, ADMIN)
- **Catálogos:** especialidades e convênios, com CRUD do admin e exclusão lógica
- **Busca de médicos:** filtros por nome (sem diferenciar acento), especialidade, cidade, estado, nota mínima e convênio; ordenação por nota ou nome. O paciente com convênio já recebe a busca filtrada pelo plano dele
- **Agendamento:** horários livres calculados a partir da agenda do médico, primeira consulta e retorno, pagamento particular ou por convênio, cancelamento, conclusão e falta
- **Proteção contra concorrência:** constraint no banco impede que dois pacientes marquem horários sobrepostos com o mesmo médico, mesmo em requisições simultâneas
- **Preço da consulta:** cada médico define o valor da consulta particular, que é copiado para a consulta no agendamento (o sistema não processa pagamento)
- **Avaliações:** o paciente avalia a consulta realizada; o perfil do médico mostra média, total e distribuição das notas
- **Dados de saúde:** o paciente preenche tipo sanguíneo, altura, peso, alergias, condições crônicas, medicamentos e contato de emergência; o médico vê esses dados apenas de pacientes com consulta com ele
- **Exames:** o paciente envia exames (PDF, JPEG ou PNG, até 10MB) e escolhe com quais consultas compartilhar; só o médico dessas consultas consegue baixar

## Como executar o projeto localmente

### Pré-requisitos

- Docker Desktop instalado e em execução
- Java 21
- Node.js e npm

### 1. Iniciar o banco de dados e a API

Abra um terminal na pasta raiz do projeto e execute:

```powershell
cd backend
docker compose up -d
.\mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`.

> O banco é recriado a cada inicialização (`ddl-auto: create-drop`) e populado automaticamente com dados de teste. Tudo que for criado durante o uso some ao reiniciar a API.

### 2. Iniciar o frontend

Abra um segundo terminal na pasta raiz do projeto e execute:

```powershell
cd frontend
npm install
npm run dev
```

Acesse o endereço exibido pelo Vite no terminal.

## Documentação da API (Swagger)

Com a API rodando, acesse **http://localhost:8080/swagger-ui.html**.

Para testar rotas protegidas: faça login em `POST /api/auth/login`, copie o `token` da resposta, clique em **Authorize** e cole o token.

## Usuários de teste

Criados automaticamente a cada inicialização:

| Perfil | E-mail | Senha | Observação |
|---|---|---|---|
| Admin | `admin@teste.com` | `admin123` | |
| Paciente | `mariana@teste.com` | `senha123` | particular (sem convênio) |
| Paciente | `bruno@teste.com` | `senha123` | Unimed, com dados de saúde preenchidos |
| Paciente | `carla@teste.com` | `senha123` | Amil |
| Médica | `ana.ribeiro@teste.com` | `senha123` | Cardiologia, São Paulo |
| Médico | `rafael.monteiro@teste.com` | `senha123` | Neurologia, só atende particular |

Há mais 8 médicos no seed, todos com a senha `senha123` (veja `DevSeedRunner.java`).

## Principais rotas

Todas com prefixo `/api`.

| Recurso | Rotas |
|---|---|
| Autenticação | `POST /auth/register`, `POST /auth/login`, `GET /auth/me` |
| Perfil do usuário | `PUT /users/me`, `GET`/`PUT /users/me/health` |
| Médicos | `GET /doctors`, `GET /doctors/{id}`, `GET /doctors/{id}/availability`, `GET`/`PUT /doctors/me`, `PUT /doctors/me/schedule` |
| Consultas | `POST /appointments`, `GET /appointments/me`, `GET /appointments/{id}`, `PATCH /appointments/{id}/cancel`, `/complete`, `/no-show`, `/notes`, `GET /appointments/{id}/patient-health` |
| Exames | `POST /exams`, `GET /exams/me`, `GET /exams/{id}/download`, `DELETE /exams/{id}`, `PUT`/`DELETE /exams/{id}/shares/{appointmentId}`, `GET /appointments/{id}/exams` |
| Avaliações | `POST /appointments/{id}/review`, `GET /doctors/{id}/reviews`, `GET /reviews/me` |
| Catálogos | `/specialties`, `/health-plans` (leitura pública, escrita só ADMIN) |
| Admin | `POST /admin/users/doctors`, `POST /admin/users/admins`, `GET /admin/specialties`, `GET /admin/health-plans` |

## Testes

O projeto tem um teste automatizado, que verifica que duas requisições simultâneas não conseguem marcar o mesmo horário (nem horários sobrepostos). Ele precisa do Postgres rodando:

```powershell
cd backend
docker compose up -d
.\mvnw.cmd test
```

As demais funcionalidades são validadas manualmente pelo Swagger ou pelo Postman.

## Frontend

Aplicação React de página única. A navegação entre telas é feita por estado no `Index.jsx` (sem biblioteca de rotas), e o token JWT do login fica no `localStorage`.

### Telas

**Acesso**

| Tela | Arquivo | O que faz |
|---|---|---|
| Login | `Login.jsx` + `LoginForm.jsx` | Entra com e-mail e senha (`POST /api/auth/login`) e leva o usuário para a área do paciente ou do médico, conforme o perfil |
| Cadastro | `Cadastro.jsx` | Cria conta de paciente com nome, e-mail, senha, CPF, data de nascimento e telefone (`POST /api/auth/register`) |

**Área do paciente**

| Tela | Arquivo | O que faz |
|---|---|---|
| Início | `TelaUsuario.jsx` | Saudação e atalhos para buscar médico, ver o histórico e as demais seções |
| Buscar médicos | `DoctorSearch.jsx` | Busca por nome, especialidade, convênio e avaliação mínima, com ordenação por nota ou nome e paginação |
| Perfil do médico | `DoctorProfile.jsx` | Mostra os dados do médico, os dias e horários livres dos próximos 14 dias e o formulário de agendamento (especialidade, forma de pagamento, convênio e observação) |
| Minhas consultas | `MinhasConsultas.jsx` | Lista as consultas com filtros (Próximas, Concluídas, Todas) e permite avaliar consultas concluídas com nota de 1 a 5 e comentário |

**Área do médico**

| Tela | Arquivo | O que faz |
|---|---|---|
| Início | `TelaMedico.jsx` | Resumo do dia e a agenda de hoje |
| Agenda geral | `AgendaMedico.jsx` | Calendário mensal; ao escolher um dia, lista as consultas dele |

**Componentes compartilhados** (`src/components/`): `Navbar` (menu lateral com saída da conta), `TervisBrand` (logo) e os componentes do formulário de login.

**Serviços** (`src/services/`): `doctorApi.js` (catálogos, busca, perfil, disponibilidade e agendamento) e `appointmentApi.js` (consultas e avaliações do usuário logado).

### Configuração

Por padrão o frontend chama a API em `http://localhost:8080`. Para usar outro endereço, crie um arquivo `frontend/.env`:

```
VITE_API_URL=http://endereco-da-api:8080
```

### Ainda não implementado no frontend

O backend já oferece estas funcionalidades, mas elas ainda não têm tela:

- **Meus exames:** envio, listagem, download e compartilhamento com consultas (o item já aparece no menu)
- **Dados de saúde** do paciente, e a visualização deles pelo médico
- **Preço da consulta** na busca, no perfil do médico e no agendamento
- **Avaliações:** tela própria com as avaliações já feitas (o item já aparece no menu)
- **Edição de perfil:** do paciente (dados e convênio) e do médico (perfil, preço e horário de atendimento)
- **Ações do médico na consulta:** concluir, marcar falta e registrar anotações
- **Cancelamento de consulta** e agendamento de **retorno**
- **Painel do administrador:** especialidades, convênios e cadastro de médicos
- "Esqueci minha senha" e login com Google / Gov.br (apenas visuais)

## Estrutura do projeto

```
app-agendamento-medico
├── backend/    # API Spring Boot
└── frontend/   # aplicação React + Vite
    └── src/
        ├── components/   # Navbar, logo e formulário de login
        ├── pages/        # telas (e o CSS de cada uma)
        └── services/     # chamadas à API
```

## Estrutura do backend

```
backend/src/main/java/com/pi/agendamento
├── config/        # segurança, JWT, seed de dados e extensões do banco
├── controller/    # rotas REST
├── dto/
│   ├── request/   # corpos de requisição (validados)
│   └── response/  # corpos de resposta
├── entity/        # entidades JPA
├── enums/
├── exception/     # exceções e tratamento padronizado de erros
├── repository/
└── service/       # regras de negócio
```

Os arquivos de exames enviados ficam na pasta `backend/uploads/`, que não vai para o Git.
