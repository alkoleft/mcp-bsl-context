/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import ru.alkoleft.context.infrastructure.hbk.parsers.specialized.MethodPageParser
import java.io.File
import kotlin.test.assertFalse

class MethodPageParserTest {
    @Test
    fun `test parse BeginGetFileFromServer5707`() {
        val parser = MethodPageParser()
        val file = File("src/test/resources/global-methods/BeginGetFileFromServer5707.html")
        val result = parser.parse(file.inputStream())

        assertEquals("НачатьПолучениеФайлаССервера", result.nameRu)
        assertEquals("BeginGetFileFromServer", result.nameEn)
        assertEquals(2, result.signatures.size)

        // Проверяем, что у нас есть две сигнатуры с правильными именами
        assertTrue(result.signatures.any { it.name == "С диалогом" })
        assertTrue(result.signatures.any { it.name == "Без диалога" })

        // Находим сигнатуры по имени
        val signature1 = result.signatures.first { it.name == "С диалогом" }
        val signature2 = result.signatures.first { it.name == "Без диалога" }

        // Проверяем синтаксис
        assertEquals(
            "НачатьПолучениеФайлаССервера(<Адрес>, <ИмяФайла>, <ПараметрыДиалогаПолученияФайлов>)",
            signature1.syntax,
        )
        assertEquals(
            "НачатьПолучениеФайлаССервера(<ОписаниеОповещенияОЗавершении>, <Адрес>, <ПутьКФайлу>)",
            signature2.syntax,
        )

        // Проверяем наличие описания
        assertNotNull(signature1.description)
        assertNotNull(signature2.description)

        // Проверяем параметры первой сигнатуры
        assertEquals(3, signature1.parameters.size)
        assertEquals("Адрес", signature1.parameters[0].name)
        assertEquals("Строка", signature1.parameters[0].type)
        assertFalse { signature1.parameters[0].isOptional }
        assertEquals(
            """
            Расположение данных во временном хранилище или в информационной базе.
            Параметр не должен быть пустой строкой. В противном случае будет сгенерировано сообщение об ошибке.
            """.trimIndent(),
            signature1.parameters[0].description,
        )

        assertEquals("ИмяФайла", signature1.parameters[1].name)
        assertEquals("Строка", signature1.parameters[1].type)
        assertTrue { signature1.parameters[1].isOptional }
        assertEquals(
            """
            Имя файла, которое будет предложено пользователю для сохранения файла. Пользователь может изменить имя файла.
            Значение по умолчанию: Пустая строка.
            """.trimIndent(),
            signature1.parameters[1].description,
        )

        assertEquals("ПараметрыДиалогаПолученияФайлов", signature1.parameters[2].name)
        assertEquals("ПараметрыДиалогаПолученияФайлов", signature1.parameters[2].type)
        assertTrue { signature1.parameters[2].isOptional }
        assertEquals(
            "Структура, содержащая параметры диалога, который будет показан пользователю.",
            signature1.parameters[2].description,
        )

        // Проверяем параметры второй сигнатуры
        assertEquals(3, signature2.parameters.size)
        assertEquals("ОписаниеОповещенияОЗавершении", signature2.parameters[0].name)
        assertEquals("ОписаниеОповещения", signature2.parameters[0].type)
        assertTrue { signature2.parameters[0].isOptional }
        assertEquals(
            """
            Содержит описание процедуры, которая будет вызвана после завершения со следующими параметрами:
            * <ПолученныйФайл> – объект типа `ОписаниеПереданногоФайла`.
            * <ДополнительныеПараметры> – значение, которое было указано при создании объекта `ОписаниеОповещения`.
            """.trimIndent(),
            signature2.parameters[0].description,
        )

        assertEquals("Адрес", signature2.parameters[1].name)
        assertEquals("Строка", signature2.parameters[1].type)
        assertFalse { signature2.parameters[1].isOptional }
        assertEquals(
            """
            Расположение данных во временном хранилище или в информационной базе.
            Параметр не должен быть пустой строкой. В противном случае будет сгенерировано сообщение об ошибке.
            """.trimIndent(),
            signature2.parameters[1].description,
        )

        assertEquals("ПутьКФайлу", signature2.parameters[2].name)
        assertEquals("Строка", signature2.parameters[2].type)
        assertFalse { signature2.parameters[2].isOptional }
        assertEquals("Путь к файлу, в который будет сохранен файл.", signature2.parameters[2].description)

        assertNull(result.example)
    }

