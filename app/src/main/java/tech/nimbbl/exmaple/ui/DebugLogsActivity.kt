package tech.nimbbl.exmaple.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Spanned
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.Menu
import android.widget.ImageButton
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.nimbbl.exmaple.R
import java.io.BufferedReader
import java.io.InputStreamReader

class DebugLogsActivity : AppCompatActivity() {
    private lateinit var tvLogs: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var btnMore: ImageButton
    private lateinit var scrollView: ScrollView
    private lateinit var etSearch: EditText
    private lateinit var btnSearchPrev: TextView
    private lateinit var btnSearchNext: TextView
    private lateinit var searchContainer: android.view.View

    private var logJob: Job? = null
    private var logProcess: java.lang.Process? = null
    private var isLogStreaming = false
    private var pauseMenuTitle: String = "Resume"

    private val buffer = StringBuilder()
    private val maxChars = 200_000
    private var activeSearchQuery: String? = null
    private var activeMatchIndex: Int? = null
    private var activeMatchOffsets: List<Int> = emptyList()

    // Streaming update batching: accumulate lines for 80ms before flushing to TextView.
    // This avoids O(n²) re-renders (tvLogs.text = fullText on every incoming line).
    private val uiHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val pendingLines = StringBuilder()
    private var flushScheduled = false
    private var bufferTrimmedSinceFlush = false
    private val flushRunnable = Runnable { flushPendingLines() }

    companion object {
        const val EXTRA_START_PAUSED = "extra_start_paused"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_debug_logs)

        val rootView = findViewById<android.view.View>(R.id.root)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Match Config screen behavior: apply system bar insets to the root container.
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvLogs = findViewById(R.id.tv_logs)
        scrollView = findViewById(R.id.sv_logs)
        etSearch = findViewById(R.id.et_search)
        searchContainer = findViewById(R.id.ll_search)
        btnSearchPrev = findViewById(R.id.btn_search_prev)
        btnSearchNext = findViewById(R.id.btn_search_next)
        btnBack = findViewById(R.id.btn_back)
        btnMore = findViewById(R.id.btn_more)

        btnBack.setOnClickListener { finish() }

        btnMore.setOnClickListener { showMoreMenu() }

        btnSearchNext.setOnClickListener { runSearch(direction = SearchDirection.NEXT) }
        btnSearchPrev.setOnClickListener { runSearch(direction = SearchDirection.PREV) }

        val startPaused = intent?.getBooleanExtra(EXTRA_START_PAUSED, true) ?: true
        if (startPaused) {
            setLogStreamingUi(isStreaming = false)
            dumpLogcatOnce()
        } else {
            startLogcat()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLogcat()
    }

