# Пакет ru.alkoleft.context.infrastructure.hbk

Пакет для работы с HBK (Help Book) файлами платформы 1С:Предприятие.

Этот пакет содержит компоненты для извлечения, парсинга и обработки 
справочной информации из HBK файлов, которые содержат документацию по 
объектам, методам и свойствам платформы 1С:Предприятие.

## Основные компоненты пакета

- **HbkContainerExtractor** - извлечение содержимого из HBK контейнеров
- **HbkContentReader** - чтение содержимого HBK файлов
- **HbkParser** - основной парсер для обработки HBK структуры
- **HbkTreeParser** - парсер древовидной структуры HBK
- **PlatformContextReader** - чтение контекста платформы из HBK
- **Page** - базовый класс для представления страниц HBK
- **toc** - компоненты для работы с оглавлением (Table of Contents)
- **pages** - специализированные парсеры для различных типов страниц
- **parsers** - дополнительные парсеры для обработки контента

## Связь с оригинальным проектом

Данный проект основан на оригинальном репозитории 
[1c-syntax/bsl-context](https://github.com/1c-syntax/bsl-context), 
который также выполняет разбор и анализ HBK файлов для построения контекста 
BSL (Business Script Language) платформы 1С:Предприятие.

## Отличие

Текущий проект развивает базовый функционал для целей построения документации, 
извлекая значительно больше информации, необходимой для построения документации для LLM. 

## Ссылки

- [Оригинальный проект bsl-context](https://github.com/1c-syntax/bsl-context)

---

*Copyright (c) 2025 alkoleft. All rights reserved.*  
*This file is part of the mcp-bsl-context project.*  
*Licensed under the MIT License. See LICENSE file in the project root for full license information.*