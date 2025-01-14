package net.darkmeow.remapper.mapping

import net.darkmeow.remapper.pipeline.ClassEntry
import net.darkmeow.remapper.util.annotations
import net.darkmeow.remapper.util.containsAnnotation
import net.darkmeow.remapper.util.findMixinAnnotation
import org.objectweb.asm.commons.Remapper

open class AsmRemapper(private val classMapping: ClassMapping) : Remapper() {
    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        return classMapping.get(owner)?.methodMapping?.getNameTo(name, descriptor) ?: name
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        return classMapping.get(owner)?.fieldMapping?.getNameTo(name) ?: name
    }

    override fun mapType(internalName: String?): String? {
        if (internalName == null) return null
        return classMapping.getNameTo(internalName) ?: internalName
    }
}

class MixinRemapper(
    classMapping: ClassMapping,
    classEntries: Collection<ClassEntry>
) : AsmRemapper(classMapping) {
    private val mixinClasses = classEntries.asSequence()
        .map { it.classNode }
        .filter { it.findMixinAnnotation() != null }
        .associateBy { it.name }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        val classNode = mixinClasses[owner] ?: return super.mapFieldName(owner, name, descriptor)

        var newName = ""
        classNode.fields.find { it.name == name && it.desc == descriptor }
            ?.let { fieldNode ->
                if (!fieldNode.annotations.containsAnnotation("Lorg/spongepowered/asm/mixin/Shadow;")) {
                    newName = name
                }
            }
        if (newName.isEmpty()) {
            newName = super.mapFieldName(owner, name, descriptor)
        }
        return newName
    }
}