package ru.pchurzin.spring.integraion.plantuml.graph

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.integration.IntegrationPatternType.pollable_channel
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.PollerSpec
import org.springframework.integration.dsl.Pollers
import org.springframework.integration.dsl.integrationFlow
import org.springframework.integration.graph.IntegrationGraphServer
import org.springframework.messaging.MessageChannel
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.pchurzin.spring.integration.plantuml.graph.writePlantUml

@ExtendWith(SpringExtension::class)
class PlantUmlTest {

    @Autowired
    private lateinit var graphServer: IntegrationGraphServer

    @Test
    fun `Should generate plantuml markup with default configuration`() {
        val appendable = StringBuilder()

        graphServer.graph.writePlantUml(appendable)

        assert(appendable hasContentOfResource "default.puml")
    }

    @Test
    fun `Should hide stereotypes`() {

        val appendable = StringBuilder()
        graphServer.graph.writePlantUml(appendable) {
            hideStereotypes()
        }

        assert(appendable hasContentOfResource "hide-stereotypes.puml")
    }

    @Test
    fun `Should show stereotypes`() {
        val appendable = StringBuilder()

        graphServer.graph.writePlantUml(appendable) {
            showStereotypes()
        }

        assert(appendable hasContentOfResource "show-stereotypes.puml")
    }

    @Test
    fun `Should generate plantuml markup with custom labels`() {
        val appendable = StringBuilder()

        graphServer.graph.writePlantUml(appendable) {
            label {
                name.substringAfterLast(".")
            }
        }

        assert(appendable hasContentOfResource "custom-labels.puml")
    }

    @Test
    fun `Should generate plantuml markup with custom stereotypes`() {
        val appendable = StringBuilder()

        graphServer.graph.writePlantUml(appendable) {
            showStereotypes()
            stereotype {
                when (integrationPatternType) {
                    pollable_channel -> "<\$polling_consumer>"
                    else -> null
                }
            }
        }

        assert(appendable hasContentOfResource "custom-stereotypes.puml")
    }

    @Test
    fun `Should generate plantuml markup with custom colors`() {
        val appendable = StringBuilder()

        graphServer.graph.writePlantUml(appendable) {
            color {
                when (integrationPatternType) {
                    pollable_channel -> "red"
                    else -> null
                }
            }
        }

        assert(appendable hasContentOfResource "custom-colors.puml")
    }

    @Configuration
    @EnableIntegration
    open class Config {

        @Bean
        open fun integrationGraphServer() = IntegrationGraphServer()

        @Bean
        open fun startChannel(): MessageChannel = QueueChannel()

        @Bean
        open fun testPoller(): PollerSpec = Pollers.fixedRate(1000)

        @Bean
        open fun testFlow(): IntegrationFlow = integrationFlow(startChannel()) {
            transformWith {
                poller(testPoller())
                transformer<String> { it.uppercase() }
            }
            filter<String>({ it.startsWith("A") }) {
                discardChannel("errorChannel")
            }
            transformWith {
                transformer<String> { it.lowercase() }
            }
            channel("nullChannel")
        }
    }
}

private infix fun Appendable.hasContentOfResource(resourceName: String) =
    ClassPathResource(resourceName).getContentAsString(Charsets.UTF_8) == toString()
