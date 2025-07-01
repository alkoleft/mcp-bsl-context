package ru.alkoleft.context.platform.exporter

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.github._1c_syntax.bsl.context.api.Context
import com.github._1c_syntax.bsl.context.platform.PlatformGlobalContext
import java.io.IOException
import java.nio.file.Path

class XmlExporter(
    private val logic: ExporterLogic = BaseExporterLogic()
) : Exporter {

    private val xmlMapper: XmlMapper = XmlMapper.builder()
        .defaultUseWrapper(false) // Используем false, чтобы избежать лишних оберток по умолчанию
        .build()
        .apply {
            // Для более красивого вывода XML
            enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
            enable(SerializationFeature.INDENT_OUTPUT)
        }

    @Throws(IOException::class)
    override fun writeProperties(context: PlatformGlobalContext, output: Path) {
        val file = output.toFile()

        logic.extractProperties(context).use { properties ->
            // Оборачиваем Stream в List для сериализации корневого элемента
            val propertyList = properties.toList()
            xmlMapper.writer().withRootName("properties").writeValue(file, propertyList)
        }
    }

    @Throws(IOException::class)
    override fun writeMethods(context: PlatformGlobalContext, output: Path) {
        val file = output.toFile()

        logic.extractMethods(context).use { methods ->
            // Оборачиваем Stream в List для сериализации корневого элемента
            val methodList = methods.toList()
            xmlMapper.writer().withRootName("methods").writeValue(file, methodList)
        }
    }

    @Throws(IOException::class)
    override fun writeTypes(contexts: List<Context>, output: Path) {
        val file = output.toFile()

        logic.extractTypes(contexts).use { types ->
            // Оборачиваем Stream в List для сериализации корневого элемента
            val typeList = types.toList()
            xmlMapper.writer().withRootName("types").writeValue(file, typeList)
        }
    }

    override fun getExtension(): String = ".xml"
} 