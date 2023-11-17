package xyz.p42.plugins

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.html.respondHtml
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.html.body
import kotlinx.html.code
import kotlinx.html.head
import kotlinx.html.style
import kotlinx.html.title
import kotlinx.serialization.encodeToString
import xyz.p42.accounts
import xyz.p42.accountsToBeReleased
import xyz.p42.graphQlEndpoint
import xyz.p42.json
import xyz.p42.model.Account
import xyz.p42.model.Message
import xyz.p42.properties.ACCOUNTS_TO_KEEP_UNUSED
import xyz.p42.properties.HTML_CODE_BLOCK_STYLE
import xyz.p42.properties.IS_REGULAR_ACCOUNT_QUERY_PARAM
import xyz.p42.properties.SERVICE_TITLE
import xyz.p42.properties.UNLOCK_ACCOUNT_QUERY_PARAM
import xyz.p42.utils.LoggingUtils
import xyz.p42.utils.getAccountVerificationKey
import xyz.p42.utils.getErrorHtml
import xyz.p42.utils.getWelcomeMessage
import xyz.p42.utils.isEndpointAvailable
import xyz.p42.utils.releaseAccountAndGetNextIndex
import xyz.p42.utils.unlockAccount
import kotlin.random.Random

val logger = LoggingUtils.logger

private val acquireAccountMutex = Mutex()
private val releaseAccountMutex = Mutex()
private val listAcquiredAccountsMutex = Mutex()
private val unlockAccountMutex = Mutex()

fun Application.configureRouting() {
  install(StatusPages) {
    exception<Throwable> { call, cause ->
      call.respondHtml(status = HttpStatusCode.InternalServerError) {
        getErrorHtml(cause.message!!, this)
      }
    }
    status(HttpStatusCode.NotFound) { call, cause ->
      call.respondHtml(status = HttpStatusCode.NotFound) {
        getErrorHtml(cause.description, this)
      }
    }
    status(HttpStatusCode.MethodNotAllowed) { call, cause ->
      call.respondHtml(status = HttpStatusCode.MethodNotAllowed) {
        getErrorHtml(cause.description, this)
      }
    }
  }

  routing {
    get("/") {
      call.respondHtml {
        head {
          title(SERVICE_TITLE)
        }
        body {
          code {
            style = HTML_CODE_BLOCK_STYLE
            +getWelcomeMessage()
          }
        }
      }
    }
    get("/acquire-account") {
      acquireAccountMutex.withLock {
        val isRegularAccount = call.request.queryParameters[IS_REGULAR_ACCOUNT_QUERY_PARAM]?.toBoolean() ?: true
        val unlockAccount = call.request.queryParameters[UNLOCK_ACCOUNT_QUERY_PARAM]?.toBoolean() ?: false

        try {
          var index = Random.nextInt(0, accounts.size - ACCOUNTS_TO_KEEP_UNUSED)
          if (isRegularAccount && isEndpointAvailable(graphQlEndpoint)) {
            logger.info("An attempt to acquire non-zkApp account...")

            val verificationKey: String? = getAccountVerificationKey(accounts[index])
            while (verificationKey != null && accounts[index].used) {
              logger
                .info(
                  "Account with index #${index} is already in use or this is the zkApp account when it is not expected!"
                )
              index = releaseAccountAndGetNextIndex()
            }
          } else {
            logger.info("An attempt to acquire any account...")

            while (accounts[index].used) {
              logger
                .info(
                  "Account with index #${index} is already in use!"
                )
              index = releaseAccountAndGetNextIndex()
            }
          }
          accounts[index].used = true
          logger
            .info(
              "Acquired account with Index #${index} and public key ${accounts[index].pk}"
            )
          if (unlockAccount) {
            logger.info("Unlocking account with index #${index}...")
            unlockAccount(accounts[index])
          }
          call.respondText(
            text = json.encodeToString(
              value = accounts[index]
            ),
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.OK
          )
        } catch (e: Throwable) {
          call.respondText(
            text = json.encodeToString(
              value = Message(
                code = HttpStatusCode.InternalServerError.value,
                message = e.message!!
              )
            ),
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.InternalServerError
          )
        }
      }
    }
    put("/release-account") {
      releaseAccountMutex.withLock {
        try {
          val account = json.decodeFromString<Account>(call.receiveText())
          val message = "Account with public key ${account.pk} is set to be released."

          accountsToBeReleased.add(account)
          logger.info(message)

          call.respondText(
            text = json.encodeToString(
              value = Message(
                code = HttpStatusCode.OK.value,
                message = message
              )
            ),
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.OK
          )
        } catch (e: Throwable) {
          call.respondText(
            text = json.encodeToString(
              value = Message(
                code = HttpStatusCode.InternalServerError.value,
                message = e.message!!
              )
            ),
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.InternalServerError
          )
        }
      }
    }
    get("/list-acquired-accounts") {
      listAcquiredAccountsMutex.withLock {
        logger.info("Listing acquired accounts...")
        call.respondText(
          text = json.encodeToString(
            value = accounts.filter { it.used }
          ),
          contentType = ContentType.Application.Json,
          status = HttpStatusCode.OK
        )
      }
    }
    put("/unlock-account") {
      unlockAccountMutex.withLock {
        try {
          val account = json.decodeFromString<Account>(call.receiveText())
          val unlockedAccount = unlockAccount(account)
          val message = "Account with public key $unlockedAccount is unlocked."

          logger.info(message)
          call.respondText(
            text = json.encodeToString(
              value = Message(
                code = HttpStatusCode.OK.value,
                message = message
              )
            ),
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.OK
          )
        } catch (e: Throwable) {
          call.respondText(
            text = json.encodeToString(
              value = Message(
                code = HttpStatusCode.InternalServerError.value,
                message = e.message!!
              )
            ),
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.InternalServerError
          )
        }
      }
    }
  }
}
