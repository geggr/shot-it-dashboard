### Faculdade de Informática e Administração Paulista (FIAP)
### Pós Graduação em Arquitetura de Software
### 8º SOAT - Tech Challenge - 05
![](https://raw.githubusercontent.com/geggr/shot-it-dashboard/refs/heads/badges/branches.svg)
![](https://raw.githubusercontent.com/geggr/shot-it-dashboard/refs/heads/badges/jacoco.svg)

---

## Shot-It

Shot-It é a aplicação que permite os usuários criarem bibliotecas de vídeos, categorizando e tendo há possibilidade
de compartilhar com todos seus amigos.

Quando um vídeo é enviado para a plataforma são automaticamente geradas thumbnails para que o usuário
tenha a possibilidade de compartilhar


### Arquitetura

- `shotit-api`
- `shotit-processor`
- `shotit-web`

### Integrações

- `AWS S3`
- `FFmpeg`
- `AWS SES`

### Como Rodar? 

#### Pré-requisitos
- Docker
- Docker Compose

#### Executando apenas a Infraestrutura

Para executar apenas os serviços de infraestrutura (MySQL, RabbitMQ e LocalStack), utilize o comando:

```bash
docker-compose up -d
```

Isso irá iniciar:
- MySQL na porta 3306
- RabbitMQ na porta 5672 (interface administrativa na porta 15672)
- LocalStack na porta 4566

### Executando a Aplicação Completa

Para executar a aplicação junto com toda a infraestrutura, utilize o comando:

```bash
docker-compose --profile app up -d
```

Isso irá iniciar:
- Todos os serviços de infraestrutura mencionados acima
- A aplicação Spring Boot na porta 8080

#### Reconstruindo a Imagem da Aplicação

Se você fez alterações no código e precisa reconstruir a imagem da aplicação, siga estes passos:

1. Primeiro, pare os serviços:
```bash
docker-compose --profile app down
```

2. Reconstrua a imagem:
```bash
docker-compose build app
```

3. Inicie os serviços novamente:
```bash
docker-compose --profile app up -d
```

Ou, em um único comando:
```bash
docker-compose --profile app up -d --build
```

#### Verificando os Serviços

Para verificar o status dos serviços:
```bash
docker-compose ps
```

#### Acessando os Serviços

- **MySQL**
  - Host: localhost
  - Porta: 3306
  - Banco: shotit
  - Usuário: root
  - Senha: verysecret

- **RabbitMQ**
  - Interface administrativa: http://localhost:15672
  - Usuário: guest
  - Senha: guest

- **LocalStack (AWS)**
  - Endpoint: http://localhost:4566
  - Região: us-east-1
  - Access Key: local
  - Secret Key: stack

- **Aplicação**
  - URL: http://localhost:8080

### Documentação da API

A documentação da API está disponível através do Swagger UI e pode ser acessada nos seguintes endpoints:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

A documentação está organizada nas seguintes seções:

- **Autenticação**: APIs de gerenciamento de autenticação
- **Vídeos**: APIs de gerenciamento e recuperação de vídeos
- **Tags**: APIs de gerenciamento de tags de vídeos
- **Upload de Vídeos**: APIs de upload e processamento de vídeos
- **Perfil do Usuário**: APIs de gerenciamento de perfil do usuário
- **Edição de Vídeos**: APIs de edição e gerenciamento de vídeos

### Parando os Serviços

Para parar todos os serviços:
```bash
docker-compose --profile app down
```

Para parar apenas a infraestrutura:
```bash
docker-compose down
```

### Logs

Para visualizar os logs de um serviço específico:
```bash
docker-compose logs -f [nome-do-servico]
```

Exemplo:
```bash
docker-compose logs -f app    # logs da aplicação
docker-compose logs -f mysql  # logs do MySQL
```
