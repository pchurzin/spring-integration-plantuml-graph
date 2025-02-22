package ru.pchurzin.spring.integration.plantuml.graph

import org.springframework.integration.IntegrationPatternType
import org.springframework.integration.IntegrationPatternType.*
import org.springframework.integration.graph.Graph
import org.springframework.integration.graph.IntegrationNode
import org.springframework.integration.graph.LinkNode

fun Graph.writePlantUml(appendable: Appendable) = generatePlantUml(this, appendable)

fun generatePlantUml(graph: Graph, appendable: Appendable) {
    appendable.appendLine("@startuml")
    appendable.appendLine("!includeurl https://raw.githubusercontent.com/plantuml-stdlib/EIP-PlantUML/main/dist/EIP-PlantUML.puml")
    appendable.appendLine("HIDE_STEREOTYPES()")
    appendable.appendLine()

    graph.nodes.forEach { node ->
        appendable.appendLine(node.plantUmlDeclaration())
    }

    appendable.appendLine()

    graph.links.forEach { link ->
        appendable.appendLine(link.plantUmlDeclaration())
    }

    appendable.appendLine("@enduml")
}

private val IntegrationPatternType.plantUmlName
    get() = when (this) {
        message_channel -> "MsgChannel"
        publish_subscribe_channel -> "PubSubChannel"
        executor_channel -> "MsgChannel"
        pollable_channel -> "MsgChannel"
        reactive_channel -> "MsgChannel"
        null_channel -> "MsgChannel"
        bridge -> "MsgBridge"
        service_activator -> "ServiceActivator"
        outbound_channel_adapter -> "ChannelAdapterLeft"
        inbound_channel_adapter -> "ChannelAdapterRight"
        outbound_gateway -> "MessagingGateway"
        inbound_gateway -> "MessagingGateway"
        splitter -> "Splitter"
        transformer -> "MessageTranslator"
        header_enricher -> "DataEnricher"
        filter -> "Filter"
        content_enricher -> "DataEnricher"
        header_filter -> "Filter"
        claim_check_in -> "Item"
        claim_check_out -> "Item"
        aggregator -> "Aggregator"
        resequencer -> "Resequencer"
        barrier -> "MessageRouter"
        chain -> "MessageRouter"
        scatter_gather -> "MessageRouter"
        delayer -> "MessageRouter"
        control_bus -> "ControlBus"
        router -> "MessageRouter"
        recipient_list_router -> "RecipientList"
    }

private fun IntegrationNode.plantUmlDeclaration() =
    "${integrationPatternType!!.plantUmlName}(node_$nodeId, \"$name\")"

private fun LinkNode.plantUmlDeclaration() =
    "Send(node_$from, node_$to)"