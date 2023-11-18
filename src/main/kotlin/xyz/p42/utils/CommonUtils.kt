package xyz.p42.utils

import kotlinx.coroutines.delay
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.code
import kotlinx.html.head
import kotlinx.html.style
import kotlinx.html.title
import xyz.p42.accountCommonPassword
import xyz.p42.accounts
import xyz.p42.accountsToBeReleased
import xyz.p42.genesisLedgerPath
import xyz.p42.graphQlEndpoint
import xyz.p42.properties.ACCOUNTS_TO_KEEP_UNUSED
import xyz.p42.properties.ACCOUNT_ACQUIRING_ATTEMPT_TIMEOUT
import xyz.p42.properties.HTML_CODE_BLOCK_STYLE
import xyz.p42.properties.IS_REGULAR_ACCOUNT_QUERY_PARAM
import xyz.p42.properties.SERVICE_TITLE
import xyz.p42.properties.UNLOCK_ACCOUNT_QUERY_PARAM
import xyz.p42.servicePort
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
           Supported Query params: 
                                   $IS_REGULAR_ACCOUNT_QUERY_PARAM=<boolean>, default: true
                                   Useful if you need to get non-zkApp account.
                                   
                                   $UNLOCK_ACCOUNT_QUERY_PARAM=<boolean>, default: false
                                   Useful if you need to get unlocked account.
           Returns JSON account key-pair:
           { pk:"", sk:"" }
           
           HTTP PUT:
           http://localhost:$servicePort/release-account
           Accepts JSON account key-pair as request payload:
           { pk:"", sk:"" }
           Returns JSON status message
           
           HTTP GET:
           http://localhost:$servicePort/list-acquired-accounts
           Returns JSON list of acquired accounts key-pairs:
           [ { pk:"", sk:"" }, ... ]
           
           HTTP PUT:
           http://localhost:$servicePort/lock-account
           Accepts JSON account key-pair as request payload:
           { pk:"", sk:"" }
           Returns JSON status message
           
           HTTP PUT:
           http://localhost:$servicePort/unlock-account
           Accepts JSON account key-pair as request payload:
           { pk:"", sk:"" }
           Returns JSON status message
           
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

fun getLockAccountGraphQlQuery(publicKey: String) =
  """
    mutation {
      lockAccount(input: {publicKey: "$publicKey"}) {
        account {
          publicKey
        }
      }
    }
    """.trimIndent()

fun getUnlockAccountGraphQlQuery(publicKey: String) =
  """
    mutation {
      unlockAccount(input: {publicKey: "$publicKey", password: "$accountCommonPassword"}) {
        account {
          publicKey
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
