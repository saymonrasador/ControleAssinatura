# SubTrack — Aplicativo Gerenciador de Assinaturas

Um aplicativo desktop JavaFX para gerenciar assinaturas digitais recorrentes e pagamentos.

## Features

- **User Authentication**: Registro seguro e login com hash de senha BCrypt
- **Subscription Management**: Acompanhe assinaturas mensais e anuais com renovação automática, status e datas de vencimento
- **Category & Payment Method Management**: Organize assinaturas com categorias codificadas por cores e métodos de pagamento
- **Payment History**: Registros de pagamento imutáveis com prevenção de duplicatas baseada em ciclo
- **Smart Alerts**: Notificações configuráveis para datas de vencimento próximas
- **Dashboard**: Gráficos interativos e filtros para análise de gastos
- **Spending Limits**: Acompanhamento de orçamento mensal com indicadores visuais de progresso

## Tech Stack

- **Java 21** + **JavaFX 21**
- **SQLite** (embutido, via JDBC)
- **BCrypt** (hash de senha)
- **Maven** (ferramenta de build)

## Pré-requisitos

- JDK 21 ou superior

## Rodar projeto

```bash
# Compile o projeto (Maven Wrapper incluído)
./mvnw.cmd clean compile    # Windows

# Execute o aplicativo
./mvnw.cmd javafx:run       # Windows
```

## Estrutura do Projeto

```
src/main/java/com/subtrack/
├── App.java                  # Ponto de entrada da aplicação
├── config/                   # Configuração do banco de dados
├── domain/                   # Classes de entidade e enums
├── repository/               # Camada de acesso a dados (JDBC)
├── service/                  # Camada de lógica de negócios
├── controller/               # Controladores FXML JavaFX
└── util/                     # Validação, datas, sessão, navegação

src/main/resources/
├── schema.sql                # Banco de dados DDL
└── com/subtrack/
    ├── views/                # Arquivos de visualização FXML
    └── styles/               # Estilos CSS
```

## Banco de Dados

O banco de dados SQLite é armazenado em:

- Windows: `%USERPROFILE%\.subtrack\subtrack.db`

O schema do banco de dados é criado automaticamente na primeira inicialização.

## Arquitetura

A aplicação segue uma arquitetura em camadas:

1. **Domain**: Classes de entidade puras e enums
2. **Repository**: Acesso a dados baseado em JDBC (um por entidade)
3. **Service**: Regras de negócio e orquestração
4. **Controller**: Controladores FXML JavaFX (lógica de UI)
5. **UI**: Visualizações FXML + estilos CSS
