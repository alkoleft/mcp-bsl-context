package ru.alkoleft.context.platform.exporter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.github._1c_syntax.bsl.context.api.Context
import com.github._1c_syntax.bsl.context.platform.PlatformGlobalContext
import ru.alkoleft.context.platform.dto.PlatformTypeDefinition
import java.io.IOException
import java.nio.file.Path

class JsonExporter(
    private val logic: ExporterLogic = BaseExporterLogic()
) : Exporter {

    @Throws(IOException::class)
    override fun writeProperties(context: PlatformGlobalContext, output: Path) {
        val jfactory = JsonFactory()
        val file = output.toFile()

        jfactory.createGenerator(file, JsonEncoding.UTF8).use { generator ->
            generator.writeStartArray()
            logic.extractProperties(context).use { properties ->
                val propertyList = properties.toList()
                for (property in propertyList) {
                    generator.writeStartObject()
                    generator.writeStringField("name", property.name)
                    property.nameEn?.let { generator.writeStringField("name_en", it) }
                    property.description?.let { generator.writeStringField("description", it) }
                    generator.writeBooleanField("readonly", property.readonly)
                    property.type?.let { generator.writeStringField("type", it) }
                    generator.writeEndObject()
                }
            }
            generator.writeEndArray()
        }
    }

    @Throws(IOException::class)
    override fun writeMethods(context: PlatformGlobalContext, output: Path) {
        val jfactory = JsonFactory()
        val file = output.toFile()

        jfactory.createGenerator(file, JsonEncoding.UTF8).use { generator ->
            generator.writeStartArray()
            logic.extractMethods(context).use { methods ->
                val methodList = methods.toList()
                for (method in methodList) {
                    generator.writeStartObject()
                    generator.writeStringField("name", method.name)
                    method.description?.let { generator.writeStringField("description", it) }
                    generator.writeArrayFieldStart("signature")
                    if (method.signature.isNotEmpty()) {
                        for (sig in method.signature) {
                            generator.writeStartObject()
                            sig.description?.let { generator.writeStringField("description", it) }
                            generator.writeArrayFieldStart("params")
                            if (sig.params.isNotEmpty()) {
                                for (param in sig.params) {
                                    generator.writeStartObject()
                                    generator.writeStringField("name", param.name)
                                    param.description?.let { generator.writeStringField("description", it) }
                                    param.type?.let { generator.writeStringField("type", it) }
                                    generator.writeBooleanField("required", param.required)
                                    generator.writeEndObject()
                                }
                            }
                            generator.writeEndArray()
                            generator.writeEndObject()
                        }
                    }
                    generator.writeEndArray()

                    if (method.returnType.isNotEmpty()) {
                        generator.writeStringField("return", method.returnType)
                    }
                    generator.writeEndObject()
                }
            }
            generator.writeEndArray()
        }
    }

    @Throws(IOException::class)
    override fun writeTypes(contexts: List<Context>, output: Path) {
        val file = output.toFile()

        val mapper = ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        val objectWriter = mapper.writerFor(PlatformTypeDefinition::class.java)

        try {
            mapper.factory.createGenerator(file, JsonEncoding.UTF8).use { generator ->
                generator.writeStartArray()
                logic.extractTypes(contexts).use { types ->
                    val typeList = types.toList()
                    for (typeDef in typeList) {
                        objectWriter.writeValue(generator, typeDef)
                    }
                }
                generator.writeEndArray()
            }
        } catch (e: IOException) {
            if (e.cause is IOException) {
                throw e.cause as IOException
            }
            throw RuntimeException(e)
        }
    }

    override fun getExtension(): String = ".json"
} 