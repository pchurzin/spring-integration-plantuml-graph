package ru.pchurzin.spring.integraion.plantuml.graph

import org.junit.jupiter.api.Test
import org.springframework.integration.channel.DirectChannel
import org.springframework.integration.graph.Graph
import org.springframework.integration.graph.LinkNode
import org.springframework.integration.graph.MessageChannelNode
import org.springframework.integration.graph.MessageSourceNode
import org.springframework.integration.resource.ResourceRetrievingMessageSource
import ru.pchurzin.spring.integration.plantuml.graph.writePlantUml

class PlantUmlTest {

    @Test
    fun test() {
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

        assert(appendable.toString() == """
            @startuml
            !includeurl https://raw.githubusercontent.com/plantuml-stdlib/EIP-PlantUML/main/dist/EIP-PlantUML.puml
            HIDE_STEREOTYPES()

            ChannelAdapterRight(node_1, "source")
            MsgChannel(node_2, "channel")

            Send(node_1, node_2)
            @enduml

        """.trimIndent())
    }
}