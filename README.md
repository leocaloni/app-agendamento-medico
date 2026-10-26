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

### 2. Iniciar o frontend
Abra um segundo terminal na pasta raiz do projeto e execute:

```powershell
cd frontend
npm install
npm run dev
```
Acesse o endereço exibido pelo Vite no terminal.
