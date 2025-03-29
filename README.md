# API de Universo de Animes

Esta API agrega dados de diferentes universos de animes, atualmente suportando Dragon Ball e Pokémon.

## Arquitetura

O projeto segue a arquitetura "Ports and Adapters" (Hexagonal), que separa claramente a lógica de domínio das camadas externas:

- **Domain**: Contém o modelo de domínio, as portas de entrada (API) e saída (SPI)
- **Application**: Contém os casos de uso que orquestram as operações de domínio
- **Infrastructure**: Contém os adaptadores de entrada (REST) e saída (APIs externas)

## Recursos

### API de Personagens

- `GET /api/characters?page=0&size=20`: Retorna uma lista paginada de personagens provenientes de todos os universos suportados.
  - Parâmetros:
    - `page`: Número da página (começando em 0)
    - `size`: Tamanho da página (máximo 50)
  - Resposta: Lista de personagens com distribuição balanceada entre os universos.

### Health Check

- `GET /health`: Retorna informações sobre a saúde da aplicação e das APIs externas.
  - Resposta: Status da aplicação e de cada serviço externo.

## Tecnologias

- Kotlin 1.9.25
- Spring Boot 3.4.4
- Spring WebFlux
- Kotlin Coroutines
- JUnit 5
- MockK

## Configuração

As configurações estão definidas em `application.properties`:

```properties
# Configuração do servidor
server.port=8080

# Configurações da API do Dragon Ball
external.api.dragonball.base-url=https://dragonball-api.com/api/
external.api.dragonball.timeout=5000

# Configurações da PokeAPI
external.api.pokemon.base-url=https://pokeapi.co/api/v2/
external.api.pokemon.timeout=5000
```

## Executando a Aplicação

```bash
# Compilar o projeto
mvn clean package

# Executar a aplicação
java -jar target/ops-anime-universe-api-0.0.1-SNAPSHOT.jar
```

## Testes

O projeto inclui testes unitários e de integração:

```bash
# Executar todos os testes
mvn test

# Executar apenas testes unitários
mvn test -Dtest="*Test"

# Executar apenas testes de integração
mvn test -Dtest="*IntegrationTest"
``` 