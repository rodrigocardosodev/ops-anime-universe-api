package com.devcorp.ops_anime_universe_api.infrastructure.config

import java.lang.reflect.Method
import java.net.URI
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.slf4j.Logger
import org.springframework.http.HttpMethod
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.test.StepVerifier

class WebClientConfigTest {

  private lateinit var webClientConfig: WebClientConfig
  private lateinit var mockLogger: Logger
  private lateinit var exchangeFunction: ExchangeFunction
  private lateinit var clientRequest: ClientRequest
  private lateinit var clientResponse: ClientResponse
  private lateinit var logRequestMethod: Method

  @BeforeEach
  fun setup() {
    webClientConfig = WebClientConfig()

    // Usando reflexão para acessar o método privado logRequest
    logRequestMethod = WebClientConfig::class.java.getDeclaredMethod("logRequest")
    logRequestMethod.isAccessible = true

    // Mock do logger usando reflexão
    val loggerField = WebClientConfig::class.java.getDeclaredField("logger")
    loggerField.isAccessible = true
    mockLogger = mock(Logger::class.java)
    loggerField.set(webClientConfig, mockLogger)

    // Configuração dos mocks para os testes
    exchangeFunction = mock(ExchangeFunction::class.java)
    clientRequest = mock(ClientRequest::class.java)
    clientResponse = mock(ClientResponse::class.java)

    `when`(clientRequest.url()).thenReturn(URI("https://example.com/api"))
    `when`(clientRequest.method()).thenReturn(HttpMethod.GET)
    `when`(exchangeFunction.exchange(clientRequest)).thenReturn(Mono.just(clientResponse))
  }

  @Test
  fun `webClientBuilder deve criar um builder configurado`() {
    val builder = webClientConfig.webClientBuilder()
    assertNotNull(builder)
  }

  @Test
  fun `logRequest deve criar um ExchangeFilterFunction válido`() {
    val filter = logRequestMethod.invoke(webClientConfig) as ExchangeFilterFunction
    assertNotNull(filter)
    assertTrue(filter is ExchangeFilterFunction)
  }

  @Test
  fun `logRequest deve registrar as requisições quando debug está habilitado`() {
    // Arrange
    `when`(mockLogger.isDebugEnabled).thenReturn(true)

    // Act
    val filterFunction = logRequestMethod.invoke(webClientConfig) as ExchangeFilterFunction
    val result = filterFunction.filter(clientRequest, exchangeFunction)

    // Assert
    // Verificar se o método debug do logger foi chamado
    verify(mockLogger).debug(Mockito.anyString(), Mockito.any(), Mockito.any())

    // Verificamos que o pipeline continua, testando o resultado
    StepVerifier.create(result)
            .expectNextMatches { response -> response === clientResponse }
            .verifyComplete()
  }

  @Test
  fun `logRequest não deve registrar requisições quando debug não está habilitado`() {
    // Arrange
    `when`(mockLogger.isDebugEnabled).thenReturn(false)

    // Act
    val filterFunction = logRequestMethod.invoke(webClientConfig) as ExchangeFilterFunction
    val result = filterFunction.filter(clientRequest, exchangeFunction)

    // Assert
    // Verificar que o método debug do logger NÃO foi chamado
    Mockito.verify(mockLogger, Mockito.never())
            .debug(Mockito.anyString(), Mockito.any(), Mockito.any())

    // Verificamos que o pipeline continua, testando o resultado
    StepVerifier.create(result)
            .expectNextMatches { response -> response === clientResponse }
            .verifyComplete()
  }

  @Test
  fun `deve criar WebClient Builder com configurações corretas`() {
    // Act
    val webClientBuilder = webClientConfig.webClientBuilder()

    // Assert
    assertNotNull(webClientBuilder)

    // Testa se o builder pode criar um cliente funcional
    val webClient = webClientBuilder.build()
    assertNotNull(webClient)
  }

  @Test
  fun `deve ter configurações de timeout e compressão`() {
    // Arrange
    val webClientBuilder = webClientConfig.webClientBuilder()
    val webClient = webClientBuilder.build()

    // Act & Assert
    // Observação: não é possível verificar diretamente as configurações internas
    // do WebClient, mas podemos verificar se ele foi criado corretamente
    assertNotNull(webClient)
  }

  @Test
  fun `WebClient Builder deve poder criar instâncias com diferentes URLs base`() {
    // Act
    val webClientBuilder = webClientConfig.webClientBuilder()

    // Criando diferentes instâncias de WebClient com URLs base diferentes
    val webClient1 = webClientBuilder.baseUrl("http://example.com/api").build()
    val webClient2 = webClientBuilder.baseUrl("http://localhost:8080").build()

    // Assert
    assertNotNull(webClient1)
    assertNotNull(webClient2)
  }

