package ru.pchurzin.spring.integraion.plantuml.graph

import org.junit.jupiter.api.Test
import org.springframework.integration.IntegrationPatternType.pollable_channel
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.channel.QueueChannel
import org.springframework.integration.graph.Graph
import org.springframework.integration.graph.LinkNode
import org.springframework.integration.graph.MessageChannelNode
import org.springframework.integration.graph.MessageSourceNode
import org.springframework.integration.resource.ResourceRetrievingMessageSource
import ru.pchurzin.spring.integration.plantuml.graph.writePlantUml

class PlantUmlTest {

    @Test
    fun `Should generate plantuml markup`() {
        val nodes = buildSet {
            add(MessageSourceNode(1, "source", ResourceRetrievingMessageSource("*"), null, null))
            add(MessageChannelNode(2, "channel", DirectChannel()))
        }
        val links = buildSet {
            add(LinkNode(1, 2, LinkNode.Type.input))
        }
        val graph = Graph(emptyMap(), nodes, links)

        val appendable = StringBuilder()
        graph.writePlantUml(appendable)

        assert(
            appendable.toString() == """
            @startuml
            !includeurl https://raw.githubusercontent.com/plantuml-stdlib/EIP-PlantUML/main/dist/EIP-PlantUML.puml
            HIDE_STEREOTYPES()

            ChannelAdapterRight(node_1, "source")
            MsgChannel(node_2, "channel")

            Send(node_1, node_2)
            @enduml

        """.trimIndent()
        )
    }

    @Test
    fun `Should generate plantuml markup with custom labels`() {
        val nodes = buildSet {
            add(MessageSourceNode(1, "org.example.source", ResourceRetrievingMessageSource("*"), null, null))
            add(MessageChannelNode(2, "channel", DirectChannel()))
            add(MessageChannelNode(3, "channel2", DirectChannel()))
        }
        val links = buildSet {
            add(LinkNode(1, 2, LinkNode.Type.input))
            add(LinkNode(1, 3, LinkNode.Type.input))
        }
        val graph = Graph(emptyMap(), nodes, links)

        val appendable = StringBuilder()
        graph.writePlantUml(appendable) {
            label {
                name.substringAfterLast(".")
            }
        }
        assert(
            appendable.toString() == """
                @startuml
                !includeurl https://raw.githubusercontent.com/plantuml-stdlib/EIP-PlantUML/main/dist/EIP-PlantUML.puml
                HIDE_STEREOTYPES()

                ChannelAdapterRight(node_1, "source")
                MsgChannel(node_2, "channel")
                MsgChannel(node_3, "channel2")

                Send(node_1, node_2)
                Send(node_1, node_3)
                @enduml

        """.trimIndent()
        )
    }

    @Test
    fun `Should hide stereotypes`() {
        val graph = Graph(emptyMap(), emptySet(), emptySet())
        val appendable = StringBuilder()
        graph.writePlantUml(appendable) {
            hideStereotypes()
        }

        assert(appendable.toString().contains("HIDE_STEREOTYPES()"))
    }

    @Test
    fun `Should not hide stereotypes`() {
        val graph = Graph(emptyMap(), emptySet(), emptySet())
        val appendable = StringBuilder()
        graph.writePlantUml(appendable) {
            showStereotypes()
        }

        assert(!appendable.toString().contains("HIDE_STEREOTYPES()"))
    }

    @Test
    fun `Should generate plantuml markup with custom stereotypes`() {
        val nodes = buildSet {
            add(MessageSourceNode(1, "source", ResourceRetrievingMessageSource("*"), null, null))
            add(MessageChannelNode(2, "channel", QueueChannel()))
        }
        val links = buildSet {
            add(LinkNode(1, 2, LinkNode.Type.input))
        }
        val graph = Graph(emptyMap(), nodes, links)
        val appendable = StringBuilder()
        graph.writePlantUml(appendable) {
            showStereotypes()
            stereotype {
                when(integrationPatternType) {
                    pollable_channel -> "<\$polling_consumer>"
                    else -> null
                }
            }
        }
        assert(appendable.toString().contains("<<\$polling_consumer>>"))
    }
}