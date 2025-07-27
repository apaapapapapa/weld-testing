package com.example.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LocalDateConverterTest {

    LocalDateConverter converter = new LocalDateConverter();

    @Nested
    @DisplayName("getAsObject のテスト")
    class GetAsObjectTests {

        @ParameterizedTest(name = "文字列 ''{0}'' を変換すると {1} になる")
        @CsvSource({
            "2025-01-01, 2025-01-01",
            "1999-12-31, 1999-12-31"
        })
        void validDateStrings_shouldBeParsedCorrectly(String input, String expectedDate) {
            LocalDate expected = LocalDate.parse(expectedDate);
            assertEquals(expected, converter.getAsObject(null, null, input));
        }

        @Test
        void nullValue_shouldReturnNull() {
            assertNull(converter.getAsObject(null, null, null));
        }

        @Test
        void emptyOrBlankString_shouldReturnNull() {
            assertNull(converter.getAsObject(null, null, ""));
            assertNull(converter.getAsObject(null, null, "   "));
        }

        @Test
        void invalidFormat_shouldThrowException() {
            assertThrows(Exception.class, () -> converter.getAsObject(null, null, "07/01/2025"));
        }
    }

    @Nested
    @DisplayName("getAsString のテスト")
    class GetAsStringTests {

        @ParameterizedTest(name = "{0} は文字列 ''{1}'' に変換される")
        @CsvSource({
            "2025-07-27, 2025-07-27",
            "2000-01-01, 2000-01-01"
        })
        void validLocalDate_shouldFormatCorrectly(String inputDate, String expected) {
            LocalDate input = LocalDate.parse(inputDate);
            assertEquals(expected, converter.getAsString(null, null, input));
        }

        @Test
        void nullValue_shouldReturnEmptyString() {
            assertEquals("", converter.getAsString(null, null, null));
        }
    }
}
