# Mina Accounts-Manager

---

## Native image

Make sure the following tools are installed and available within the OS `PATH`:

- `GraalVM`
- `native-image`
    - `gu install native-image` (if not installed by default)

Tested with:

```shell
openjdk 21.0.1 2023-10-17
OpenJDK Runtime Environment GraalVM CE 21.0.1+12.1 (build 21.0.1+12-jvmci-23.1-b19)
OpenJDK 64-Bit Server VM GraalVM CE 21.0.1+12.1 (build 21.0.1+12-jvmci-23.1-b19, mixed mode, sharing)

------------------------------------------------------------
Gradle 8.5-rc-2
------------------------------------------------------------

Build time:   2023-11-14 14:16:30 UTC
Revision:     9bfdf5b90ff69b6775c2fe163808a99eac2dc543

Kotlin:       1.9.20
Groovy:       3.0.17
Ant:          Apache Ant(TM) version 1.10.13 compiled on January 4 2023
JVM:          21.0.1 (GraalVM Community 21.0.1+12-jvmci-23.1-b19)
OS:           Mac OS X 14.1.1 x86_64
```

Then simply invoke the command:

```shell
./gradlew nativeCompile
```

This will give you an `accounts-manager` native application under the `build/native/nativeCompile` path.  
It can be used then as part of
the `Lightweight Mina Network` [Docker image](https://hub.docker.com/r/o1labs/mina-local-network).

---

```shell
./build/native/nativeCompile/accounts-manager <genesisLedgerPath> [servicePort] [graphQlPort] [accountCommonPassword]

-----------------------------
.:: Mina Accounts-Manager ::.
-----------------------------

Application initialized and is running at: http://localhost:8181
Available endpoints:

   HTTP GET:
   http://localhost:8181/acquire-account
   Supported Query params:
                           isRegularAccount=<boolean>, default: true
                           Useful if you need to get non-zkApp account.

                           unlockAccount=<boolean>, default: false
                           Useful if you need to get unlocked account.
   Returns JSON account key-pair:
   { pk:"", sk:"" }

   HTTP PUT:
   http://localhost:8181/release-account
   Accepts JSON account key-pair as request payload:
   { pk:"", sk:"" }
   Returns JSON status message

   HTTP GET:
   http://localhost:8181/list-acquired-accounts
   Returns JSON list of acquired accounts key-pairs:
   [ { pk:"", sk:"" }, ... ]

   HTTP PUT:
   http://localhost:8383/lock-account
   Accepts JSON account key-pair as request payload:
   { pk:"", sk:"" }
   Returns JSON status message

   HTTP PUT:
   http://localhost:8181/unlock-account
   Accepts JSON account key-pair as request payload:
   { pk:"", sk:"" }
   Returns JSON status message

Operating with:
   Mina Genesis ledger:   /root/.mina-network/mina-local-network-2-1-1/daemon.json
   Mina GraphQL endpoint: http://localhost:8080/graphql
```
