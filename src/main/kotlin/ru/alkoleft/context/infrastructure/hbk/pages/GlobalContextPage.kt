/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.pages

import ru.alkoleft.context.business.entities.MethodDefinition
import ru.alkoleft.context.business.entities.PropertyDefinition

class GlobalContextPage(val properties: List<PropertyDefinition>, val methods: List<MethodDefinition>)