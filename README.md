# Mina Accounts-Manager

---

## Native image

Make sure the following tools are installed and available within the OS `PATH`:

- `GraalVM`
- `native-image`
    - `gu install native-image`

Tested with:

```shell
openjdk 19.0.1 2022-10-18
OpenJDK Runtime Environment GraalVM CE 22.3.0 (build 19.0.1+10-jvmci-22.3-b08)
OpenJDK 64-Bit Server VM GraalVM CE 22.3.0 (build 19.0.1+10-jvmci-22.3-b08, mixed mode, sharing)
```

Then simply invoke the command:

```shell
./gradlew nativeCompile
```

This will give you an `accounts-manager` native application under the `build/native/nativeCompile` path, which you can
then use as part of `Lightweight Mina Network` Docker image generation procedure.
