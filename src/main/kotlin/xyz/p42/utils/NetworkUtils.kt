package xyz.p42.utils


import xyz.p42.graphQlEndpoint
import xyz.p42.json
import xyz.p42.model.Account
import xyz.p42.model.GraphQlPayload
import xyz.p42.model.VkGraphQlResponse
import xyz.p42.properties.ENDPOINT_AVAILABILITY_CHECK_TIMEOUT
import xyz.p42.properties.RESPONSE_STRING_LIMIT_CHARS
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun isEndpointAvailable(url: String): Boolean =
  try {
    LoggingUtils.logger.info("Checking the '$url' availability...")

    val connection = URL(url).openConnection()
    connection.connectTimeout = ENDPOINT_AVAILABILITY_CHECK_TIMEOUT
    connection.connect()
    true
  } catch (e: Exception) {
    LoggingUtils.logger.info(e.message!!)
    false
  }

fun getAccountVerificationKey(account: Account): String? =
  try {
    val client = HttpClient.newBuilder().build()
    val request =
      HttpRequest.newBuilder()
        .uri(URI.create(graphQlEndpoint))
        .POST(
          HttpRequest.BodyPublishers.ofString(
            json.encodeToString(
              serializer = GraphQlPayload.serializer(),
              value = GraphQlPayload(
                query = getAccountVkGraphQlQuery(account.pk)
              )
            )
          )
        )
        .header("Content-Type", "application/json")
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString()).body()
    LoggingUtils.logger.info("GraphQL Response: ${response.take(RESPONSE_STRING_LIMIT_CHARS)} ...")

    json.decodeFromString<VkGraphQlResponse>(response).data.account.verificationKey?.verificationKey
  } catch (e: Exception) {
    null
  }
