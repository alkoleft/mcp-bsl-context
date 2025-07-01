package ru.alkoleft.context.platform.exporter

import com.github._1c_syntax.bsl.context.api.Context
import com.github._1c_syntax.bsl.context.platform.PlatformGlobalContext
import ru.alkoleft.context.platform.dto.MethodDefinition
import ru.alkoleft.context.platform.dto.PlatformTypeDefinition
import ru.alkoleft.context.platform.dto.PropertyDefinition
import java.util.stream.Stream

interface ExporterLogic {
    fun extractProperties(context: PlatformGlobalContext): Stream<PropertyDefinition>

    fun extractMethods(context: PlatformGlobalContext): Stream<MethodDefinition>

    fun extractTypes(contexts: List<Context>): Stream<PlatformTypeDefinition>
} 