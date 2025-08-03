package ru.pchurzin.spring.integration.plantuml.graph

import org.springframework.integration.IntegrationPatternType
import org.springframework.integration.IntegrationPatternType.*
import org.springframework.integration.graph.Graph
import org.springframework.integration.graph.IntegrationNode
import org.springframework.integration.graph.LinkNode
import java.util.*

fun Graph.writePlantUml(appendable: Appendable, configure: ConfigScope.() -> Unit = defaultConfig) {
    writePlantUml(this, appendable, configure)
}

@JvmName("writePlantUmlStatic")
private fun writePlantUml(graph: Graph, appendable: Appendable, configure: ConfigScope.() -> Unit = defaultConfig) {
    val config = ConfigBuilder().apply(configure).build()
    val graphWithoutImplicitChannels = if (config.showImplicitChannels) graph else graph.withoutImplicitChannels()
    appendable.appendLine("@startuml")
    appendable.appendLine("!includeurl https://raw.githubusercontent.com/plantuml-stdlib/EIP-PlantUML/main/dist/EIP-PlantUML.puml")

    if (config.hideStereotypes) {
        appendable.appendLine("HIDE_STEREOTYPES()")
    }

    appendable.appendLine()

    val nodes = visibleNodes(graphWithoutImplicitChannels, config)
    nodes.forEach { node ->
        appendable.appendLine(node.plantUmlDeclaration(config))
    }

    appendable.appendLine()

    val links = visibleLinks(graphWithoutImplicitChannels, nodes)
    links.forEach { link ->
        appendable.appendLine(link.plantUmlDeclaration())
    }

    appendable.appendLine("@enduml")
}

private fun Graph.withoutImplicitChannels(): Graph {
    val implicitChannelNodeIds =
        nodes.filter { it.name.matches(internalChannelNameRegex) }.map(IntegrationNode::getNodeId)
    val fromNodeIdToLinkNodes = links.associateBy(LinkNode::getFrom)
    val toNodeIdToLinkNodes = links.associateBy(LinkNode::getTo)
    val newLinks = implicitChannelNodeIds.mapNotNull { nodeId ->
        val fromNodeId = toNodeIdToLinkNodes[nodeId]?.from
        val toNodeId = fromNodeIdToLinkNodes[nodeId]?.to
        if (fromNodeId == null || toNodeId == null) {
            null
        } else {
            LinkNode(fromNodeId, toNodeId, LinkNode.Type.route)
        }
    }
    val filteredNodes = nodes.filter { it.nodeId !in implicitChannelNodeIds }
    val filteredLinks = links.filter { it.from !in implicitChannelNodeIds && it.to !in implicitChannelNodeIds }
    return Graph(contentDescriptor, filteredNodes, filteredLinks + newLinks)
}

private val internalChannelNameRegex = Regex(".+channel#\\d+$")

private fun visibleNodes(graph: Graph, config: Config): Collection<IntegrationNode> {
    if (config.startNodeSelectors.isEmpty()) return graph.nodes
    val nodeIdToNode = graph.nodes.associateBy(IntegrationNode::getNodeId)
    val result = hashSetOf<IntegrationNode>()
    val startNodes = graph.nodes.filter { it.satisfiesAny(config.startNodeSelectors) }
    val queue: Queue<IntegrationNode> = LinkedList(startNodes)
    while (queue.isNotEmpty()) {
        val node = queue.remove()
        result.add(node)
        graph.links
            .filter { it.from == node.nodeId }
            .forEach { link -> queue.add(nodeIdToNode[link.to]) }
    }
    return result
}

private fun IntegrationNode.satisfiesAny(selectors: Collection<(IntegrationNode) -> Boolean>): Boolean =
    selectors.any { it(this) }

private fun visibleLinks(graph: Graph, visibleNodes: Collection<IntegrationNode>): Collection<LinkNode> {
    val visibleNodeIds = visibleNodes.map(IntegrationNode::getNodeId).toSet()
    return graph.links.filter { it.from in visibleNodeIds && it.to in visibleNodeIds }.toSet()
}

@DslMarker
annotation class ConfigDsl

@ConfigDsl
interface ConfigScope {
    fun label(labelGenerator: IntegrationNode.() -> String)
    fun stereotype(stereotypeGenerator: IntegrationNode.() -> String?)
    fun hideStereotypes()
    fun showStereotypes()
    fun color(colorGenerator: IntegrationNode.() -> String?)
    fun startWith(nodeSelector: IntegrationNode.() -> Boolean)
    fun showImplicitChannels(show: Boolean = false)
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

    private var colorGenerator: (IntegrationNode) -> String? = { null }

    override fun color(colorGenerator: (IntegrationNode) -> String?) {
        this.colorGenerator = colorGenerator
    }

    private var startNodeSelectors: MutableSet<(IntegrationNode) -> Boolean> = mutableSetOf()

    override fun startWith(nodeSelector: (IntegrationNode) -> Boolean) {
        startNodeSelectors.add(nodeSelector)
    }

    private var showImplicitChannels: Boolean = false

    override fun showImplicitChannels(show: Boolean) {
        showImplicitChannels = show
    }

    fun build(): Config = Config(
        labelGenerator,
        stereotypeGenerator,
        hideStereotypes,
        colorGenerator,
        startNodeSelectors,
        showImplicitChannels,
    )
}

private data class Config(
    val labelGenerator: (IntegrationNode) -> String,
    val stereotypeGenerator: (IntegrationNode) -> String?,
    val hideStereotypes: Boolean,
    val colorGenerator: (IntegrationNode) -> String?,
    val startNodeSelectors: Set<(IntegrationNode) -> Boolean>,
    val showImplicitChannels: Boolean,
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

    if (!config.hideStereotypes) {
        val stereotype = config.stereotypeGenerator(this@plantUmlDeclaration)
        if (!stereotype.isNullOrBlank()) {
            append(" <")
            append(stereotype)
            append(">")
        }
    }

    val color = config.colorGenerator(this@plantUmlDeclaration)
    if (!color.isNullOrBlank()) {
        append(" #")
        append(color)
    }
}

private fun LinkNode.plantUmlDeclaration() =
    "Send(node_$from, node_$to)"

val defaultConfig: ConfigScope.() -> Unit = {
    hideStereotypes()
    label { name }
    stereotype {
        when (integrationPatternType) {
            pollable_channel -> "<\$polling_consumer>"
            else -> null
        }
    }
}
