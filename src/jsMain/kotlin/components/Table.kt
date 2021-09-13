package components

import com.ccfraser.muirwik.components.*
import com.ccfraser.muirwik.components.button.mIconButton
import com.ccfraser.muirwik.components.table.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.css.*
import react.*
import react.dom.br
import styled.StyleSheet
import styled.StyledElementBuilder
import styled.css
import styled.styledDiv
import kotlin.math.min

private val scope = MainScope()

abstract class Table<T, ColumnId>(props: Props<ColumnId>) : RComponent<Table.Props<ColumnId>, Table.State<T>>() {

    interface Props<ColumnId> : RProps {
        var title: String
        var sortTemplate: (ColumnId, MTableCellSortDirection) -> String
    }

    interface State<T> : RState {
        var items: MutableList<Pair<Int, T>>
        var order: MTableCellSortDirection
    }

    data class ColumnData<T>(
        val name: T,
        val rightAligned: Boolean,
        val disablePadding: Boolean,
        val label: String
    )

    override fun State<T>.init() {
        items = mutableListOf()
        scope.launch {
            val items: List<T> = get()
            setState {
                items.forEach { this.items.add(it._id to it) }
                order = MTableCellSortDirection.asc
            }
        }
    }

    // State for sort and select example

    protected val selectedIds = mutableSetOf<Int>()

    protected var page = 0

    protected var rowsPerPage = 10

    abstract suspend fun get(): List<T>

    abstract fun T.label(): String

    abstract fun ColumnId.comparator(a: T, b: T): Int

    abstract val columnData: List<ColumnData<ColumnId>>

    abstract var orderByColumn: ColumnId

    abstract fun StyledElementBuilder<*>.buildRow(source: T, isSelected: Boolean)

    abstract val T._id: Int

    override fun RBuilder.render() {
        mTypography("${props.title}")
        br { }
        mTypography(props.sortTemplate(orderByColumn, state.order))
        sortingAndSelecting()
    }

    val size get() = state.items.size
    val Pair<Int, T>.id get() = first
    fun RBuilder.sortingAndSelecting() {
        mPaper {
            css {
                width = 100.pct
                marginTop = 3.spacingUnits
            }
            enhancedTableToolbar(selectedIds.size)
            styledDiv {
                css { overflowX = Overflow.auto }
                mTable {
                    css { minWidth = 700.px }
                    enhancedTableHead(
                        selectedIds.size, state.order, orderByColumn, size,
                        ::handleSelectAllClick, ::handleRequestSort
                    )
                    mTableBody {
                        state.items.subList(page * rowsPerPage, min((page + 1) * rowsPerPage, size)).forEach {
                            val isSelected = selectedIds.contains(it.id)
                            mTableRow(it.id, isSelected, true, onClick = { _ -> handleClick(it.id) }) {
                                attrs.asDynamic().tabIndex = -1
                                attrs.asDynamic().role = "checkbox"
                                buildRow(it.second, isSelected)
                            }
                        }
                        val emptyRows = rowsPerPage - min(rowsPerPage, state.items.size - page * rowsPerPage)
                        if (emptyRows > 0) {
                            mTableRow {
                                css { height = (49 * emptyRows).px }
                                mTableCell(colSpan = 6)
                            }
                        }
                    }
                }
            }
            mTablePagination(count = state.items.size, rowsPerPage = rowsPerPage, page = page,
                onChangePage = { _, newPage -> setState { page = newPage } },
                onChangeRowsPerPage = {
                    setState {
                        rowsPerPage = it.target.asDynamic().value
                        page = 0
                    }
                })
        }
    }

    private fun handleSelectAllClick(checked: Boolean): Unit {
        setState {
            if (checked) {
                selectedIds.addAll(state.items.map { it.first })
            } else {
                selectedIds.clear()
            }
        }
    }