    @Test
    fun `test parse GetCommonTemplate376`() {
        val parser = MethodPageParser()
        val file = File("src/test/resources/global-methods/GetCommonTemplate376.html")
        val result = parser.parse(file.inputStream())

        assertEquals("ПолучитьОбщийМакет", result.nameRu)
        assertEquals("GetCommonTemplate", result.nameEn)

        // Проверяем, что описание метода было добавлено
        assertNotNull(result.example)
        assertEquals(
            """
            // Получение общего макета по имени

            МакетСтруктурыКонфигураци = ПолучитьОбщийМакет("СтруктураКонфигурации");
            // Получение общего макета по объекту описания метаданного

            МакетСтруктурыКонфигураци = ПолучитьОбщийМакет(Метаданные.ОбщиеМакеты.СтруктураКонфигурации);
            """.trimIndent(),
            result.example?.trim(),
        )

        // Проверяем, что есть хотя бы одна сигнатура
        assertTrue(result.signatures.isNotEmpty())

        val signature = result.signatures[0]

        // Проверяем синтаксис
        assertEquals("ПолучитьОбщийМакет(<ОбщийМакет>)", signature.syntax)

        // Проверяем параметры
        assertEquals(1, signature.parameters.size)
        assertEquals("ОбщийМакет", signature.parameters[0].name)
        assertEquals("Строка,ОбъектМетаданных: Макет", signature.parameters[0].type)
        assertFalse { signature.parameters[0].isOptional }
        assertEquals(
            "Имя общего макета, как оно задано в конфигураторе, или объект описания метаданного общего макета.",
            signature.parameters[0].description,
        )

        // Проверка возвращаемого значения
        assertNotNull(result.returnValue)
        assertEquals(
            "ТабличныйДокумент,ТекстовыйДокумент; другой объект, который может быть макетом.",
            result.returnValue!!.type,
        )
        assertNotNull(result.returnValue!!.description)
    }

    @Test
    fun `test parse BeginTransaction9`() {
        val parser = MethodPageParser()
        val file = File("src/test/resources/global-methods/BeginTransaction9.html")
        val result = parser.parse(file.inputStream())

        assertEquals("НачатьТранзакцию", result.nameRu)
        assertEquals("BeginTransaction", result.nameEn)
        assertEquals(1, result.signatures.size)

        val signature = result.signatures[0]
        assertEquals("НачатьТранзакцию(<РежимБлокировок>)", signature.syntax)
        assertTrue(signature.description.isEmpty())

        // Проверяем параметры
        assertEquals(1, signature.parameters.size)
        assertEquals("РежимБлокировок", signature.parameters[0].name)
        assertEquals("РежимУправленияБлокировкойДанных", signature.parameters[0].type)
        assertTrue { signature.parameters[0].isOptional }
        assertEquals(
            """
            Установка параметра имеет смысл, если для свойства конфигурации "Режим управления блокировкой данных" выбрано значение "Автоматический и Управляемый".
            Если значение параметра `Автоматический`, то данная транзакция будет выполняться в режиме автоматических блокировок.
            Если значение параметра `Управляемый`, то в данной транзакции будут выполняться управляемые блокировки.
            Если к моменту вызова данного метода была начата транзакция, выполняющаяся в автоматическом режиме управления блокировками, то установка значения параметра `Управляемый` не приведет к изменению режима управления блокировками.
            Если к моменту вызова данного метода была начата транзакция, выполняющаяся в управляемом режиме блокировок, то установка значения параметра `Автоматический` приведет к возникновению исключительной ситуации, которая может быть обработана конструкцией Попытка... Исключение... КонецПопытки.
            Если для свойства конфигурации "Режим управления блокировкой данных" выбрано значение "Управляемый", то значение параметра по умолчанию `Управляемый`.
            Значение по умолчанию: `Автоматический`.
            """.trimIndent(),
            signature.parameters[0].description,
        )

        assertNull(result.returnValue)
        assertEquals(
            """
            // Увеличение закупочной цены на 5%

            ВыборкаТоваров = Справочники.Номенклатура.Выбрать();
            НачатьТранзакцию();
            Пока ВыборкаТоваров.Следующий() Цикл
                ТоварОбъект = ВыборкаТоваров.ПолучитьОбъект();
                ЗакупочнаяЦена = ТоварОбъект.ЗакупочнаяЦена;
                Если ЗакупочнаяЦена <> 0 Тогда
                    ТоварОбъект.ЗакупочнаяЦена = ЗакупочнаяЦена * 1.05;
                    ТоварОбъект.Записать();
                КонецЕсли
            КонецЦикла;
            ЗафиксироватьТранзакцию();
            """.trimIndent(),
            result.example?.trim(),
        )
    }

