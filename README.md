# SubTrack — Gerenciador de Assinaturas Digitais

Aplicativo desktop JavaFX para gerenciamento centralizado de assinaturas digitais recorrentes. Controle gastos, receba alertas de vencimento, organize por categorias e acompanhe todo o histórico de pagamentos em um único lugar!

---

## Funcionalidades

### Autenticação

- Cadastro de conta com validação de e-mail e senha (mínimo 8 caracteres)
- Login seguro com hash BCrypt
- Gerenciamento de sessão via singleton

### Dashboard

- Resumo de gastos mensais (com equivalência proporcional para assinaturas anuais)
- Controle de orçamento mensal com indicador visual de progresso
- Contador de assinaturas ativas
- Filtros em tempo real: busca por nome, categoria, método de pagamento e status
- Tabela interativa com ações de editar, excluir e marcar como pago
- Gráficos de pizza: distribuição de gastos por categoria e por método de pagamento
- Badge de notificações não lidas

### Gerenciamento de Assinaturas

- Criação e edição de assinaturas com: nome, valor, periodicidade (Mensal/Anual), data de compra, categoria, método de pagamento e renovação automática
- Cálculo automático de próxima data de vencimento
- Status calculado automaticamente:
  - **PENDENTE** — pagamento futuro
  - **ALERTA** — dentro da janela de alerta configurada
  - **ATRASADO** — vencido com renovação automática habilitada
  - **PAGO** — pagamento registrado no ciclo atual
- Exclusão lógica (soft delete) para preservar histórico

### Categorias e Métodos de Pagamento

- CRUD completo com seletor de cor (hex)
- Categoria padrão "Geral" e método padrão "Outros" criados automaticamente no cadastro
- Exclusão redireciona assinaturas vinculadas para o padrão (sem orphans)
- Validação de nomes duplicados

### Histórico de Pagamentos

- Registros imutáveis com snapshots de nome da assinatura, categoria e método de pagamento
- Prevenção de duplicatas por ciclo (competência `yyyy-MM`)
- Filtros: assinatura, categoria, método de pagamento, intervalo de datas

### Notificações

- Geração automática de alertas multitipos (ALERTA, ATRASADO, PENDENTE)
- Janela de antecedência configurável (padrão: 3 dias)
- Popup de alertas urgentes ao abrir o dashboard
- Marcar individualmente ou todas como lidas

### Perfil

- Configuração de dias de antecedência para alertas
- Definição de limite mensal de gastos

---

## Stack Tecnológica

| Componente        | Tecnologia        | Versão     |
| ----------------- | ----------------- | ---------- |
| Linguagem         | Java              | 21         |
| Interface gráfica | JavaFX            | 21.0.2     |
| Banco de dados    | SQLite (embedded) | 3.45.3.0   |
| Hash de senha     | BCrypt (jbcrypt)  | 0.4        |
| Build             | Maven Wrapper     | 3.12.1     |
| Módulos           | Java JPMS         | Habilitado |

---

## Pré-requisitos

- **JDK 21** ou superior instalado e configurado no PATH

Não é necessário instalar o Maven — o projeto inclui o **Maven Wrapper** (`mvnw.cmd`).

---

## Como Executar

```bash
# Windows — compilar e executar
.\mvnw.cmd javafx:run

# Apenas compilar
.\mvnw.cmd clean compile
```

Na primeira execução, o banco de dados é criado automaticamente em:

```
%USERPROFILE%\.subtrack\subtrack.db
```

---

## Arquitetura

A aplicação segue arquitetura em camadas com separação estrita de responsabilidades:

```
Controller (UI/FXML)
      │
   Service (Negócio)
      │
  Repository (JDBC)
      │
  SQLite (Banco)
```

**Decisões de design relevantes:**

- **SQLite embutido** — zero dependência externa, banco criado no diretório do usuário
- **JDBC puro com PreparedStatements** — sem ORM, proteção contra SQL injection
- **Registros de pagamento imutáveis** — snapshots de nome/categoria/método evitam inconsistências históricas ao renomear entidades
- **Deduplicação por competência** — constraint `UNIQUE(subscription_id, competence)` impede duplo registro no mesmo ciclo mensal
- **Soft delete em assinaturas** — desativação preserva histórico de pagamentos
- **Reassign automático** — ao excluir categoria ou método, assinaturas vinculadas migram para o padrão
- **Equivalência mensal** — assinaturas anuais são divididas por 12 para cálculo correto do orçamento mensal

---

## Segurança

- Senhas armazenadas com **BCrypt** (custo 12, com salt)
- **PreparedStatements** em todas as queries (prevenção de SQL injection)
- **Foreign keys** habilitadas no SQLite
- Nenhum dado sensível em logs

---