    private fun handleRequestSort(id: ColumnId): Unit {
        setState {
            if (orderByColumn == id) {
                order = if (order == MTableCellSortDirection.asc)
                    MTableCellSortDirection.desc
                else
                    MTableCellSortDirection.asc
            } else {
                order = MTableCellSortDirection.asc
            }

            orderByColumn = id

            if (order == MTableCellSortDirection.asc) {
                items.sortWith { a, b -> id.comparator(a.second, b.second) }
            } else {
                items.sortWith { a, b -> id.comparator(b.second, a.second) }
            }
        }
    }

    protected fun handleClick(id: Int): Unit {
        setState {
            if (selectedIds.contains(id)) {
                selectedIds.remove(id)
            } else {
                selectedIds.add(id)
            }
        }
    }

    protected fun RBuilder.enhancedTableHead(
        numSelected: Int,
        order: MTableCellSortDirection,
        orderByColumn: ColumnId,
        rowCount: Int,
        onSelectAllClick: (checked: Boolean) -> Unit,
        onRequestSort: (id: ColumnId) -> Unit
    ) {
        mTableHead {
            mTableRow {
                mTableCell(padding = MTableCellPadding.checkbox) {
                    mCheckbox(indeterminate = numSelected > 0 && numSelected < rowCount,
                        checked = numSelected == rowCount,
                        onChange = { _, checked -> onSelectAllClick(checked) })
                }
                columnData.forEach { data ->
                    mTableCell(
                        data.name,
                        align = if (data.rightAligned) MTableCellAlign.right else MTableCellAlign.left,
                        padding = if (data.disablePadding) MTableCellPadding.none else MTableCellPadding.default,
                        sortDirection = if (orderByColumn == data.name) order else MTableCellSortDirection.False
                    ) {
                        mTooltip(
                            "Sort",
                            if (data.rightAligned) TooltipPlacement.bottomEnd else TooltipPlacement.bottomStart,
                            enterDelay = 300
                        ) {
                            mTableSortLabel(data.label, orderByColumn == data.name,
//                                    iconFunction = { mIcon("star", addAsChild = false) },
                                direction = if (order == MTableCellSortDirection.asc) MTableSortLabelDirection.asc else MTableSortLabelDirection.desc,
                                onClick = { onRequestSort(data.name) })
                        }
                    }
                }
            }
        }
    }

    fun RBuilder.enhancedTableToolbar(numSelected: Int) {
        themeContext.Consumer { theme ->
            val styles = object : StyleSheet("ComponentStyles", isStatic = true) {
                //XXX - I was getting this error:
                //  When accessing module declarations from UMD, they must be marked by both @JsModule and @JsNonModule
                //
                //val spacer by css {
                //    flex(1.0, 1.0, 100.pct)
                //}
                //val highlight by css {
                //    if (theme.palette.type == "light") {
                //        color = Color(theme.palette.secondary.main)
                //        backgroundColor = Color(lighten(theme.palette.secondary.light, 0.85))
                //    } else {
                //        color = Color(theme.palette.text.primary)
                //        backgroundColor = Color(lighten(theme.palette.secondary.dark, 0.85))
                //    }
                //}
                //val actions by css {
                //    color = Color(theme.palette.text.secondary)
                //}
            }

            mToolbar {
                //if (numSelected > 0) css(styles.highlight)
                styledDiv {
                    css { flex(0.0, 0.0, FlexBasis.auto) }
                    if (numSelected > 0) {
                        mTypography("$numSelected selected", variant = MTypographyVariant.subtitle1)
                    } else {
                        mTypography("Nutrition", variant = MTypographyVariant.h6)
                    }
                }
                //styledDiv { css(styles.spacer) }
                styledDiv {
                    //css(styles.actions)
                    if (numSelected > 0) {
                        mTooltip("Delete") {
                            mIconButton("delete")
                        }
                    } else {
                        mTooltip("Filter list") {
                            mIconButton("filter_list")
                        }
                    }
                }
            }
        }
    }
}

