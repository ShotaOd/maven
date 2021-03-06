package org.carbon.objects.validation.evaluation

import org.carbon.objects.validation.Describe

class Key(private val keys: List<KeyNode>) : List<KeyNode> by keys, Describe {
    companion object {
        val Undefined = Key(emptyList())
    }

    val qualifiedName: String
        get() = keys.map(KeyNode::qualifiedName).joinToString(".")

    override fun describe(i: Int): String = qualifiedName
}

class KeyNode(
        val name: String,
        private val index: Int? = null
) {

    val qualifiedName: String
        get() = "$name${index?.let { "[$it]" } ?: ""}"
}

interface KeyModifier {
    fun modify(base: Key): Key

    operator fun plus(other: KeyModifier): KeyModifier = CombinedModifier(this, other)

    private class CombinedModifier(val head: KeyModifier, val tail: KeyModifier) : KeyModifier {
        override fun modify(base: Key): Key = tail
                .modify(base)
                .let { head.modify(it) }
    }
}

class IndexModifier(private val i: Int) : KeyModifier {
    override fun modify(base: Key): Key {
        val last = base.last()
        return Key(base.subList(0, base.lastIndex) + KeyNode(last.name, i))
    }
}

class HeadModifier(private val parent: Key) : KeyModifier {
    override fun modify(base: Key): Key = Key(parent + base)
}

object NoopModifier : KeyModifier {
    override fun modify(base: Key): Key = base
}

abstract class KeyAppender : KeyModifier {
    override fun modify(base: Key): Key = append()
    protected abstract fun append(): Key
}

class NameAppender(private val name: String) : KeyAppender() {
    override fun append(): Key = Key(listOf(KeyNode(name)))
}
