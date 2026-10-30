package com.gasperpintar.smokingtracker.utils.manager

import com.gasperpintar.smokingtracker.utils.ElapsedTime
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook

/** Optional fork-only XLSX sheet. Upstream's six-sheet importer ignores it. */
object MySmokingTrackerSettings {
    const val SHEET_NAME = "MySmokingTrackerSettings"
    private const val KEY_HEADER = "Key"
    private const val VALUE_HEADER = "Value"
    private const val THRESHOLD_KEY = "ElapsedTimeWarningThresholdMinutes"

    fun write(workbook: XSSFWorkbook, thresholdMinutes: Int) {
        workbook.createSheet(SHEET_NAME).apply {
            createRow(0).apply {
                createCell(0).setCellValue(KEY_HEADER)
                createCell(1).setCellValue(VALUE_HEADER)
            }
            createRow(1).apply {
                createCell(0).setCellValue(THRESHOLD_KEY)
                createCell(1).setCellValue(thresholdMinutes.toDouble())
            }
        }
    }

    fun readThresholdMinutes(workbook: XSSFWorkbook): Int {
        val sheet = workbook.getSheet(SHEET_NAME) ?: return ElapsedTime.DEFAULT_THRESHOLD_MINUTES
        val header = sheet.getRow(0) ?: return ElapsedTime.DEFAULT_THRESHOLD_MINUTES
        val hasValidHeader = runCatching {
            header.getCell(0).stringCellValue == KEY_HEADER && header.getCell(1).stringCellValue == VALUE_HEADER
        }.getOrDefault(false)
        if (!hasValidHeader) return ElapsedTime.DEFAULT_THRESHOLD_MINUTES

        sheet.drop(1).forEach { row ->
            val key = runCatching { row.getCell(0)?.stringCellValue }.getOrNull()
            if (key != THRESHOLD_KEY) return@forEach
            val cell = row.getCell(1) ?: return ElapsedTime.DEFAULT_THRESHOLD_MINUTES
            val rawValue = when (cell.cellType) {
                CellType.NUMERIC -> cell.numericCellValue.takeIf { it.isFinite() && it % 1.0 == 0.0 }
                    ?.toLong()?.toString() ?: return ElapsedTime.DEFAULT_THRESHOLD_MINUTES
                CellType.STRING -> cell.stringCellValue
                else -> return ElapsedTime.DEFAULT_THRESHOLD_MINUTES
            }
            return ElapsedTime.parseThresholdMinutes(rawValue) ?: ElapsedTime.DEFAULT_THRESHOLD_MINUTES
        }
        return ElapsedTime.DEFAULT_THRESHOLD_MINUTES
    }
}
