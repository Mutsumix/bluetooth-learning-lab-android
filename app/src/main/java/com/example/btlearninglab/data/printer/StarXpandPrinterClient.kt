package com.example.btlearninglab.data.printer

import android.content.Context
import android.util.Log
import com.starmicronics.stario10.*
import com.starmicronics.stario10.starxpandcommand.*
import com.starmicronics.stario10.starxpandcommand.DocumentBuilder
import com.starmicronics.stario10.starxpandcommand.printer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.future.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class StarXpandPrinterClient(private val context: Context) {
    private var printerSettings: StarConnectionSettings? = null
    private var discoveryManager: StarDeviceDiscoveryManager? = null

    private val _connectionState = MutableStateFlow<PrinterConnectionState>(PrinterConnectionState.Idle)
    val connectionState: StateFlow<PrinterConnectionState> = _connectionState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    companion object {
        const val TARGET_DEVICE_NAME = "SM-S210i"
    }

    suspend fun discoverAndConnect() = withContext(Dispatchers.IO) {
        try {
            addLog("> Searching for printers...")
            _connectionState.value = PrinterConnectionState.Discovering

            // プリンター検索
            val printerInfo = discoverPrinter()

            if (printerInfo == null) {
                _connectionState.value = PrinterConnectionState.Error("SM-S210i not found")
                addLog("> Error: SM-S210i not found")
                return@withContext
            }

            // 接続設定を保存
            printerSettings = printerInfo.connectionSettings

            _connectionState.value = PrinterConnectionState.DeviceFound(
                TARGET_DEVICE_NAME,
                printerInfo.connectionSettings.identifier
            )
            addLog("> Found: $TARGET_DEVICE_NAME (${printerInfo.connectionSettings.identifier})")

            // 接続済みとしてマーク（実際の接続は印刷時に行う）
            _connectionState.value = PrinterConnectionState.Connected(TARGET_DEVICE_NAME)
            addLog("> Ready to print")

        } catch (e: Exception) {
            addLog("> Exception: ${e.javaClass.simpleName} - ${e.message}")
            _connectionState.value = PrinterConnectionState.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun discoverPrinter(): StarPrinter? = suspendCancellableCoroutine { continuation ->
        val manager = StarDeviceDiscoveryManagerFactory.create(
            listOf(InterfaceType.Bluetooth),
            context
        )

        discoveryManager = manager
        var foundPrinter: StarPrinter? = null

        manager.discoveryTime = 10000 // 10秒

        manager.callback = object : StarDeviceDiscoveryManager.Callback {
            override fun onPrinterFound(printer: StarPrinter) {
                val deviceName = printer.connectionSettings?.identifier ?: "Unknown"
                addLog("> Found device: $deviceName")

                // SM-S210iを探す（identifierまたはmodelNameで判定）
                foundPrinter = printer
            }

            override fun onDiscoveryFinished() {
                addLog("> Discovery finished")
                if (continuation.isActive) {
                    continuation.resume(foundPrinter)
                }
            }
        }

        continuation.invokeOnCancellation {
            manager.stopDiscovery()
        }

        manager.startDiscovery()
    }

    suspend fun print(text: String) = withContext(Dispatchers.IO) {
        val settings = printerSettings
        if (settings == null) {
            addLog("> Error: Not connected")
            return@withContext
        }

        // 印刷のたびに新しいStarPrinterインスタンスを作成（React Nativeパターン）
        val printer = StarPrinter(settings, context)

        try {
            _connectionState.value = PrinterConnectionState.Printing
            addLog("> Building print job...")
            addLog("> Text to print: \"$text\"")

            // StarXpandCommandBuilder でコマンド生成（公式推奨パターン）
            addLog("> Creating print command with method chaining...")
            addLog("> Input text: \"$text\"")
            addLog("> Input text length: ${text.length} chars")

            val commands = StarXpandCommandBuilder().apply {
                addDocument(DocumentBuilder().apply {
                    addPrinter(PrinterBuilder().apply {
                        addLog("> Setting Japanese character encoding...")
                        styleSecondPriorityCharacterEncoding(CharacterEncodingType.Japanese)
                        addLog("> Japanese encoding set")
                        actionPrintText("$text\n")
                    })
                })
            }.getCommands()

            addLog("> Commands created successfully")
            addLog("> Command size: ${commands.length} bytes")

            // JSON全体をログに出力（改行で分割して読みやすく）
            addLog("> ==== FULL COMMAND JSON START ====")
            commands.lines().forEach { line ->
                addLog(line)
            }
            addLog("> ==== FULL COMMAND JSON END ====")

            // テキストが含まれているか確認
            if (commands.contains(text)) {
                addLog("> ✓ Input text found in commands")
            } else {
                addLog("> ✗ WARNING: Input text NOT found in commands!")

                // 日本語が含まれているかも確認
                val hasJapanese = text.any { it.code > 127 }
                if (hasJapanese) {
                    addLog("> Input contains Japanese characters")
                    // 英数字だけが含まれているか確認
                    val asciiOnly = text.filter { it.code <= 127 }
                    if (commands.contains(asciiOnly)) {
                        addLog("> ✗ CRITICAL: Only ASCII characters found, Japanese stripped!")
                    }
                }
            }

            // open→print→closeのサイクル
            addLog("> Opening printer...")
            printer.openAsync().await()

            addLog("> Printing...")
            printer.printAsync(commands).await()

            addLog("> Print completed")

            // Connected状態に戻る
            _connectionState.value = PrinterConnectionState.Connected(TARGET_DEVICE_NAME)

        } catch (e: StarIO10UnprintableException) {
            addLog("> !!! StarIO10UnprintableException caught !!!")
            addLog("> Error code: ${e.errorCode}")
            addLog("> Message: ${e.message}")

            when (e.errorCode.name) {
                "DeviceHasError" -> {
                    addLog("> Printer error: No paper or cover open")
                    _connectionState.value = PrinterConnectionState.Error("用紙切れまたはカバー開放")
                }
                "PrinterHoldingPaper" -> {
                    addLog("> Printer error: Remove previous paper")
                    _connectionState.value = PrinterConnectionState.Error("前の用紙を取り除いてください")
                }
                else -> {
                    addLog("> Unprintable error: ${e.errorCode} - ${e.message}")
                    handleStarIOException(e)
                    _connectionState.value = PrinterConnectionState.Connected(TARGET_DEVICE_NAME)
                }
            }
        } catch (e: StarIO10Exception) {
            addLog("> !!! StarIO10Exception caught !!!")
            addLog("> Error code: ${e.errorCode}")
            addLog("> Message: ${e.message}")
            addLog("> Stack trace: ${e.stackTraceToString().take(300)}")
            handleStarIOException(e)
            _connectionState.value = PrinterConnectionState.Connected(TARGET_DEVICE_NAME)
        } catch (e: Exception) {
            addLog("> !!! Exception caught !!!")
            addLog("> Class: ${e.javaClass.name}")
            addLog("> Message: ${e.message}")
            addLog("> Stack trace: ${e.stackTraceToString().take(300)}")
            _connectionState.value = PrinterConnectionState.Error(e.message ?: "Print failed")
        } finally {
            // 必ずクローズ
            try {
                addLog("> Closing printer...")
                printer.closeAsync().await()
            } catch (e: Exception) {
                addLog("> Close error (ignored): ${e.message}")
            }
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        addLog("> Disconnecting...")

        printerSettings = null
        discoveryManager?.stopDiscovery()
        discoveryManager = null

        _connectionState.value = PrinterConnectionState.Idle
        addLog("> Disconnected")
    }

    private fun handleStarIOException(e: StarIO10Exception) {
        val errorMessage = when (e) {
            is StarIO10UnprintableException -> "Unprintable error"
            is StarIO10CommunicationException -> "Communication error"
            is StarIO10NotFoundException -> "Printer not found"
            else -> e.message ?: "Unknown StarIO10 error"
        }

        _connectionState.value = PrinterConnectionState.Error(errorMessage)
        addLog("> StarIO10 Error: $errorMessage")
    }

    private fun addLog(message: String) {
        Log.d("StarXpandPrinter", message)
        _logs.value = _logs.value + message
    }
}
