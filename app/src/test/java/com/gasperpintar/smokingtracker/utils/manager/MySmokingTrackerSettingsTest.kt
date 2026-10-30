package com.gasperpintar.smokingtracker.utils.manager

import com.gasperpintar.smokingtracker.utils.ElapsedTime
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.Assert.assertEquals
import org.junit.Test

class MySmokingTrackerSettingsTest {
    @Test fun extensionRoundTripPreservesThreshold() {
        XSSFWorkbook().use { workbook ->
            MySmokingTrackerSettings.write(workbook, 90)
            assertEquals(90, MySmokingTrackerSettings.readThresholdMinutes(workbook))
            assertEquals(MySmokingTrackerSettings.SHEET_NAME, workbook.getSheetAt(0).sheetName)
            assertEquals("Key", workbook.getSheetAt(0).getRow(0).getCell(0).stringCellValue)
            assertEquals("Value", workbook.getSheetAt(0).getRow(0).getCell(1).stringCellValue)
        }
    }
    @Test fun missingExtensionUsesDefault() {
        XSSFWorkbook().use { assertEquals(60, MySmokingTrackerSettings.readThresholdMinutes(it)) }
    }
    @Test fun malformedAndUnknownExtensionValuesUseDefaults() {
        listOf("", "0", "-2", "invalid", "1.5", "10081").forEach { value ->
            XSSFWorkbook().use { workbook ->
                MySmokingTrackerSettings.write(workbook, 60)
                workbook.getSheet(MySmokingTrackerSettings.SHEET_NAME).getRow(1).getCell(1).setCellValue(value)
                assertEquals(value, ElapsedTime.DEFAULT_THRESHOLD_MINUTES, MySmokingTrackerSettings.readThresholdMinutes(workbook))
            }
        }
        XSSFWorkbook().use { workbook ->
            MySmokingTrackerSettings.write(workbook, 90)
            workbook.getSheet(MySmokingTrackerSettings.SHEET_NAME).getRow(1).getCell(0).setCellValue("UnknownFutureSetting")
            assertEquals(60, MySmokingTrackerSettings.readThresholdMinutes(workbook))
        }
    }
    @Test fun malformedExtensionHeaderUsesDefault() {
        XSSFWorkbook().use { workbook ->
            MySmokingTrackerSettings.write(workbook, 90)
            workbook.getSheet(MySmokingTrackerSettings.SHEET_NAME).getRow(0).getCell(0).setCellValue("Unexpected")
            assertEquals(60, MySmokingTrackerSettings.readThresholdMinutes(workbook))
        }
    }
}