    private fun startLogcat() {
        stopLogcat()
        isLogStreaming = true
        setLogStreamingUi(isStreaming = true)
        logJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                // Works best on debuggable builds. Filters to this app's PID.
                val pid = android.os.Process.myPid()
                val pb = ProcessBuilder("logcat", "-v", "time", "--pid=$pid")
                pb.redirectErrorStream(true)
                val proc = pb.start()
                logProcess = proc

                BufferedReader(InputStreamReader(proc.inputStream)).use { reader ->
                    var line: String?
                    while (true) {
                        line = reader.readLine() ?: break
                        appendLine(line!!)
                    }
                }
            } catch (e: Exception) {
                appendLine("Failed to read logcat: ${e.message}")
                appendLine("Tip: this screen works on debuggable builds; some devices restrict logcat access.")
            }
        }
    }

    private fun stopLogcat() {
        logJob?.cancel()
        logJob = null
        flushPendingLines()
        try {
            logProcess?.destroy()
        } catch (_: Exception) {
        }
        logProcess = null
        isLogStreaming = false
        setLogStreamingUi(isStreaming = false)
    }

    private fun flushPendingLines() {
        uiHandler.removeCallbacks(flushRunnable)
        flushScheduled = false
        val textToAppend = pendingLines.toString()
        pendingLines.clear()
        if (bufferTrimmedSinceFlush || !activeSearchQuery.isNullOrBlank()) {
            bufferTrimmedSinceFlush = false
            renderLogs(keepScrollPosition = true)
        } else if (textToAppend.isNotEmpty()) {
            tvLogs.append(textToAppend)
            scrollView.post { scrollView.fullScroll(android.view.View.FOCUS_DOWN) }
        }
    }

    private fun toggleLogStreaming() {
        if (isLogStreaming) stopLogcat() else startLogcat()
    }

    private fun setLogStreamingUi(isStreaming: Boolean) {
        pauseMenuTitle = if (isStreaming) "Pause" else "Resume"
    }

    private fun showMoreMenu() {
        val menu = PopupMenu(this, btnMore)
        // Force show icons if available (best-effort).
        try {
            val f = menu.javaClass.getDeclaredField("mPopup")
            f.isAccessible = true
            val helper = f.get(menu)
            helper.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java).invoke(helper, true)
        } catch (_: Exception) {
        }

        // Build menu each time to keep Pause/Resume label in sync.
        val pauseTitle = pauseMenuTitle
        val m = menu.menu
        m.add(Menu.NONE, 1, 1, "Search")
        m.add(Menu.NONE, 2, 2, pauseTitle)
        m.add(Menu.NONE, 3, 3, "Clear")
        m.add(Menu.NONE, 4, 4, "Copy")

        menu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    toggleSearchBar()
                    true
                }
                2 -> {
                    toggleLogStreaming()
                    true
                }
                3 -> {
                    buffer.clear()
                    tvLogs.text = ""
                    activeSearchQuery = null
                    true
                }
                4 -> {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("logs", buffer.toString()))
                    true
                }
                else -> false
            }
        }
        menu.show()
    }

    private fun dumpLogcatOnce() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pid = android.os.Process.myPid()
                // Dump everything currently available for this PID (up to logcat ring buffer limits).
                val pb = ProcessBuilder("logcat", "-d", "-v", "time", "--pid=$pid")
                pb.redirectErrorStream(true)
                val proc = pb.start()
                val snapshot = StringBuilder()
                BufferedReader(InputStreamReader(proc.inputStream)).use { reader ->
                    var line: String?
                    while (true) {
                        line = reader.readLine() ?: break
                        snapshot.append(line).append('\n')
                    }
                }
                try {
                    proc.destroy()
                } catch (_: Exception) {
                }
                appendSnapshot(snapshot.toString())
            } catch (e: Exception) {
                appendLine("Failed to dump logcat: ${e.message}")
            }
        }
    }

    private suspend fun appendSnapshot(text: String) {
        if (text.isBlank()) return
        // Keep within maxChars (best-effort) before rendering once.
        if (buffer.length + text.length > maxChars) {
            val keep = (maxChars / 2).coerceAtLeast(10_000)
            if (buffer.length > keep) buffer.delete(0, buffer.length - keep)
        }
        buffer.append(text)
        withContext(Dispatchers.Main) {
            renderLogs(keepScrollPosition = false)
            // Keep view at bottom after initial dump so "latest" is visible.
            scrollView.post { scrollView.fullScroll(android.view.View.FOCUS_DOWN) }
        }
    }

    private suspend fun appendLine(line: String) {
        val wasTrimmed = buffer.length > maxChars
        if (wasTrimmed) {
            buffer.delete(0, buffer.length - (maxChars / 2))
        }
        buffer.append(line).append('\n')
        withContext(Dispatchers.Main) {
            pendingLines.append(line).append('\n')
            if (wasTrimmed) bufferTrimmedSinceFlush = true
            if (!flushScheduled) {
                flushScheduled = true
                uiHandler.postDelayed(flushRunnable, 80)
            }
        }
    }

    private fun toggleSearchBar() {
        if (searchContainer.visibility == android.view.View.VISIBLE) {
            etSearch.setText("")
            searchContainer.visibility = android.view.View.GONE
            activeSearchQuery = null
            activeMatchIndex = null
            activeMatchOffsets = emptyList()
            renderLogs(keepScrollPosition = true)
        } else {
            searchContainer.visibility = android.view.View.VISIBLE
            etSearch.requestFocus()
        }
    }

    private enum class SearchDirection { NEXT, PREV }

    private fun runSearch(direction: SearchDirection) {
        val q = etSearch.text?.toString().orEmpty().trim()
        val newQuery = q.takeIf { it.isNotBlank() }

        if (newQuery != activeSearchQuery) {
            activeMatchIndex = null
            activeMatchOffsets = emptyList()
        }
        activeSearchQuery = newQuery

        val query = activeSearchQuery
        if (query.isNullOrBlank()) {
            renderLogs(keepScrollPosition = true)
            return
        }

        val text = getDisplayText()
        if (text.isEmpty()) return
        val lower = text.lowercase()
        val needle = query.lowercase()

        // Build a stable list of matches; then navigate by index.
        activeMatchOffsets = computeMatchOffsets(lower, needle)
        if (activeMatchOffsets.isEmpty()) return

        val layout = tvLogs.layout
        val viewportOffset = if (layout != null) {
            val currentTopLine = layout.getLineForVertical(scrollView.scrollY)
            layout.getLineStart(currentTopLine).coerceIn(0, text.length)
        } else 0

        val currentOffset = activeMatchIndex ?: viewportOffset
        val currentPos = activeMatchOffsets.indexOfLast { it <= currentOffset }.let { if (it >= 0) it else 0 }

        val targetPos = when (direction) {
            SearchDirection.NEXT -> (currentPos + 1).takeIf { it < activeMatchOffsets.size } ?: 0
            SearchDirection.PREV -> (currentPos - 1).takeIf { it >= 0 } ?: (activeMatchOffsets.size - 1)
        }
        val targetIdx = activeMatchOffsets[targetPos]

        activeMatchIndex = targetIdx
        // Don't restore scroll here; we'll jump to the match explicitly.
        renderLogs(keepScrollPosition = false)
        scrollToMatchOffset(targetIdx)
    }

    private fun computeMatchOffsets(lowerText: String, needle: String): List<Int> {
        if (needle.isEmpty()) return emptyList()
        val offsets = ArrayList<Int>(32)
        var idx = lowerText.indexOf(needle, 0)
        while (idx >= 0) {
            offsets.add(idx)
            idx = lowerText.indexOf(needle, idx + needle.length)
        }
        return offsets
    }

    private fun renderLogs(keepScrollPosition: Boolean) {
        val y = if (keepScrollPosition) scrollView.scrollY else null

        val query = activeSearchQuery?.takeIf { it.isNotBlank() }
        if (query == null) {
            tvLogs.text = getDisplayText()
            if (y != null) scrollView.post { scrollView.scrollTo(0, y) }
            return
        }

        val text = getDisplayText()
        val lower = text.lowercase()
        val q = query.lowercase()

        val spannable = SpannableString(text)
        var idx = lower.indexOf(q)
        while (idx >= 0) {
            spannable.setSpan(
                // Stronger highlight yellow
                BackgroundColorSpan(0xCCFFF59D.toInt()),
                idx,
                (idx + q.length).coerceAtMost(text.length),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            idx = lower.indexOf(q, idx + q.length)
        }
        tvLogs.text = spannable

        if (y != null) scrollView.post { scrollView.scrollTo(0, y) }
    }

    private fun getDisplayText(): String {
        return buffer.toString()
    }

    private fun scrollToMatchOffset(offset: Int) {
        tvLogs.post {
            val layout = tvLogs.layout ?: return@post
            val safeOffset = offset.coerceIn(0, (tvLogs.text?.length ?: 0).coerceAtLeast(0))
            val line = layout.getLineForOffset(safeOffset)
            val y = layout.getLineTop(line)
            scrollView.smoothScrollTo(0, y)
        }
    }
}

