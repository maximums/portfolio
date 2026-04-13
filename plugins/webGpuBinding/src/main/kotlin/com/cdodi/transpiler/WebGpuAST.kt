package com.cdodi.transpiler

import com.cdodi.transpiler.BindingSlices.CUSTOM_TYPE
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT

private const val SLICE_DELIMITER = "\n---------------------------------------------------------------------------------------------------------------------\n"

interface BindingContext {
    operator fun <K, V>get(slice: Slice<K, V>, key: K): V?
}

class MutableBidingContext : BindingContext {
    private val storage = mutableMapOf<Slice<*, *>, MutableMap<Any, Any>>()

    operator fun <K: Any, V: Any> set(slice: Slice<K, V>, key: K, value: V) {
        storage.getOrPut(slice) { mutableMapOf() }[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <K, V> get(slice: Slice<K, V>, key: K): V? = storage[slice]?.get(key as Any) as? V

    operator fun get(slice: Slice<*, *>): Map<Any, Any>? = storage[slice]

    override fun toString(): String = storage.entries.joinToString(separator = SLICE_DELIMITER) { entry ->
        "${entry.key.name} -- ${entry.value.entries.joinToString(separator = "\n")}"
    }
}

data class Slice<K, V>(val name: String)

object BindingSlices {
    val INTERFACE = Slice<String, Descriptor.InterfaceDescriptor>("INTERFACE")
    val PARTIAL_INTERFACE = Slice<String, Descriptor.InterfaceDescriptor>("PARTIAL_INTERFACE")
    val FAKE_INTERFACE = Slice<String, Descriptor.InterfaceDescriptor>("MIXIN")
    val CUSTOM_TYPE = Slice<String, String>("CUSTOM_TYPE")
}

sealed interface Descriptor {
    val name: String

    data class TypeDescriptor(
        override val name: String,
        val isNullable: Boolean = true,
        val unionMembers: Set<TypeDescriptor>? = null,
        val record: Map<TypeDescriptor, TypeDescriptor>? = null,
        val sequenceOf: TypeDescriptor? = null,
        val promiseOf: TypeDescriptor? = null
    ) : Descriptor

    data class InterfaceDescriptor(
        override val name: String,
        val members: Set<InterfaceMember>,
        val superTypes: Set<String>,
    ) : Descriptor {
        val isAbstractClass: Boolean
            get() = members.any { it.name == "constructor" }

        operator fun plus(other: InterfaceDescriptor?): InterfaceDescriptor {
            if (other == null) return this

            return copy(members = members + other.members, superTypes = superTypes + other.superTypes)
        }
    }
}

sealed interface InterfaceMember : Descriptor {

    data class VariableDescriptor(
        override val name: String,
        val type: Descriptor.TypeDescriptor,
        val defaultValue: String? = null
    ) : InterfaceMember

    data class FunctionDescriptor(
        override val name: String,
        val returnType: Descriptor.TypeDescriptor,
        val parameters: Set<VariableDescriptor>,
    ) : InterfaceMember
}