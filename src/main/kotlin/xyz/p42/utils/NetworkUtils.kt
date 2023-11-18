package xyz.p42.utils


import kotlinx.serialization.encodeToString
import xyz.p42.graphQlEndpoint
import xyz.p42.json
import xyz.p42.model.Account
import xyz.p42.model.GraphQlPayload
import xyz.p42.model.LockAccountGraphQlResponse
import xyz.p42.model.UnlockAccountGraphQlResponse
import xyz.p42.model.VkGraphQlResponse
import xyz.p42.properties.ENDPOINT_AVAILABILITY_CHECK_TIMEOUT
import xyz.p42.properties.RESPONSE_STRING_LIMIT_CHARS
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

val logger = LoggingUtils.logger

fun isEndpointAvailable(url: String): Boolean =
  try {
    logger.info("Checking the '$url' availability...")

    val connection = URI(url).toURL().openConnection()
    connection.connectTimeout = ENDPOINT_AVAILABILITY_CHECK_TIMEOUT
    connection.connect()
    true
  } catch (e: Exception) {
    logger.info(e.message!!)
    false
  }

fun getAccountVerificationKey(account: Account): String? =
  sendGraphQlQuery(getAccountVkGraphQlQuery(account.pk)).let {
    if (it == null) {
      logger.info("Verification key for the account '${account.pk}' is not available!")
      return null
    }
    return json.decodeFromString<VkGraphQlResponse>(it).data.account.verificationKey?.verificationKey
  }

fun lockAccount(account: Account): String =
  sendGraphQlQuery(getLockAccountGraphQlQuery(account.pk)).let {
    checkNotNull(it) { "Account '${account.pk}' cannot be locked!" }
    return json.decodeFromString<LockAccountGraphQlResponse>(it).data.lockAccount.account.publicKey
  }

fun unlockAccount(account: Account): String =
  sendGraphQlQuery(getUnlockAccountGraphQlQuery(account.pk)).let {
    checkNotNull(it) { "Account '${account.pk}' cannot be unlocked!" }
    return json.decodeFromString<UnlockAccountGraphQlResponse>(it).data.unlockAccount.account.publicKey
  }

fun sendGraphQlQuery(query: String): String? =
  try {
    val client = HttpClient.newBuilder().build()
    val request =
      HttpRequest.newBuilder()
        .uri(URI.create(graphQlEndpoint))
        .POST(
          HttpRequest.BodyPublishers.ofString(
            json.encodeToString(
              value = GraphQlPayload(
                query
              )
            )
          )
        )
        .header("Content-Type", "application/json")
        .build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    logger.info("GraphQL response: ${response.body().take(RESPONSE_STRING_LIMIT_CHARS)} ...")
    if (response.statusCode() != 200) {
      null
    } else {
      response.body()
    }
  } catch (e: Exception) {
    null
  }