    @Test
    fun `test parse AttachAddIn697`() {
        val parser = MethodPageParser()
        val file = File("src/test/resources/global-methods/AttachAddIn697.html")
        val result = parser.parse(file.inputStream())

        assertEquals("ПодключитьВнешнююКомпоненту", result.nameRu)
        assertEquals("AttachAddIn", result.nameEn)
        assertEquals(2, result.signatures.size)

        val signature0 = result.signatures[0]
        val signature1 = result.signatures[1]
        assertEquals("По идентификатору", signature0.name)
        assertEquals("ПодключитьВнешнююКомпоненту(<ИдентификаторОбъекта>)", signature0.syntax)
        assertEquals(
            """
            Компонент должен быть выполнен по технологии COM и зарегистрирован в реестре MS Windows.
            Эти компоненты совместимы с компонентами 1С:Предприятия 7.7.
            Внимание! Вариант метода не работает на сервере и во внешнем соединении.
            """.trimIndent(),
            signature0.description,
        )
        assertEquals("ИдентификаторОбъекта", signature0.parameters[0].name)
        assertEquals("Строка", signature0.parameters[0].type)
        assertEquals(
            """
            Идентификатор объекта внешнего компонента в виде ProgID (Programmatic Identifier) реестра MS Windows (например: "AddIn.Scanner").
            Должно соответствовать информации, находящейся в регистрационной базе данных системы (Registry).
            """.trimIndent(),
            signature0.parameters[0].description,
        )
        assertFalse { signature0.parameters[0].isOptional }

        assertEquals("По имени и местоположению", signature1.name)
        assertEquals("ПодключитьВнешнююКомпоненту(<Местоположение>, <Имя>, <Тип>, <ТипПодключения>)", signature1.syntax)
        assertEquals(
            """
            Подключает компоненты, выполненные по технологии Native API и COM. 
            Компонент может храниться в информационной базе или макете конфигурации в виде двоичных данных или в ZIP-архиве.
            Для режимов запуска "Тонкий клиент" и "Веб-клиент", компонент должен быть предварительно установлен методом `УстановитьВнешнююКомпоненту`.
            """.trimIndent(),
            signature1.description,
        )
        assertEquals(
            """
            Местоположение внешнего компонента.
            В качестве местоположения может использоваться:
            * путь к файлу внешнего компонента в файловой системе (недоступно на веб-клиенте), не ZIP-архив;
            * полное имя макета, хранящего двоичные данные или ZIP-архив;
            * URL к внешнему компоненту, в виде двоичных данных или ZIP-архива, в формате, аналогичном `ПолучитьНавигационнуюСсылку`.
            """.trimIndent(),
            signature1.parameters[0].description,
        )

        assertEquals("ТипПодключения", signature1.parameters[3].name)
        assertEquals("ТипПодключенияВнешнейКомпоненты", signature1.parameters[3].type)
        assertEquals(
            """
            Тип подключения внешней компоненты.

            В режиме совместимости конфигурации `Версия8_3_20` и ниже, используется значение `НеИзолированно`. 
            В остальных случаях, на сервере используется `Изолированно`, а на клиенте - `НеИзолированно`.
            """.trimIndent(),
            signature1.parameters[3].description,
        )
        assertTrue { signature1.parameters[3].isOptional }

        assertNotNull(result.returnValue)
        assertEquals("Булево", result.returnValue!!.type)
        assertEquals(
            """
            Если ПодключитьВнешнююКомпоненту("AddinObject.Scanner") Тогда
                Сообщить("Компонента для сканера штрихкодов загружена");
            Иначе
                Сообщить("Компонента для сканера штрихкодов не загружена");
            КонецЕсли;
            """.trimIndent(),
            result.example,
        )
    }

