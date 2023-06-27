package xyz.p42.utils

import kotlinx.coroutines.delay
import kotlinx.html.*
import xyz.p42.*
import xyz.p42.properties.*
import kotlin.random.Random

fun getWelcomeMessage() =
    """
                
                
        -----------------------------
        .:: $SERVICE_TITLE ::.
        -----------------------------
        
        Application initialized and is running at: http://localhost:$servicePort
        Available endpoints:
        
           HTTP GET:
           http://localhost:$servicePort/acquire-account
           Supported Query params: $IS_REGULAR_ACCOUNT_QUERY_PARAM=<boolean>, default: true
                                   Useful if you need to get non-zkApp account.
           Returns Account JSON: 
           { pk:"", sk:"" }
           
           HTTP PUT:
           http://localhost:$servicePort/release-account
           Accepts Account JSON as request payload: 
           { pk:"", sk:"" }
           
        Operating with:
           Mina Genesis ledger:   $genesisLedgerPath
           Mina GraphQL endpoint: $graphQlEndpoint
           
    """.trimIndent()

fun getErrorHtml(cause: String, block: HTML): HTML {
    block.head {
        title(SERVICE_TITLE)
    }
    block.body {
        code {
            style = HTML_CODE_BLOCK_STYLE
            +"""
                
                
                -----------------------------
                .:: $SERVICE_TITLE ::.
                -----------------------------
        
                $cause
           
            """.trimIndent()
        }
    }

    return block
}

fun getAccountVkGraphQlQuery(publicKey: String) =
    """
    {
      account(publicKey: "$publicKey") {
        verificationKey {
          verificationKey
        }
      }
    }
    """.trimIndent()

suspend fun releaseAccountAndGetNextIndex(): Int {
    if (accounts.isNotEmpty()) {
        val publicKey = accountsToBeReleased.removeLast().pk
        accounts[accounts.indexOfFirst {
            it.pk == publicKey
        }].used = false
    }
    delay(ACCOUNT_ACQUIRING_ATTEMPT_TIMEOUT)
    return Random.nextInt(0, accounts.size - ACCOUNTS_TO_KEEP_UNUSED)
}
