package io.legado.app.ui.book.read.page

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import io.legado.app.constant.AppConst.timeFormat
import io.legado.app.data.entities.Bookmark
import io.legado.app.databinding.ViewBookPageBinding
import io.legado.app.help.config.ReadBookConfig
import io.legado.app.help.config.ReadTipConfig
import io.legado.app.model.ReadBook
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.ui.book.read.page.entities.TextPage
import io.legado.app.ui.book.read.page.entities.TextPos
import io.legado.app.ui.book.read.page.provider.ChapterProvider
import io.legado.app.ui.widget.BatteryView
import io.legado.app.utils.activity
import io.legado.app.utils.dpToPx
import io.legado.app.utils.statusBarHeight
import io.legado.app.utils.visible
import splitties.views.backgroundColor
import java.util.*

/**
 * 页面视图
 */
class PageView(context: Context) : FrameLayout(context) {

    private val binding = ViewBookPageBinding.inflate(LayoutInflater.from(context), this, true)
    private val readBookActivity get() = activity as? ReadBookActivity
    private var battery = 100
    private var tvTitle: BatteryView? = null
    private var tvTime: BatteryView? = null
    private var tvBattery: BatteryView? = null
    private var tvBatteryP: BatteryView? = null
    private var tvPage: BatteryView? = null
    private var tvTotalProgress: BatteryView? = null
    private var tvPageAndTotal: BatteryView? = null
    private var tvBookName: BatteryView? = null
    private var tvTimeBattery: BatteryView? = null
    private var tvTimeBatteryP: BatteryView? = null

    val headerHeight: Int
        get() {
            val h1 = if (ReadBookConfig.hideStatusBar) 0 else context.statusBarHeight
            val h2 = if (binding.llHeader.isGone) 0 else binding.llHeader.height
            return h1 + h2
        }

