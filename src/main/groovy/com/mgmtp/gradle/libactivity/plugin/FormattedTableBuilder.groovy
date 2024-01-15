package com.mgmtp.gradle.libactivity.plugin


import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.apache.commons.lang3.StringUtils

/**
 * Builds a table as String like the following
 * <p>
 *     +---------------------+-------------------+-------------------+
 *     | column headline 1   |         ..        | column headline n |
 *     +---------------------+-------------------+-------------------+
 *     | column element 11   |         ..        | column element n1 |
 *     |        ..           |                   |        ..         |
 *     | column element 1n   |         ..        | column element nn |
 *     +---------------------+-------------------+-------------------+
 * </p>
 * <p>
 *     Headline and border are optional.
 * </p>
 */
@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PACKAGE_PRIVATE)
class FormattedTableBuilder {

    private final List<Column> columns = []

    private boolean visibleBorder = true

    private boolean withColumnHeadline = true

    private static final String CORNER_SYMBOL = '+'

    private static final String FRAME_SYMBOL = '|'

    private static final String LINE_SYMBOL = '-'

    private static final String WHITESPACE = ' '

    private static final String DOUBLE_WHITESPACE = WHITESPACE * 2

    private static final String EMPTY_STRING = ''

    @PackageScope
    FormattedTableBuilder column(final String headline, final List<String> elements) {
        columns << new Column(headline, new ArrayList<>(elements))
        return this
    }

    @PackageScope
    FormattedTableBuilder invisibleBorder() {
        visibleBorder = false
        return this
    }

    @PackageScope
    FormattedTableBuilder skipColumnHeadline() {
        withColumnHeadline = false
        return this
    }

    @PackageScope
    String build() {

        removeEmptyColumns()

        if (columns) {

            final int numRows = columns
                    .collect { final Column column -> column.elements.size() }
                    .max()

            final List<Integer> columnWidths = columns
                    .collect { final Column column -> [column.headline] + column.elements }
                    .collect { final List<String> elements -> elements.collect { final String element -> element.length() }.max() }

            final StringBuilder tableStringBuilder = new StringBuilder()

            if (visibleBorder) {
                addBorder(tableStringBuilder, columnWidths)
            }
            if (withColumnHeadline) {
                addLine(tableStringBuilder, -1, columnWidths)
                addBorder(tableStringBuilder, columnWidths)
            }

            addLines(tableStringBuilder, numRows, columnWidths)

            if (visibleBorder) {
                addBorder(tableStringBuilder, columnWidths)
            }

            return tableStringBuilder.toString()
        }

        return EMPTY_STRING
    }

    /**
     * Removes columns that either contain no elements or only empty strings.
     */
    private void removeEmptyColumns() {

        for (int i = columns.size() - 1; i > -1; --i) {

            boolean containsVisibleElement = columns[i].elements.any { final String element -> element }

            if (!containsVisibleElement) {
                columns.remove(i)
            }
        }
    }

    /**
     * Respects column widths plus whitespace at beginning and end of cell to provide some air.
     */
    private void addBorder(final StringBuilder tableStringBuilder, final List<Integer> columnWidths) {

        final String outerCornerSymbol = visibleBorder ? CORNER_SYMBOL : EMPTY_STRING
        final String innerCornerSymbol = visibleBorder ? outerCornerSymbol : DOUBLE_WHITESPACE
        final String lineSymbol = visibleBorder ? LINE_SYMBOL : WHITESPACE

        tableStringBuilder.append(outerCornerSymbol)

        columnWidths.eachWithIndex { final int columnWidth, final int i ->

            final int cellWidthPaddingAware = visibleBorder ? columnWidth + 2 : columnWidth
            final String cornerSymbol = i < columnWidths.size() - 1 ? innerCornerSymbol : outerCornerSymbol
            tableStringBuilder
                    .append(lineSymbol * cellWidthPaddingAware)
                    .append(cornerSymbol)
        }

        tableStringBuilder.append(System.lineSeparator())
    }

    /**
     * Adds the passed number of rows / adds row cells to all columns. Applies no frame symbol / just whitespace if
     * the option for "border between headline and content" is set.
     *
     * @param numRows The number of rows to add to the table. This number may exceed the number of elements in selected
     * columns in which case empty strings will be added to the respective table columns.
     */
    private void addLines(final StringBuilder tableStringBuilder, final int numRows, final List<Integer> columnWidths) {
        for (int numRow = 0; numRow < numRows; ++numRow) {
            addLine(tableStringBuilder, numRow, columnWidths)
        }
    }

    /**
     * Adds a row at the passed index / adds row cells to all columns.
     *
     * @param numRow row index. If negative the column elements will not be accessed for content. Instead, the column
     * headline will be used.
     */
    private void addLine(final StringBuilder tableStringBuilder, final int numRow, final List<Integer> columnWidths) {

        final String outerFrameSymbol = visibleBorder ? FRAME_SYMBOL : EMPTY_STRING
        final String innerFrameSymbol = visibleBorder ? outerFrameSymbol : DOUBLE_WHITESPACE
        final String cellPaddingSymbol = visibleBorder ? WHITESPACE : EMPTY_STRING

        tableStringBuilder.append(outerFrameSymbol)

        columns.eachWithIndex { final Column column, final int i ->

            final String content = numRow > -1 ? column.elements[numRow] : column.headline
            final String frameSymbol = i < columns.size() - 1 ? innerFrameSymbol : outerFrameSymbol

            tableStringBuilder
                    .append(cellPaddingSymbol)
            // in case column sizes are not equal we pad with empty cells
                    .append(StringUtils.rightPad(content ?: EMPTY_STRING, columnWidths[i]))
                    .append(cellPaddingSymbol)
                    .append(frameSymbol)
        }

        tableStringBuilder.append(System.lineSeparator())
    }

    @TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
    @VisibilityOptions(Visibility.PRIVATE)
    private static class Column {

        final String headline

        final List<String> elements
    }
}