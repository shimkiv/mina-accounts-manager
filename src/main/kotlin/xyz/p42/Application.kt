package xyz.p42

import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlinx.serialization.json.Json
import xyz.p42.model.Account
import xyz.p42.model.GenesisLedger
import xyz.p42.plugins.configureRouting
import xyz.p42.properties.ACCOUNT_COMMON_PASSWORD
import xyz.p42.utils.LoggingUtils
import xyz.p42.utils.getWelcomeMessage
import java.io.File

var servicePort = 8181
var graphQlPort = 8080
var genesisLedgerPath: String = "N/A"
var graphQlEndpoint: String = "N/A"
var accountCommonPassword: String = "N/A"
lateinit var accounts: List<Account>
val accountsToBeReleased: MutableList<Account> = mutableListOf()

val json = Json { ignoreUnknownKeys = true; isLenient = true }
val logger = LoggingUtils.logger

@Suppress("ExtractKtorModule")
fun main(args: Array<String>) {
  if (args.isNotEmpty()) {
    logger.info(
      "CLI args passed: ${args.joinToString(";")}"
    )
  }

  configurePorts(args)
  configureStrings(args)

  embeddedServer(CIO, port = servicePort) {
    configureInMemoryAccounts()
    configureRouting()
    logger.info(getWelcomeMessage())
  }.start(wait = true)
}

fun configurePorts(args: Array<String>) {
  if (args.isNotEmpty()) {
    servicePort = args.getOrNull(1)?.toInt() ?: servicePort
    graphQlPort = args.getOrNull(2)?.toInt() ?: graphQlPort
  }
}

fun configureStrings(args: Array<String>) {
  genesisLedgerPath =
    args.getOrNull(0)?.trim() ?: throw IllegalArgumentException("Genesis ledger path is not provided")
  graphQlEndpoint = "http://localhost:${graphQlPort}/graphql"
  accountCommonPassword = args.getOrNull(3)?.trim() ?: ACCOUNT_COMMON_PASSWORD
}

fun configureInMemoryAccounts() {
  accounts = json.decodeFromString<GenesisLedger>(File(genesisLedgerPath).readText()).ledger.accounts
}
