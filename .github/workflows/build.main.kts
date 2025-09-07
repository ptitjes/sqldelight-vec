#!/usr/bin/env kotlin

@file:Repository("https://repo.maven.apache.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.5.0")
@file:Repository("https://bindings.krzeminski.it")

@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:setup-java:v5")
@file:DependsOn("actions:cache:v4")
@file:DependsOn("actions:upload-artifact:v4")
@file:DependsOn("gradle:actions__wrapper-validation:v4")

@file:DependsOn("EnricoMi:publish-unit-test-result-action:v2")
@file:DependsOn("EnricoMi:publish-unit-test-result-action__macos:v2")
@file:DependsOn("EnricoMi:publish-unit-test-result-action__windows:v2")

import io.github.typesafegithub.workflows.actions.actions.Cache
import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.enricomi.PublishUnitTestResultAction
import io.github.typesafegithub.workflows.actions.gradle.ActionsWrapperValidation
import io.github.typesafegithub.workflows.domain.Mode
import io.github.typesafegithub.workflows.domain.Permission
import io.github.typesafegithub.workflows.domain.RunnerType
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow

workflow(
    name = "Build",
    on = listOf(Push(), PullRequest()),
    sourceFile = __FILE__,
    permissions = mapOf(
        Permission.Checks to Mode.Write,
        Permission.PullRequests to Mode.Write,
    )
) {
    job(
        id = "build",
        name = """Build on ${expr("matrix.os")}""",
        strategyMatrix = mapOf(
            "os" to listOf(
                "ubuntu-latest",
                "ubuntu-24.04-arm",
                "macos-latest",
                "windows-latest",
            )
        ),
        runsOn = RunnerType.Custom(expr("matrix.os")),
    ) {
        uses(name = "Checkout", action = Checkout())

        uses(
            name = "Install Java",
            action = SetupJava(javaVersion = "17", distribution = SetupJava.Distribution.Adopt),
        )

        uses(name = "Validate gradle wrapper", action = ActionsWrapperValidation())

        uses(
            name = "Set up cache",
            action = Cache(
                path = listOf<String>("~/.gradle/caches"),
                key = expr { runner.os } + "-gradle-" + expr { hashFiles("**/*.gradle*", quote = true) },
                restoreKeys = listOf<String>(expr { runner.os } + "-gradle-"),
            ),
        )

        run(name = "Build", command = "./gradlew build -Dsplit_targets")

        uses(
            name = "Publish Test Result",
            `if` = expr("!cancelled()"),
            action = PublishUnitTestResultAction(
                checkName = """Tests on ${expr("matrix.os")}""",
                files = listOf<String>("**/build/test-results/**/*.xml"),
            ),
        )
    }
}