    @Test
    fun `test parse NumberInWords714`() {
        val parser = MethodPageParser()
        val file = File("src/test/resources/global-methods/NumberInWords714.html")
        val result = parser.parse(file.inputStream())

        assertEquals("ЧислоПрописью", result.nameRu)
        assertEquals("NumberInWords", result.nameEn)
        assertEquals(1, result.signatures.size)

        val signature = result.signatures[0]
        assertEquals("ЧислоПрописью(<Число>, <ФорматнаяСтрока>, <ПараметрыПредметаИсчисления>)", signature.syntax)
        assertTrue(signature.description.isEmpty())

        // Проверяем параметры
        assertEquals(3, signature.parameters.size)
        assertEquals("Число", signature.parameters[0].name)
        assertEquals("Число", signature.parameters[0].type)
        assertFalse { signature.parameters[0].isOptional }
        assertEquals("Число, которое необходимо преобразовать в строку прописью.", signature.parameters[0].description)

        assertEquals("ФорматнаяСтрока", signature.parameters[1].name)
        assertEquals("Строка", signature.parameters[1].type)
        assertTrue { signature.parameters[1].isOptional }
        assertEquals(
            """
            Форматная строка представляет собой строковое значение, включающее параметры форматирования. Параметры форматирования перечисляются через символ ";" (точка с запятой). Если параметр не указывается, используется значение параметра по умолчанию.
            Каждый параметр задается именем параметра, символом "=" (равно) и значением параметра. Значение параметра может указываться в одинарных или двойных кавычках. Это необходимо, если значение параметра содержит символы, используемые в синтаксисе форматной строки.
            * Л (L) - Код локализации. По умолчанию используется код локализации, установленный в операционной системе. Примеры кодов локализации: ru_RU - Русский (Россия); en_US - Английский (США). 
            * НП (SN) - Включать/не включать название предмета исчисления (Булево), по умолчанию - `Истина`.
            * НД (FN) - Включать/не включать название десятичных частей предмета исчисления (Булево), по умолчанию - `Истина`.
            * ДП (FS) - Дробную часть выводить прописью/числом (Булево), по умолчанию - Ложь;
            * ИИ (AU) - Определяет формирование классической (с использованием союза and перед прописью десятков и/или единиц внутри прописи триады) или упрощенной (без использования союза and) прописи числа. Имеет смысл и анализируется только при англоязычной локализации (en, en_XX). Возможные значения параметра:
            * НеИспользовать (DontUse) - формировать упрощенную пропись числа (используется по умолчанию);
            * Использовать (Use) - формировать классическую пропись числа.
            """.trimIndent(),
            signature.parameters[1].description,
        )

        assertEquals("ПараметрыПредметаИсчисления", signature.parameters[2].name)
        assertEquals("Строка", signature.parameters[2].type)
        assertTrue { signature.parameters[2].isOptional }
        assertEquals(
            """
            Представляет собой строковое значение, определяющее параметры предмета исчисления. Параметры предмета исчисления перечисляются через запятую. Формат строки зависит от кода локализации.
            Для русского и белорусского языков (ru_RU, be_BY):
            "рубль, рубля, рублей, м, копейка, копейки, копеек, ж, 2", где:
            "рубль, рубля, рублей, м" – предмет исчисления: 
            * рубль – единственное число именительный падеж; 
            * рубля – единственное число родительный падеж; 
            * рублей – множественное число родительный падеж;
            * м – мужской род (ж – женский род, с - средний род);
            * "копейка, копейки, копеек, ж" – дробная часть, аналогично предмету исчисления (может отсутствовать);
            * "2" – количество разрядов дробной части (может отсутствовать, по умолчанию равно 2).Для украинского языка (uk_UA): 
            "гривна, гривны, гривен, м, копейка, копейки, копеек, ж, 2", где:
            "гривна, гривны, гривен, м" – предмет исчисления: 
            * "гривна – единственное число именительный падеж;
            * гривны – единственное число родительный падеж;
            * гривен – множественное число родительный падеж; 
            * м – мужской род (ж – женский род, с - средний род);
            * "копейка, копейки, копеек, ж" – дробная часть, аналогично предмету исчисления (может отсутствовать);
            * "2" – количество разрядов дробной части (может отсутствовать, по умолчанию равно 2).Для польского языка (pl_PL): 
            "złoty, złote, złotych, m, grosz, grosze, groszy, m, 2" где:
            "złoty, złote, złotych, m " - предмет исчисления (m - мужской род, ż - женский род, ń - средний род, mo – личностный мужской род).
            * złoty - единственное число именительный падеж;
            * złote - единственное число винительный падеж;
            * złotych - множественное число винительный падеж;
            * m - мужской род (ż - женский род, ń - средний род, mo – личностный мужской род);
            * "grosz, grosze, groszy, m " - дробная часть (может отсутствовать) (аналогично целой части);
            * 2 - количество разрядов дробной части (может отсутствовать, по умолчанию равно 2).Пример:
            Пропись = ЧислоПрописью(1832, “L=pl_PL;SN=true;FN=true;FS=true”, “złoty, złote, złotych, m, grosz, grosze, groszy, m, 2”);
            Для английского, французского, финского и казахского языков (en_US, fr_CA,fi_FI, kk_KZ):
            "dollar, dollars, cent, cents, 2", где:
            * "dollar, dollars" – предмет исчисления в единственном и множественном числе;
            * "cent, cents" – дробная часть в единственном и множественном числе (может отсутствовать);
            * "2" – количество разрядов дробной части (может отсутствовать, по умолчанию равно 2).Для немецкого языка (de_DE):
            "EURO, EURO, М, Cent, Cent, M, 2", где:
            "EURO, EURO, М" – предмет исчисления: 
            * EURO, EURO – предмет исчисления в единственном и множественном числе; 
            * М – мужской род (F – женский род, N - средний род);"Cent, Cent, M" – дробная часть, аналогично предмету исчисления (может отсутствовать);
            "2" – количество разрядов дробной части (может отсутствовать, по умолчанию равно 2).
            Для латышского языка (lv_LV):
            "lats, lati, latu, V, santīms, santīmi, santīmu, V, 2, J, J", где:
            "lats, lati, latu, v" – предмет исчисления: 
            * lats – для чисел заканчивающихся на 1, кроме 11;
            * lati – для чисел заканчивающихся на 2-9 и 11;
            * latu – множественное число (родительный падеж) используется после числительных 0, 10, 20,..., 90, 100, 200, ..., 1000, ..., 100000; 
            * v – мужской род (s – женский род);"santīms, santīmi, santīmu, V" – дробная часть, аналогично предмету исчисления (может отсутствовать);
            "2" – количество разрядов дробной части (может отсутствовать, по умолчанию равно 2);
            "J" - число 100 выводится как "Одна сотня" для предмета исчисления (N - как "Сто");
            может отсутствовать, по умолчанию равно "J";
            "J" - число 100 выводится как "Одна сотня" для дробной части (N - как "Сто");
            может отсутствовать, по умолчанию равно "J".
            Для литовского языка (lt_LT):
            "litas, litai, litų, М, centas, centai, centų, М, 2", где:
            "litas, litai, litų, М" – предмет исчисления:
            * litas - единственное число целой части;
            * litai - множественное число целой части от 2 до 9;
            * litų - множественное число целой части прочие;
            * m - род целой части (f - женский род),"centas, centai, centų, М" – дробная часть, аналогично предмету исчисления (может отсутствовать);
            "2" - количество разрядов дробной части (может отсутствовать, по умолчанию равно 2).
            Для эстонского языка (et_EE):
            "kroon, krooni, sent, senti, 2", где:
            "kroon, krooni" – – предмет исчисления в единственном и множественном числе;
            * "sent, senti" – дробная часть в единственном и множественном числе (может отсутствовать);
            * 2 – количество разрядов дробной части (может отсутствовать, по умолчанию равно 2).Для болгарского языка (bg_BG):
            "лев, лева, м, стотинка, стотинки, ж, 2", где:
            "лев, лева, м" – предмет исчисления:
            * лев - единственное число целой части;
            * лева - множественное число целой части;
            * м - род целой части,"стотинка, стотинки, ж" - дробная часть:
            * стотинка - единственное число дробной части;
            * стотинки - множественное число дробной части;
            * ж - род дробной части,"2" - количество разрядов дробной части.
            Для румынского языка (ro_RO):
            "leu, lei, M, ban, bani, W, 2";
            "leu, lei, M" – предмет исчисления:
            * leu - единственное число целой части;
            * lei - множественное число целой части;
            * M - род целой части;"ban, bani, W" - дробная часть:
            * ban - единственное число дробной части;
            * bani - множественное число дробной части;
            * W - род дробной части;"2" - количество разрядов дробной части.
            Для грузинского языка (ka_GE):
            "ლარი, თეთრი, 2";
            * ლარი - целая часть;
            * თეთრი - дробная часть;
            * "2" - количество разрядов дробной части.Для азербайджанского(az) и туркменского языков(tk):
            "TL,Kr,2 " , где
            * "TL" - предмет исчисления;
            * "Kr" - дробная часть (может отсутствовать);
            * 2 - количество разрядов дробной части (может отсутствовать, по умолчанию - 2)Для вьетнамского языка (vi_VN):
            "dong, xu, 2";
            * dong, - целая часть;
            * xu, - дробная часть;
            * 2 - количество разрядов дробной части.Для турецкого языка (tr_TR):
            "TL,Kr,2,Separate", где:
            * TL - целая часть;
            * Kr - дробная часть (может отсутствовать);
            * 2 - количество разрядов дробной части (может отсутсвовать, значение по умолчанию - 2);
            * "Separate" - признак написания прописи раздельно, "Solid" - слитно (может отсутствовать, по умолчанию слитно).Для венгерского языка (hu):
            "Forint, fillér, 2", где
            * Forint - целая часть;
            * fillér - дробная часть;
            * "2" - количество разрядов дробной части.Для китайского языка (zh, zh_CN):
            "元,角,分,2" , где
            * "元" – юань/предмет исчисления;
            * "角" – цзяо/дробная (одна десятая) часть;
            * "分" – фынь/дробная (одна сотая) часть;
            * 2 – количество разрядов дробной части.
            Значение по умолчанию: Пустая строка.
            """.trimIndent(),
            signature.parameters[2].description,
        )

        assertNotNull(result.returnValue)
        assertEquals("Строка", result.returnValue!!.type)
        assertEquals(
            """
            // Пример форматной строки для вывода числа прописью на русском

            // языке, с выводом целой и дробной части прописью и выводом

            // предмета исчисления.

            ФормСтрока = "Л = ru_RU; ДП = Истина";
            ПарПредмета="доллар,доллара,долларов,м,цент,цента,центов,м,2";
            ПрописьЧисла = ЧислоПрописью(2341.56, ФормСтрока, ПарПредмета);

            // Результат вычисления:

            // "Две тысячи триста сорок один доллар пятьдесят шесть центов"
            """.trimIndent(),
            result.example,
        )
    }

