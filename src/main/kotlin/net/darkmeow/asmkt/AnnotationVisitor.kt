package net.darkmeow.asmkt

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AnnotationNode
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap as O2OHashMap

typealias AnnotationValueVisit<T> = (value: T) -> Unit
typealias AnnotationArrayVisit<T> = (values: List<T>) -> Unit
typealias AnnotationEnumVisit = (value: String) -> Unit
typealias AnnotationAnnotationVisit = net.darkmeow.asmkt.AnnotationVisitorBuilder.() -> Unit

class AnnotationVisitorBuilder {
    private val valueVisits = O2OHashMap<Class<*>, O2OHashMap<String, net.darkmeow.asmkt.AnnotationValueVisit<Any>>>()
    private val arrayVisits = O2OHashMap<Class<*>, O2OHashMap<String, net.darkmeow.asmkt.AnnotationArrayVisit<Any>>>()
    private val enumVisits = O2OHashMap<String, O2OHashMap<String, net.darkmeow.asmkt.AnnotationEnumVisit>>()
    private val annotationVisits = O2OHashMap<String, O2OHashMap<String, net.darkmeow.asmkt.AnnotationAnnotationVisit>>()

    fun <T> visitValue(type: Class<T>, name: String, block: net.darkmeow.asmkt.AnnotationValueVisit<T>) {
        @Suppress("UNCHECKED_CAST")
        require(valueVisits.getOrPut(type, ::O2OHashMap).put(name, block as net.darkmeow.asmkt.AnnotationValueVisit<Any>) == null) { "Already visited $name:$type" }
    }

    inline fun <reified T> visitValue(name: String, noinline block: net.darkmeow.asmkt.AnnotationValueVisit<T>) {
        visitValue(T::class.java, name, block)
    }

    fun <T> visitArray(type: Class<T>, name: String, block: net.darkmeow.asmkt.AnnotationArrayVisit<T>) {
        @Suppress("UNCHECKED_CAST")
        require(arrayVisits.getOrPut(type, ::O2OHashMap).put(name, block as net.darkmeow.asmkt.AnnotationArrayVisit<Any>) == null) { "Already visited array $name:$type" }
    }

    inline fun <reified T> visitArray(name: String, noinline block: net.darkmeow.asmkt.AnnotationArrayVisit<T>) {
        visitArray(T::class.java, name, block)
    }

    fun visitEnum(name: String, desc: String, block: net.darkmeow.asmkt.AnnotationEnumVisit) {
        require(enumVisits.getOrPut(name, ::O2OHashMap).put(desc, block) == null) { "Already visited enum $name:$desc" }
    }

    fun visitAnnotation(name: String, desc: String, block: net.darkmeow.asmkt.AnnotationAnnotationVisit) {
        require(annotationVisits.getOrPut(name, ::O2OHashMap).put(desc, block) == null) { "Already visited annotation $name:$desc" }
    }

    fun build(): AnnotationVisitor {
        return object : AnnotationVisitor(Opcodes.ASM9) {
            override fun visit(name: String, value: Any?) {
                if (value != null) {
                    valueVisits[value::class.java]?.get(name)?.invoke(value)
                }
            }

            override fun visitArray(name: String): AnnotationVisitor {
                return object : AnnotationVisitor(Opcodes.ASM9) {
                    private var values: ObjectArrayList<Any>? = null
                    private var type: Class<*>? = null

                    override fun visit(name: String?, value: Any) {
                        if (values == null) {
                            values = ObjectArrayList<Any>()
                            type = value::class.java
                        } else {
                            require(type == value::class.java) { "Array type mismatch" }
                        }
                        values!!.add(value)
                    }

                    override fun visitAnnotation(e: String?, descriptor: String?): AnnotationVisitor? {
                        return annotationVisits[name]?.get(descriptor)?.let { net.darkmeow.asmkt.AnnotationVisitorBuilder()
                            .apply(it).build() }
                    }

                    override fun visitEnd() {
                        values?.let { arrayVisits[type!!]?.get(name)?.invoke(it) }
                    }
                }
            }

            override fun visitEnum(name: String, descriptor: String, value: String) {
                enumVisits[name]?.get(descriptor)?.invoke(value)
            }

            override fun visitAnnotation(name: String, descriptor: String): AnnotationVisitor? {
                return annotationVisits[name]?.get(descriptor)?.let { net.darkmeow.asmkt.AnnotationVisitorBuilder().apply(it).build() }
            }
        }
    }
}

inline fun AnnotationNode.visit(block: net.darkmeow.asmkt.AnnotationVisitorBuilder.() -> Unit) {
    accept(net.darkmeow.asmkt.AnnotationVisitorBuilder().apply(block).build())
}