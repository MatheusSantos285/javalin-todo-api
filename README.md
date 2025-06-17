# API de Gestão de Tarefas (ToDo) com Javalin

Uma API RESTful robusta e bem testada para gestão de tarefas, construída com o micro-framework **Javalin**.  
Este projeto foi desenvolvido como um trabalho acadêmico, com foco em boas práticas de arquitetura de software, clareza de código e uma cobertura de testes completa.

---

## 🏛️ Arquitetura

A aplicação segue uma arquitetura em camadas para garantir a separação de responsabilidades, manutenibilidade e testabilidade:

- **Controller:**  
  Responsável por lidar com as requisições e respostas HTTP. Faz a ponte entre o mundo web e a lógica da aplicação.

- **Service:**  
  Contém a lógica de negócio principal. Orquestra as operações e validações.

- **Repository:**  
  Camada de acesso a dados, responsável por toda a comunicação com o banco de dados H2.

- **DTO (Data Transfer Objects):**  
  Utilizados para definir um contrato claro e seguro para a API, desacoplando o modelo interno da representação externa.

---

## ✨ Funcionalidades

A API implementa as operações **CRUD** (Criar, Ler, Atualizar, Apagar) para o recurso de tarefas.

| Método | Endpoint            | Descrição                                   |
|--------|---------------------|----------------------------------------------|
| GET    | `/tarefas`          | Lista todas as tarefas                      |
| GET    | `/tarefas/{id}`     | Busca uma tarefa específica pelo seu ID      |
| POST   | `/tarefas`          | Cria uma nova tarefa                        |
| PUT    | `/tarefas/{id}`     | Atualiza uma tarefa existente                |
| DELETE | `/tarefas/{id}`     | Apaga uma tarefa existente                   |
| GET    | `/status`           | Verifica a saúde e o timestamp da API        |

---

## 🚀 Começar a Usar

### Pré-requisitos

- Java **JDK 17** ou superior
- **Gradle 8.5** ou superior

### Instalação e Execução

Clone o repositório:

```
git clone https://github.com/seu-usuario/seu-repositorio.git
cd seu-repositorio

```
Execute a aplicação (o Gradle Wrapper está incluído):
### No Windows
```
./gradlew.bat run
```
### No Linux/macOS
```
./gradlew run
```
O servidor será iniciado em:
👉 http://localhost:7000

## ⚙️ Uso da API
Para interagir com a API, é necessário enviar um token de autenticação no cabeçalho de cada requisição (exceto para os endpoints de utilidade).

- Token: vasco-da-gama

## 🔗 Exemplos de Requisição com curl
1. Criar uma nova tarefa:
```
curl -X POST http://localhost:7000/tarefas \
-H "Content-Type: application/json" \
-H "Authorization: vasco-da-gama" \
-d '{"titulo":"Comprar pão","descricao":"Ir à padaria da esquina."}'
```
2. Listar todas as tarefas:
```
curl -X GET http://localhost:7000/tarefas \
-H "Authorization: vasco-da-gama"
```
3. Buscar a tarefa com ID 1:
```
curl -X GET http://localhost:7000/tarefas/1 \
-H "Authorization: vasco-da-gama"
```
4. Atualizar a tarefa com ID 1:
```
curl -X PUT http://localhost:7000/tarefas/1 \
-H "Content-Type: application/json" \
-H "Authorization: vasco-da-gama" \
-d '{"titulo":"Comprar pão integral","descricao":"Ir à padaria da esquina.","concluida":true}'
```
5. Apagar a tarefa com ID 1:
```
curl -X DELETE http://localhost:7000/tarefas/1 \
-H "Authorization: vasco-da-gama"

```
## ✅ Executar os Testes
Este projeto possui uma suíte de testes unitários e de integração para garantir a qualidade e o correto funcionamento do código.

Execute os testes com:

# No Windows
```
./gradlew.bat test
```
# No Linux/macOS
```
./gradlew test
```
Um relatório detalhado será gerado em:
📄 build/reports/tests/test/index.html

## 📄 Licença
Este projeto está sob a licença MIT.
Consulte o arquivo LICENSE para mais detalhes.