    @Test
    fun `test parse StrTemplate4527`() {
        val parser = MethodPageParser()
        val file = File("src/test/resources/global-methods/StrTemplate4527.html")
        val result = parser.parse(file.inputStream())

        assertEquals("СтрШаблон", result.nameRu)
        assertEquals("StrTemplate", result.nameEn)
        assertEquals(1, result.signatures.size)

        val signature = result.signatures[0]
        assertEquals("СтрШаблон(<Шаблон>, <Значение1-Значение10>)", signature.syntax)
        assertTrue(signature.description.isEmpty())

        // Проверяем параметры
        assertEquals(2, signature.parameters.size) // Шаблон + Значение1-Значение10

        assertEquals("Шаблон", signature.parameters[0].name)
        assertEquals("Строка", signature.parameters[0].type)
        assertFalse { signature.parameters[0].isOptional }
        assertEquals(
            """
            Строка, содержащая маркеры подстановки вида: "%1..%N". Нумерация маркеров начинается с 1. N не может быть больше 10. 
            Если требуется сразу после номера подстановки написать цифру, то номер подстановки должен быть указан в скобках. Тогда строка может иметь следующий вид: "%(1)1cv8с.exe"
            """.trimIndent(),
            signature.parameters[0].description,
        )

        assertEquals("Значение1-Значение10", signature.parameters[1].name)
        assertEquals("Произвольный", signature.parameters[1].type)
        assertTrue { signature.parameters[1].isOptional }
        assertEquals(
            """
            Параметры, содержащие произвольные значения, строковые представления которых должны быть подставлены в шаблон. Указываются через запятую.
            Количество значений должно совпадать с числом N из %N. Если количество значений не совпадает с числом N из %N, тогда генерируется исключительная ситуация "Недостаточно фактических параметров".
            """.trimIndent(),
            signature.parameters[1].description,
        )

        assertNotNull(result.returnValue)
        assertEquals("Строка", result.returnValue!!.type)
        assertEquals("СтрШаблон(\"Ошибка в данных в строке %1 (требуется тип %2)\", 2, \"Дата\")", result.example)
    }

