package xyz.p42

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.serialization.json.Json
import xyz.p42.model.Account
import xyz.p42.model.GenesisLedger
import xyz.p42.plugins.configureRouting
import xyz.p42.utils.LoggingUtils
import xyz.p42.utils.getWelcomeMessage
import java.io.File

const val servicePort = 8181
var genesisLedgerPath: String = "N/A"
var graphQlEndpoint: String = "N/A"
lateinit var accounts: List<Account>
val accountsToBeReleased: MutableList<Account> = mutableListOf()

val json = Json { ignoreUnknownKeys = true; isLenient = true }

@Suppress("ExtractKtorModule")
fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        LoggingUtils.logger.info(
            "CLI args passed: ${args.joinToString(";")}"
        )
    }

    embeddedServer(CIO, port = servicePort) {
        genesisLedgerPath = args[0].trim()
        graphQlEndpoint = "http://localhost:${args[1].toInt()}/graphql"
        accounts = json.decodeFromString<GenesisLedger>(File(genesisLedgerPath).readText()).ledger.accounts

        configureRouting()

        LoggingUtils.logger.info(getWelcomeMessage())
    }.start(wait = true)
}
