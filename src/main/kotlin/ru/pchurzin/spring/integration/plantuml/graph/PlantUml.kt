package ru.pchurzin.spring.integration.plantuml.graph

import org.springframework.integration.IntegrationPatternType
import org.springframework.integration.IntegrationPatternType.*
import org.springframework.integration.graph.Graph
import org.springframework.integration.graph.IntegrationNode
import org.springframework.integration.graph.LinkNode

fun Graph.writePlantUml(appendable: Appendable, configure: ConfigScope.() -> Unit = {}) {
    writePlantUml(this, appendable, configure)
}

@JvmName("writePlantUmlStatic")
private fun writePlantUml(graph: Graph, appendable: Appendable, configure: ConfigScope.() -> Unit = {}) {
    val config = ConfigBuilder().apply(configure).build()
    appendable.appendLine("@startuml")
    appendable.appendLine("!includeurl https://raw.githubusercontent.com/plantuml-stdlib/EIP-PlantUML/main/dist/EIP-PlantUML.puml")

    if (config.hideStereotypes) {
        appendable.appendLine("HIDE_STEREOTYPES()")
    }

    appendable.appendLine()

    graph.nodes.forEach { node ->
        appendable.appendLine(node.plantUmlDeclaration(config))
    }

    appendable.appendLine()

    graph.links.forEach { link ->
        appendable.appendLine(link.plantUmlDeclaration())
    }

    appendable.appendLine("@enduml")
}

@DslMarker
annotation class ConfigDsl

@ConfigDsl
interface ConfigScope {
    fun label(labelGenerator: IntegrationNode.() -> String)
    fun stereotype(stereotypeGenerator: IntegrationNode.() -> String?)
    fun hideStereotypes()
    fun showStereotypes()
}

private class ConfigBuilder : ConfigScope {
    private var labelGenerator: (IntegrationNode) -> String = { it.name }

    override fun label(labelGenerator: (IntegrationNode) -> String) {
        this.labelGenerator = labelGenerator
    }

    private var stereotypeGenerator: (IntegrationNode) -> String? = { null }

    override fun stereotype(stereotypeGenerator: (IntegrationNode) -> String?) {
        this.stereotypeGenerator = stereotypeGenerator
    }

    private var hideStereotypes: Boolean = true

    override fun hideStereotypes() {
        hideStereotypes = true
    }

    override fun showStereotypes() {
        hideStereotypes = false
    }

    fun build(): Config = Config(
        labelGenerator,
        stereotypeGenerator,
        hideStereotypes
    )
}

private data class Config(
    val labelGenerator: (IntegrationNode) -> String,
    val stereotypeGenerator: (IntegrationNode) -> String?,
    val hideStereotypes: Boolean,
)

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

private fun IntegrationNode.plantUmlDeclaration(config: Config): String = buildString {
    append(integrationPatternType!!.plantUmlName)
    append("(node_")
    append(nodeId)
    append(", \"")
    append(config.labelGenerator(this@plantUmlDeclaration))
    append("\")")
}

private fun LinkNode.plantUmlDeclaration() =
    "Send(node_$from, node_$to)"