    @Test
    fun `test parse InputDate25`() {
        val parser = MethodPageParser()
        val file = File("src/test/resources/global-methods/InputDate25.html")
        val result = parser.parse(file.inputStream())

        assertEquals("ВвестиДату", result.nameRu)
        assertEquals("InputDate", result.nameEn)
        assertEquals(1, result.signatures.size)

        val signature = result.signatures[0]
        assertEquals("ВвестиДату(<Дата>, <Подсказка>, <ЧастьДаты>)", signature.syntax)
        assertTrue(signature.description.isEmpty())

        // Проверяем параметры
        assertEquals(3, signature.parameters.size)
        assertEquals("Дата", signature.parameters[0].name)
        assertEquals("Дата", signature.parameters[0].type)
        assertFalse { signature.parameters[0].isOptional }
        assertEquals(
            "Имя доступной в модуле переменной. В эту переменную будет помещено введенное значение даты. Начальное значение переменной будет использовано в качестве начального значения в диалоге.",
            signature.parameters[0].description,
        )

        assertEquals("Подсказка", signature.parameters[1].name)
        assertEquals("Строка", signature.parameters[1].type)
        assertTrue { signature.parameters[1].isOptional }
        assertEquals(
            """
            Текст заголовка окна диалога ввода даты. Может использоваться в качестве подсказки пользователю.
            Значение по умолчанию: Пустая строка.
            """.trimIndent(),
            signature.parameters[1].description,
        )

        assertEquals("ЧастьДаты", signature.parameters[2].name)
        assertEquals("ЧастиДаты", signature.parameters[2].type)
        assertTrue { signature.parameters[2].isOptional }
        assertEquals(
            """
            Вводимая в диалоге часть (или части) даты.
            Значение по умолчанию: `ДатаВремя`.
            """.trimIndent(),
            signature.parameters[2].description,
        )

        assertNotNull(result.returnValue)
        assertEquals("Булево", result.returnValue!!.type)
        assertEquals("`Истина` - дата введена; `Ложь` - пользователь отказался от ввода даты.", result.returnValue!!.description)
        assertEquals(
            """
            ДатаНапоминания = РабочаяДата;
            Подсказка = "Введите дату и время";
            ЧастьДаты = ЧастиДаты.ДатаВремя;
            Если ВвестиДату(ДатаНапоминания, Подсказка, ЧастьДаты) Тогда
                // запомнить дату напоминания
            
            КонецЕсли;
            """.trimIndent(),
            result.example
                ?.replace("\r\n", "\n")
                ?.replace("\r", "\n")
                ?.trim(),
        )
    }
}
