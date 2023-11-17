package xyz.p42.model

import kotlinx.serialization.Serializable

@Serializable
data class VerificationKey(
  val verificationKey: String?
)

@Serializable
data class AccountWrapper(
  val publicKey: String
)

@Serializable
data class VkWrapper(
  val verificationKey: VerificationKey?
)

@Serializable
data class UnlockAccount(
  val account: AccountWrapper
)

@Serializable
data class VkAccountWrapper(
  val account: VkWrapper
)

@Serializable
data class UnlockAccountWrapper(
  val unlockAccount: UnlockAccount
)

@Serializable
data class VkGraphQlResponse(
  val data: VkAccountWrapper
)

@Serializable
data class UnlockAccountGraphQlResponse(
  val data: UnlockAccountWrapper
)

@Serializable
data class GraphQlPayload(
  val query: String,
  val variables: Map<String, String> = mapOf(),
  val operationName: String? = null
)

@Serializable
data class Message(
  val code: Int = 200,
  val message: String
)

@Serializable
data class Account(
  var used: Boolean = false,
  val pk: String,
  val sk: String? = null
)

@Serializable
data class Ledger(
  val name: String,
  val accounts: List<Account>
)

@Serializable
data class GenesisLedger(
  val ledger: Ledger
)