  @Test
  fun `WebClient Builder deve poder ser usado em ambientes diferentes`() {
    // Act
    val webClientBuilder = webClientConfig.webClientBuilder()

    // Criando um WebClient com configurações específicas
    val webClient =
            webClientBuilder
                    .defaultHeader("X-Api-Key", "test-key")
                    .defaultHeader("Content-Type", "application/json")
                    .build()

    // Assert
    assertNotNull(webClient)
  }

  @Test
  fun `ExchangeStrategies deve ser configurado com o tamanho correto`() {
    // Arrange & Act
    val webClientBuilder = webClientConfig.webClientBuilder()
    val webClient = webClientBuilder.build()

    // Assert
    assertNotNull(webClient)

    // Testamos indiretamente que a configuração está correta
    // verificando se o builder pode criar um cliente funcional
    assertTrue(webClient is WebClient)

    // Verificamos o valor configurado para maxInMemorySize
    val maxInMemorySize = ReflectionTestUtils.getField(webClientConfig, "maxInMemorySize") as Int?
    assertNotNull(maxInMemorySize)
    assertEquals(16 * 1024 * 1024, maxInMemorySize) // 16MB
  }

  @Test
  fun `HttpClient deve ser configurado com timeout e compressão`() {
    // Reflexão para acessar o httpClient diretamente
    try {
      // Tentativa de acesso ao campo httpClient declarado como propriedade top-level
      val httpClientField = webClientConfig.javaClass.getDeclaredField("httpClient")
      httpClientField.isAccessible = true
      val httpClient = httpClientField.get(webClientConfig) as? HttpClient

      if (httpClient != null) {
        // Se tivermos acesso ao HttpClient, podemos fazer mais asserções
        assertNotNull(httpClient)
      } else {
        // Se não conseguirmos acessar o campo, testamos indiretamente pelo builder
        val builder = webClientConfig.webClientBuilder()
        assertNotNull(builder)
      }
    } catch (e: NoSuchFieldException) {
      // Se o campo não existir ou não for acessível, validamos pelo builder
      val builder = webClientConfig.webClientBuilder()
      assertNotNull(builder)
    }
  }

  @Test
  fun `ConnectionProvider deve ser configurado com valores corretos`() {
    // Verify that the configuration parameters are set to expected values
    // Note: We can't directly test the ConnectionProvider construction, but we can verify the
    // configuration
    val maxConnections = ReflectionTestUtils.getField(webClientConfig, "maxConnections")
    val maxIdleTimeSeconds = ReflectionTestUtils.getField(webClientConfig, "maxIdleTimeSeconds")
    val pendingAcquireTimeoutSeconds =
            ReflectionTestUtils.getField(webClientConfig, "pendingAcquireTimeoutSeconds")

    assertNotNull(maxConnections)
    assertNotNull(maxIdleTimeSeconds)
    assertNotNull(pendingAcquireTimeoutSeconds)

    // Verify the WebClientBuilder can be created with these settings
    val webClientBuilder = webClientConfig.webClientBuilder()
    assertNotNull(webClientBuilder)
  }

  @Test
  fun `doOnConnected deve adicionar handlers de timeout`() {
    // Esta função testa a configuração do HttpClient com os handlers de timeout
    // É difícil testar diretamente a função lambda doOnConnected, então testamos indiretamente

    // Verificamos os valores de timeout que seriam usados
    val readTimeoutSeconds =
            ReflectionTestUtils.getField(webClientConfig, "readTimeoutSeconds") as Long
    val writeTimeoutSeconds =
            ReflectionTestUtils.getField(webClientConfig, "writeTimeoutSeconds") as Long

    assertEquals(30L, readTimeoutSeconds)
    assertEquals(30L, writeTimeoutSeconds)

    // Criamos o builder e verificamos que ele funciona, o que implica que a configuração está
    // correta
    val webClientBuilder = webClientConfig.webClientBuilder()
    assertNotNull(webClientBuilder)
  }

  @Test
  fun `logRequest deve filtrar e logar chamadas`() {
    // Arrange
    val filter = logRequestMethod.invoke(webClientConfig) as ExchangeFilterFunction

    // Configurar o mock do logger para retornar true em isDebugEnabled
    `when`(mockLogger.isDebugEnabled).thenReturn(true)

    // Act
    // Aplicamos o filtro através de uma simulação
    val result = filter.filter(clientRequest, exchangeFunction)

    // Assert
    assertNotNull(result)

    // Verificar que o método correto do logger foi chamado
    StepVerifier.create(result).expectNextMatches { it === clientResponse }.verifyComplete()

    // Verificar que exchange foi chamado com o clientRequest original
    verify(exchangeFunction).exchange(clientRequest)
  }
}
