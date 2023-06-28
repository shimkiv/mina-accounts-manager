package xyz.p42.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.html.*
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
import xyz.p42.utils.*
import kotlin.random.Random

private val acquireAccountMutex = Mutex()
private val releaseAccountMutex = Mutex()

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

                try {
                    var index = Random.nextInt(0, accounts.size - ACCOUNTS_TO_KEEP_UNUSED)
                    if (isRegularAccount && isEndpointAvailable(graphQlEndpoint)) {
                        LoggingUtils.logger.info("An attempt to acquire non-zkApp account...")

                        val verificationKey: String? = getAccountVerificationKey(accounts[index])
                        while (verificationKey != null && accounts[index].used) {
                            LoggingUtils.logger
                                .info(
                                    "Account with index #${index} is already in use or this is the zkApp account when it is not expected!"
                                )
                            index = releaseAccountAndGetNextIndex()
                        }
                    } else {
                        LoggingUtils.logger.info("An attempt to acquire any account...")

                        while (accounts[index].used) {
                            LoggingUtils.logger
                                .info(
                                    "Account with index #${index} is already in use!"
                                )
                            index = releaseAccountAndGetNextIndex()
                        }
                    }
                    accounts[index].used = true
                    LoggingUtils.logger
                        .info(
                            "Acquired account with Index #${index} and Public Key: ${accounts[index].pk}"
                        )
                    call.respondText(
                        text = json.encodeToString(
                            serializer = Account.serializer(),
                            value = accounts[index]
                        ),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK
                    )
                } catch (e: Throwable) {
                    call.respondText(
                        text = json.encodeToString(
                            serializer = Message.serializer(),
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
                    LoggingUtils.logger.info(message)

                    call.respondText(
                        text = json.encodeToString(
                            serializer = Message.serializer(),
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
                            serializer = Message.serializer(),
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
