package ru.alkoleft.context.platform.exporter

import com.github._1c_syntax.bsl.context.api.Context
import com.github._1c_syntax.bsl.context.platform.PlatformGlobalContext
import org.slf4j.LoggerFactory
import ru.alkoleft.context.platform.dto.*
import java.io.BufferedWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.function.Supplier
import java.util.stream.Stream

class ContextExporter(
    private val logic: ExporterLogic = BaseExporterLogic()
) : Exporter {

    companion object {
        private const val SEPARATOR = "----------------------------------------"
        private val log = LoggerFactory.getLogger(ContextExporter::class.java)
    }

    @Throws(IOException::class)
    private fun appendLine(writer: BufferedWriter, line: String) {
        writer.write(line)
        writer.newLine()
    }

    @Throws(IOException::class)
    private fun appendIfNotNullOrEmpty(writer: BufferedWriter, prefix: String, value: String?) {
        if (!value.isNullOrEmpty()) {
            appendLine(writer, prefix + value)
        }
    }

    @Throws(IOException::class)
    private fun writeSnippetStart(writer: BufferedWriter, title: String, description: String?) {
        appendLine(writer, "TITLE: $title")
        appendIfNotNullOrEmpty(writer, "DESCRIPTION: ", description)
    }

    @Throws(IOException::class)
    private fun writeSeparator(writer: BufferedWriter, firstSnippet: Boolean) {
        if (!firstSnippet) {
            writer.newLine()
            appendLine(writer, SEPARATOR)
            writer.newLine()
        }
    }

    @Throws(IOException::class)
    private fun <T> writeEntries(
        file: Path,
        exportMessageFormat: String,
        streamSupplier: Supplier<Stream<T>>,
        entryWriterFunc: EntryWriter<T>
    ) {
        log.info(exportMessageFormat, file.toAbsolutePath())

        Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use { writer ->
            streamSupplier.get().use { entryStream ->
                val iterator = entryStream.iterator()
                var isFirst = true

                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    writeSeparator(writer, isFirst)
                    entryWriterFunc.write(writer, entry)
                    isFirst = false
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun writeProperties(context: PlatformGlobalContext, output: Path) {
        writeEntries(
            output,
            "Exporting global properties to: {}",
            { logic.extractProperties(context) }
        ) { writer, property ->
            writeSnippetStart(writer, "Global Property: ${property.name}", property.description)
            appendLine(writer, "Name: ${property.name}")
            appendLine(writer, "Readonly: ${property.readonly}")
            appendIfNotNullOrEmpty(writer, "Type: ", property.type)
        }
    }

    @Throws(IOException::class)
    override fun writeMethods(context: PlatformGlobalContext, output: Path) {
        writeEntries(
            output,
            "Exporting global methods to: {}",
            { logic.extractMethods(context) }
        ) { writer, method ->
            writeSnippetStart(writer, "Global Method: ${method.name}", method.description)
            appendLine(writer, "Name: ${method.name}")
            writer.newLine()
            appendLine(writer, "Signatures:")

            if (method.signature.isNotEmpty()) {
                for (sig in method.signature) {
                    appendLine(writer, "  ---")
                    val signatureLine = formatCompleteSignature(method, sig)
                    appendLine(writer, "  $signatureLine")
                    appendIfNotNullOrEmpty(writer, "    Description: ", sig.description)
                    appendSignatureParameterDescriptions(writer, sig.params, "    ")
                }
            } else {
                val returnTypeString = if (method.returnType.isNotEmpty()) ":${method.returnType}" else ""
                appendLine(writer, "  ${method.name}()$returnTypeString")
                appendLine(writer, "    (No specific parameter details provided for this general signature)")
            }
        }
    }

    @Throws(IOException::class)
    private fun appendTypeProperties(writer: BufferedWriter, properties: List<PropertyDefinition>?) {
        writer.newLine()
        appendLine(writer, "Properties:")
        if (!properties.isNullOrEmpty()) {
            for (prop in properties) {
                val propType = if (!prop.type.isNullOrEmpty()) prop.type else "any"
                val readonlyStatus = if (prop.readonly) "readonly" else "readwrite"
                val propDesc = if (!prop.description.isNullOrEmpty()) " - ${prop.description}" else ""
                var namePart = prop.name
                if (!prop.nameEn.isNullOrEmpty()) {
                    namePart += " (${prop.nameEn})"
                }
                appendLine(writer, "  - $namePart: $propType ($readonlyStatus)$propDesc")
            }
        } else {
            appendLine(writer, "  (No properties)")
        }
    }

    private fun formatParametersForSignature(params: List<ParameterDefinition>?): String {
        if (params.isNullOrEmpty()) {
            return ""
        }
        return params.joinToString(", ") { param ->
            val paramType = if (!param.type.isNullOrEmpty()) param.type else "any"
            "${param.name}:$paramType"
        }
    }

    private fun formatCompleteSignature(method: MethodDefinition, sig: Signature): String {
        val paramsString = formatParametersForSignature(sig.params)
        val returnTypeString = if (method.returnType.isNotEmpty()) ":${method.returnType}" else ""
        return "${method.name}($paramsString)$returnTypeString"
    }

    @Throws(IOException::class)
    private fun appendSignatureParameterDescriptions(
        writer: BufferedWriter,
        params: List<ParameterDefinition>?,
        indentPrefix: String
    ) {
        if (!params.isNullOrEmpty()) {
            appendLine(writer, "${indentPrefix}Parameters:")
            for (param in params) {
                val paramType = if (!param.type.isNullOrEmpty()) param.type else "any"
                val requiredStatus = if (param.required) "required" else "optional"
                val paramDesc = if (!param.description.isNullOrEmpty()) " - ${param.description}" else ""
                appendLine(writer, "$indentPrefix  - ${param.name}: $paramType ($requiredStatus)$paramDesc")
            }
        } else {
            appendLine(writer, "${indentPrefix}Parameters: (None)")
        }
    }

    @Throws(IOException::class)
    private fun appendTypeMethods(writer: BufferedWriter, methods: List<MethodDefinition>?) {
        writer.newLine()
        appendLine(writer, "Methods:")
        if (!methods.isNullOrEmpty()) {
            for (method in methods) {
                appendLine(writer, "  ---")
                appendLine(writer, "  Method: ${method.name}")
                appendIfNotNullOrEmpty(writer, "    Description: ", method.description)
                appendLine(writer, "    Signatures:")

                if (method.signature.isNotEmpty()) {
                    for (sig in method.signature) {
                        appendLine(writer, "      ---")
                        val signatureLine = formatCompleteSignature(method, sig)
                        appendLine(writer, "      $signatureLine")
                        appendIfNotNullOrEmpty(writer, "        Description: ", sig.description)
                        appendSignatureParameterDescriptions(writer, sig.params, "        ")
                    }
                } else {
                    val returnTypeString = if (method.returnType.isNotEmpty()) ":${method.returnType}" else ""
                    appendLine(writer, "      ${method.name}()$returnTypeString")
                    appendLine(writer, "        (No specific parameter details provided for this general signature)")
                }
            }
        } else {
            appendLine(writer, "  (No methods)")
        }
    }

    @Throws(IOException::class)
    private fun appendTypeConstructors(writer: BufferedWriter, typeDef: PlatformTypeDefinition) {
        val constructors = typeDef.constructors

        writer.newLine()
        appendLine(writer, "Constructors:")
        if (!constructors.isNullOrEmpty()) {
            for (constructor in constructors) {
                appendLine(writer, "  ---")
                val signatureLine = formatCompleteSignatureForConstructor(constructor, typeDef)
                appendLine(writer, "  $signatureLine")
                appendIfNotNullOrEmpty(writer, "    Description: ", constructor.description)
                appendSignatureParameterDescriptions(writer, constructor.params, "    ")
            }
        } else {
            appendLine(writer, "  (No constructors)")
        }
    }

    private fun formatCompleteSignatureForConstructor(constructor: ISignature, type: PlatformTypeDefinition): String {
        val paramsString = formatParametersForSignature(constructor.params)
        return "Новый ${type.name}($paramsString)"
    }

    @Throws(IOException::class)
    override fun writeTypes(contexts: List<Context>, output: Path) {
        writeEntries(
            output,
            "Exporting types to: {}",
            { logic.extractTypes(contexts) }
        ) { writer, typeDef ->
            writeSnippetStart(writer, "Type: ${typeDef.name}", typeDef.description)
            appendLine(writer, "Name: ${typeDef.name}")
            appendTypeConstructors(writer, typeDef)
            appendTypeProperties(writer, typeDef.properties)
            appendTypeMethods(writer, typeDef.methods)
        }
    }

    override fun getExtension(): String = ".txt"

    fun interface EntryWriter<T> {
        @Throws(IOException::class)
        fun write(writer: BufferedWriter, entry: T)
    }
} 