    init {
        if (!isInEditMode) {
            upStyle()
        }
        binding.contentTextView.upView = {
            setProgress(it)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        upBg()
    }

    fun upStyle() = binding.run {
        upTipStyle()
        ReadBookConfig.let {
            val tipColor = with(ReadTipConfig) {
                if (tipColor == 0) it.textColor else tipColor
            }
            tvHeaderLeft.setColor(tipColor)
            tvHeaderMiddle.setColor(tipColor)
            tvHeaderRight.setColor(tipColor)
            tvFooterLeft.setColor(tipColor)
            tvFooterMiddle.setColor(tipColor)
            tvFooterRight.setColor(tipColor)
            upStatusBar()
            llHeader.setPadding(
                it.headerPaddingLeft.dpToPx(),
                it.headerPaddingTop.dpToPx(),
                it.headerPaddingRight.dpToPx(),
                it.headerPaddingBottom.dpToPx()
            )
            llFooter.setPadding(
                it.footerPaddingLeft.dpToPx(),
                it.footerPaddingTop.dpToPx(),
                it.footerPaddingRight.dpToPx(),
                it.footerPaddingBottom.dpToPx()
            )
            vwTopDivider.visible(it.showHeaderLine)
            vwBottomDivider.visible(it.showFooterLine)
        }
        contentTextView.upVisibleRect()
        upTime()
        upBattery(battery)
    }

    /**
     * 显示状态栏时隐藏header
     */
    fun upStatusBar() = with(binding.vwStatusBar) {
        setPadding(paddingLeft, context.statusBarHeight, paddingRight, paddingBottom)
        isGone = ReadBookConfig.hideStatusBar || readBookActivity?.isInMultiWindow == true
    }

    /**
     * 更新阅读信息
     */
    private fun upTipStyle() = binding.run {
        tvHeaderLeft.tag = null
        tvHeaderMiddle.tag = null
        tvHeaderRight.tag = null
        tvFooterLeft.tag = null
        tvFooterMiddle.tag = null
        tvFooterRight.tag = null
        llHeader.isGone = when (ReadTipConfig.headerMode) {
            1 -> false
            2 -> true
            else -> !ReadBookConfig.hideStatusBar
        }
        llFooter.isGone = when (ReadTipConfig.footerMode) {
            1 -> true
            else -> false
        }
        ReadTipConfig.apply {
            tvHeaderLeft.isGone = tipHeaderLeft == none
            tvHeaderRight.isGone = tipHeaderRight == none
            tvHeaderMiddle.isGone = tipHeaderMiddle == none
            tvFooterLeft.isInvisible = tipFooterLeft == none
            tvFooterRight.isGone = tipFooterRight == none
            tvFooterMiddle.isGone = tipFooterMiddle == none
        }
        tvTitle = getTipView(ReadTipConfig.chapterTitle)?.apply {
            tag = ReadTipConfig.chapterTitle
            isBattery = false
            typeface = ChapterProvider.typeface
            textSize = 12f
        }
        tvTime = getTipView(ReadTipConfig.time)?.apply {
            tag = ReadTipConfig.time
            isBattery = false
            typeface = ChapterProvider.typeface
            textSize = 12f
        }
        tvBattery = getTipView(ReadTipConfig.battery)?.apply {
            tag = ReadTipConfig.battery
            isBattery = true
            textSize = 11f
        }
        tvPage = getTipView(ReadTipConfig.page)?.apply {
            tag = ReadTipConfig.page
            isBattery = false
            typeface = ChapterProvider.typeface
            textSize = 12f
        }
        tvTotalProgress = getTipView(ReadTipConfig.totalProgress)?.apply {
            tag = ReadTipConfig.totalProgress
            isBattery = false
            typeface = ChapterProvider.typeface
            textSize = 12f
        }
        tvPageAndTotal = getTipView(ReadTipConfig.pageAndTotal)?.apply {
            tag = ReadTipConfig.pageAndTotal
            isBattery = false
            typeface = ChapterProvider.typeface
            textSize = 12f
        }
        tvBookName = getTipView(ReadTipConfig.bookName)?.apply {
            tag = ReadTipConfig.bookName
            isBattery = false
            typeface = ChapterProvider.typeface
            textSize = 12f
        }
        tvTimeBattery = getTipView(ReadTipConfig.timeBattery)?.apply {
            tag = ReadTipConfig.timeBattery
            isBattery = true
            typeface = ChapterProvider.typeface
            textSize = 11f
        }
        tvBatteryP = getTipView(ReadTipConfig.batteryPercentage)?.apply {
            tag = ReadTipConfig.batteryPercentage
            isBattery = false
            typeface = ChapterProvider.typeface
            textSize = 12f
        }
        tvTimeBatteryP = getTipView(ReadTipConfig.timeBatteryPercentage)?.apply {
            tag = ReadTipConfig.timeBatteryPercentage
            isBattery = false
            typeface = ChapterProvider.typeface
            textSize = 12f
        }
    }

    /**
     * 获取信息视图
     * @param tip 信息类型
     */
    private fun getTipView(tip: Int): BatteryView? = binding.run {
        return when (tip) {
            ReadTipConfig.tipHeaderLeft -> tvHeaderLeft
            ReadTipConfig.tipHeaderMiddle -> tvHeaderMiddle
            ReadTipConfig.tipHeaderRight -> tvHeaderRight
            ReadTipConfig.tipFooterLeft -> tvFooterLeft
            ReadTipConfig.tipFooterMiddle -> tvFooterMiddle
            ReadTipConfig.tipFooterRight -> tvFooterRight
            else -> null
        }
    }

    /**
     * 更新背景
     */
    fun upBg() {
        if (ReadBookConfig.bgAlpha < 100) {
            binding.vwRoot.backgroundColor = ReadBookConfig.bgMeanColor
        } else {
            binding.vwRoot.background = null
        }
        binding.vwBg.background = ReadBookConfig.bg
        upBgAlpha()
    }

    /**
     * 更新背景透明度
     */
    fun upBgAlpha() {
        binding.vwBg.alpha = ReadBookConfig.bgAlpha / 100f
    }

    /**
     * 更新时间信息
     */
    fun upTime() {
        tvTime?.text = timeFormat.format(Date(System.currentTimeMillis()))
        upTimeBattery()
    }

    /**
     * 更新电池信息
     */
    @SuppressLint("SetTextI18n")
    fun upBattery(battery: Int) {
        this.battery = battery
        tvBattery?.setBattery(battery)
        tvBatteryP?.text = "$battery%"
        upTimeBattery()
    }

    /**
     * 更新电池信息
     */
    @SuppressLint("SetTextI18n")
    private fun upTimeBattery() {
        val time = timeFormat.format(Date(System.currentTimeMillis()))
        tvTimeBattery?.setBattery(battery, time)
        tvTimeBatteryP?.text = "$time $battery%"
    }

    /**
     * 设置内容
     */
    fun setContent(textPage: TextPage, resetPageOffset: Boolean = true) {
        setProgress(textPage)
        if (resetPageOffset) {
            resetPageOffset()
        }
        binding.contentTextView.setContent(textPage)
    }

    /**
     * 设置无障碍文本
     */
    fun setContentDescription(content: String) {
        binding.contentTextView.contentDescription = content
    }

    /**
     * 重置滚动位置
     */
    fun resetPageOffset() {
        binding.contentTextView.resetPageOffset()
    }

    /**
     * 设置进度
     */
    @SuppressLint("SetTextI18n")
    fun setProgress(textPage: TextPage) = textPage.apply {
        tvBookName?.text = ReadBook.book?.name
        tvTitle?.text = textPage.title
        tvPage?.text = "${index.plus(1)}/$pageSize"
        tvTotalProgress?.text = readProgress
        tvPageAndTotal?.text = "${index.plus(1)}/$pageSize  $readProgress"
    }

    /**
     * 滚动事件
     */
    fun scroll(offset: Int) {
        binding.contentTextView.scroll(offset)
    }

    /**
     * 更新是否开启选择功能
     */
    fun upSelectAble(selectAble: Boolean) {
        binding.contentTextView.selectAble = selectAble
    }

    /**
     * 优先处理页面内单击
     * @return true:已处理, false:未处理
     */
    fun onClick(x: Float, y: Float): Boolean {
        return binding.contentTextView.click(x, y - headerHeight)
    }

    /**
     * 长按事件
     */
    fun longPress(
        x: Float, y: Float,
        select: (textPos: TextPos) -> Unit,
    ) {
        return binding.contentTextView.longPress(x, y - headerHeight, select)
    }

    /**
     * 选择文本
     */
    fun selectText(
        x: Float, y: Float,
        select: (textPos: TextPos) -> Unit,
    ) {
        return binding.contentTextView.selectText(x, y - headerHeight, select)
    }

    fun selectStartMove(x: Float, y: Float) {
        binding.contentTextView.selectStartMove(x, y - headerHeight)
    }

    fun selectStartMoveIndex(relativePagePos: Int, lineIndex: Int, charIndex: Int) {
        binding.contentTextView.selectStartMoveIndex(relativePagePos, lineIndex, charIndex)
    }

    fun selectEndMove(x: Float, y: Float) {
        binding.contentTextView.selectEndMove(x, y - headerHeight)
    }

    fun selectEndMoveIndex(relativePagePos: Int, lineIndex: Int, charIndex: Int) {
        binding.contentTextView.selectEndMoveIndex(relativePagePos, lineIndex, charIndex)
    }

    fun cancelSelect(fromSearchExit: Boolean = false) {
        binding.contentTextView.cancelSelect(fromSearchExit)
    }

    fun createBookmark(): Bookmark? {
        return binding.contentTextView.createBookmark()
    }

    fun relativePage(relativePagePos: Int): TextPage {
        return binding.contentTextView.relativePage(relativePagePos)
    }

    val textPage get() = binding.contentTextView.textPage

    val selectedText: String get() = binding.contentTextView.getSelectedText()

    val selectStartPos get() = binding.contentTextView.selectStart
}