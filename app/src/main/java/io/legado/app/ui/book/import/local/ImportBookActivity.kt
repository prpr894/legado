package io.legado.app.ui.book.import.local

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import io.legado.app.R
import io.legado.app.constant.AppConst
import io.legado.app.constant.PreferKey
import io.legado.app.databinding.DialogEditTextBinding
import io.legado.app.help.config.AppConfig
import io.legado.app.lib.dialogs.alert
import io.legado.app.lib.permission.Permissions
import io.legado.app.lib.permission.PermissionsCompat
import io.legado.app.lib.theme.backgroundColor
import io.legado.app.ui.book.import.BaseImportBookActivity
import io.legado.app.ui.file.HandleFileContract
import io.legado.app.ui.widget.SelectActionBar
import io.legado.app.utils.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import java.io.File

/**
 * 导入本地书籍界面
 */
class ImportBookActivity : BaseImportBookActivity<ImportBookViewModel>(),
    PopupMenu.OnMenuItemClickListener,
    ImportBookAdapter.CallBack,
    SelectActionBar.CallBack {

    override val viewModel by viewModels<ImportBookViewModel>()
    private val adapter by lazy { ImportBookAdapter(this, this) }
    private var scanDocJob: Job? = null
    private var menuItem: MenuItem? = null

    private var searchSelectIndex = -1
    private val searchResultIndex: MutableList<Int> = ArrayList()

    private val selectFolder = registerForActivityResult(HandleFileContract()) {
        it.uri?.let { uri ->
            AppConfig.importBookPath = uri.toString()
            initRootDoc(true)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        searchView.queryHint = getString(R.string.screen) + " • " + getString(R.string.local_book)
        launch {
            initView()
            initEvent()
            if (setBookStorage() && AppConfig.importBookPath.isNullOrBlank()) {
                AppConfig.importBookPath = AppConfig.defaultBookTreeUri
            }
            initData()
        }
    }

    override fun onCompatCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.import_book, menu)
        return super.onCompatCreateOptionsMenu(menu)
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        menu.findItem(R.id.menu_sort_name)?.isChecked = viewModel.sort == 0
        menu.findItem(R.id.menu_sort_size)?.isChecked = viewModel.sort == 1
        menu.findItem(R.id.menu_sort_time)?.isChecked = viewModel.sort == 2
        return super.onMenuOpened(featureId, menu)
    }

    override fun onCompatOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_select_folder -> selectFolder.launch()
            R.id.menu_scan_folder -> scanFolder()
            R.id.menu_import_file_name -> alertImportFileName()
            R.id.menu_sort_name -> upSort(0)
            R.id.menu_sort_size -> upSort(1)
            R.id.menu_sort_time -> upSort(2)
            R.id.menu_search -> {
                menuItem = item
                item.isVisible = false
                searchClick()
            }
        }
        return super.onCompatOptionsItemSelected(item)
    }

    private fun searchClick() {
        searchView.visibility = View.VISIBLE
        searchView.performClick()
        binding.llSearchResultCtr.visibility = View.VISIBLE
        binding.tvSearchResultIndex.text = resources.getString(R.string.empty)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_del_selection -> viewModel.deleteDoc(adapter.selectedUris) {
                adapter.removeSelection()
            }
        }
        return false
    }

    override fun selectAll(selectAll: Boolean) {
        adapter.selectAll(selectAll)
    }

    override fun revertSelection() {
        adapter.revertSelection()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClickSelectBarMainAction() {
        viewModel.addToBookshelf(adapter.selectedUris) {
            adapter.selectedUris.clear()
            adapter.notifyDataSetChanged()
        }
    }

    private fun initView() {
        binding.layTop.setBackgroundColor(backgroundColor)
        binding.tvEmptyMsg.setText(R.string.empty_msg_import_book)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        binding.selectActionBar.setMainActionText(R.string.add_to_bookshelf)
        binding.selectActionBar.inflateMenu(R.menu.import_book_sel)
        binding.selectActionBar.setOnMenuItemClickListener(this)
        binding.selectActionBar.setCallBack(this)
        searchView.visibility = View.GONE
        binding.llSearchResultCtr.visibility = View.GONE
        binding.imgBtnPre.setOnClickListener { preSearchResult() }
        binding.imgBtnNext.setOnClickListener { nextSearchResult() }
        binding.tvSearchResultIndex.setOnClickListener { toastOnUi(binding.tvSearchResultIndex.text.trim()) }
        binding.tvSearchResultIndex.setOnLongClickListener { view ->
            //TODO 添加跳转到指定搜索结果
            true
        }
    }

    private fun initEvent() {
        binding.tvGoBack.setOnClickListener {
            goBackDir()
        }
        searchView.setOnCloseListener {
            searchView.isVisible = false
            menuItem?.isVisible = true
            binding.llSearchResultCtr.visibility = View.GONE
            searchSelectIndex = -1
            searchResultIndex.clear()
            binding.tvSearchResultIndex.text = resources.getString(R.string.empty)
            adapter.refreshSearchResult(-1)
            false
        }
        searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchResultIndex.clear()
                val bookFileNames = adapter.getItems()
                for (i in bookFileNames.indices) {
                    if (query != null && bookFileNames[i].name.contains(query)) {
                        searchResultIndex.add(i)
                    }
                }
                firstSearchResult()
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun nextSearchResult() {
        val j = getIndexByValueInSearchResultIndex(searchSelectIndex) + 1
        if (searchResultIndex.size > j) {
            val i = searchResultIndex[j]
            searchSelectIndex = i
            refreshSearchResult(j, i)
        }

    }

    private fun preSearchResult() {
        val j = getIndexByValueInSearchResultIndex(searchSelectIndex) - 1
        if (searchResultIndex.size >= j && j >= 0) {
            val i = searchResultIndex[j]
            searchSelectIndex = i
            refreshSearchResult(j, i)
        }
    }

    private fun firstSearchResult() {
        if (searchResultIndex.size > 0) {
            val i = searchResultIndex[0]
            searchSelectIndex = i
            refreshSearchResult(0, i)
        }
    }

    private fun getIndexByValueInSearchResultIndex(indexValue: Int): Int {
        for (i in 0 until searchResultIndex.size) {
            if (searchResultIndex[i] == indexValue) {
                return i
            }
        }
        return -1
    }

    private fun refreshSearchResult(j: Int, i: Int) {
        val size = searchResultIndex.size
        val jI = j + 1
        val indexToShow = "$jI/$size"
        binding.tvSearchResultIndex.text = indexToShow
        binding.recyclerView.scrollToPosition(i)
        adapter.refreshSearchResult(i)
    }

    private fun initData() {
        viewModel.dataFlowStart = {
            initRootDoc()
        }
        launch {
            viewModel.dataFlow.conflate().collect { docs ->
                adapter.setItems(docs)
            }
        }
    }

    private fun initRootDoc(changedFolder: Boolean = false) {
        if (viewModel.rootDoc != null && !changedFolder) {
            upPath()
        } else {
            val lastPath = AppConfig.importBookPath
            if (lastPath.isNullOrBlank()) {
                binding.tvEmptyMsg.visible()
                selectFolder.launch()
            } else {
                val rootUri = if (lastPath.isUri()) {
                    Uri.parse(lastPath)
                } else {
                    Uri.fromFile(File(lastPath))
                }
                when {
                    rootUri.isContentScheme() -> {
                        kotlin.runCatching {
                            val doc = DocumentFile.fromTreeUri(this, rootUri)
                            if (doc == null || doc.name.isNullOrEmpty()) {
                                binding.tvEmptyMsg.visible()
                                selectFolder.launch()
                            } else {
                                viewModel.subDocs.clear()
                                viewModel.rootDoc = FileDoc.fromDocumentFile(doc)
                                upDocs(viewModel.rootDoc!!)
                            }
                        }.onFailure {
                            binding.tvEmptyMsg.visible()
                            selectFolder.launch()
                        }
                    }
                    AppConst.isPlayChannel -> {
                        binding.tvEmptyMsg.visible()
                        selectFolder.launch()
                    }
                    else -> initRootPath(rootUri.path!!)
                }
            }
        }
    }

    private fun initRootPath(path: String) {
        binding.tvEmptyMsg.visible()
        PermissionsCompat.Builder()
            .addPermissions(*Permissions.Group.STORAGE)
            .rationale(R.string.tip_perm_request_storage)
            .onGranted {
                kotlin.runCatching {
                    viewModel.rootDoc = FileDoc.fromFile(File(path))
                    viewModel.subDocs.clear()
                    upPath()
                }.onFailure {
                    binding.tvEmptyMsg.visible()
                    selectFolder.launch()
                }
            }
            .request()
    }

    private fun upSort(sort: Int) {
        viewModel.sort = sort
        putPrefInt(PreferKey.localBookImportSort, sort)
        if (scanDocJob?.isActive != true) {
            viewModel.dataCallback?.setItems(adapter.getItems())
        }
    }

    @Synchronized
    private fun upPath() {
        binding.tvGoBack.isEnabled = viewModel.subDocs.isNotEmpty()
        viewModel.rootDoc?.let {
            scanDocJob?.cancel()
            upDocs(it)
        }
    }

    private fun upDocs(rootDoc: FileDoc) {
        binding.tvEmptyMsg.gone()
        var path = rootDoc.name + File.separator
        var lastDoc = rootDoc
        for (doc in viewModel.subDocs) {
            lastDoc = doc
            path = path + doc.name + File.separator
        }
        binding.tvPath.text = path
        adapter.selectedUris.clear()
        adapter.clearItems()
        viewModel.loadDoc(lastDoc)
    }

    /**
     * 扫描当前文件夹及所有子文件夹
     */
    private fun scanFolder() {
        viewModel.rootDoc?.let { doc ->
            adapter.clearItems()
            val lastDoc = viewModel.subDocs.lastOrNull() ?: doc
            binding.refreshProgressBar.isAutoLoading = true
            scanDocJob?.cancel()
            scanDocJob = launch(IO) {
                viewModel.scanDoc(lastDoc, true, this) {
                    launch {
                        binding.refreshProgressBar.isAutoLoading = false
                    }
                }
            }
        }
    }

    private fun alertImportFileName() {
        alert(R.string.import_file_name) {
            setMessage("""使用js处理文件名变量src，将书名作者分别赋值到变量name author""")
            val alertBinding = DialogEditTextBinding.inflate(layoutInflater).apply {
                editView.hint = "js"
                editView.setText(AppConfig.bookImportFileName)
            }
            customView { alertBinding.root }
            okButton {
                AppConfig.bookImportFileName = alertBinding.editView.text?.toString()
            }
            cancelButton()
        }
    }

    @Synchronized
    override fun nextDoc(fileDoc: FileDoc) {
        viewModel.subDocs.add(fileDoc)
        upPath()
    }

    @Synchronized
    private fun goBackDir(): Boolean {
        return if (viewModel.subDocs.isNotEmpty()) {
            viewModel.subDocs.removeAt(viewModel.subDocs.lastIndex)
            upPath()
            true
        } else {
            false
        }
    }

    override fun onSearchTextChange(newText: String?) {
        viewModel.updateCallBackFlow(newText)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (!goBackDir()) {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    override fun upCountView() {
        binding.selectActionBar.upCountView(adapter.selectedUris.size, adapter.checkableCount)
    }

    override fun startRead(fileDoc: FileDoc) {
        if (!ArchiveUtils.isArchive(fileDoc.name)) {
            startReadBook(fileDoc.toString())
        } else {
            onArchiveFileClick(fileDoc)
        }
    }

}
