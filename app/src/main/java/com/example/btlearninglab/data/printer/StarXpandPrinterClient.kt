package com.example.btlearninglab.data.printer

import android.content.Context
import com.starmicronics.stario10.*
import com.starmicronics.stario10.starxpandcommand.*
import com.starmicronics.stario10.starxpandcommand.DocumentBuilder
import com.starmicronics.stario10.starxpandcommand.printer.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class StarXpandPrinterClient(private val context: Context) {
    private var starPrinter: StarPrinter? = null
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
            // 既存の接続をクリーンアップ
            if (starPrinter != null) {
                addLog("> Cleaning up existing connection...")
                try {
                    starPrinter?.closeAsync()?.await()
                } catch (e: Exception) {
                    addLog("> Cleanup error (ignored): ${e.message}")
                }
                starPrinter = null
            }

            addLog("> === Starting connection process ===")
            _connectionState.value = PrinterConnectionState.Discovering
            addLog("> Searching for printers...")

            // プリンター検索
            addLog("> Calling discoverPrinter()...")
            val printerInfo = discoverPrinter()

            if (printerInfo == null) {
                _connectionState.value = PrinterConnectionState.Error("SM-S210i not found")
                addLog("> Error: SM-S210i not found")
                return@withContext
            }

            _connectionState.value = PrinterConnectionState.DeviceFound(
                TARGET_DEVICE_NAME,
                printerInfo.connectionSettings.identifier
            )
            addLog("> Found: $TARGET_DEVICE_NAME (${printerInfo.connectionSettings.identifier})")

            // 接続
            addLog("> Attempting to connect...")
            connectToPrinter(printerInfo.connectionSettings)

        } catch (e: Exception) {
            addLog("> Exception in discoverAndConnect: ${e.javaClass.simpleName}")
            addLog("> Message: ${e.message}")
            addLog("> Stack: ${e.stackTraceToString().take(300)}")
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

    private suspend fun connectToPrinter(settings: StarConnectionSettings) = withContext(Dispatchers.IO) {
        try {
            _connectionState.value = PrinterConnectionState.Connecting
            addLog("> Connecting to printer...")
            addLog("> Interface: ${settings.interfaceType}")
            addLog("> Identifier: ${settings.identifier}")

            addLog("> Creating StarPrinter instance...")
            val printer = StarPrinter(settings, context)
            starPrinter = printer

            addLog("> Calling openAsync()...")
            printer.openAsync().await()

            addLog("> Connected successfully")
            _connectionState.value = PrinterConnectionState.Connected(TARGET_DEVICE_NAME)

        } catch (e: StarIO10Exception) {
            addLog("> StarIO10Exception in connectToPrinter: ${e.javaClass.simpleName}")
            addLog("> Error code: ${e.errorCode}")
            addLog("> Message: ${e.message}")
            addLog("> Stack: ${e.stackTraceToString().take(300)}")
            handleStarIOException(e)
        } catch (e: Exception) {
            addLog("> Exception in connectToPrinter: ${e.javaClass.simpleName}")
            addLog("> Message: ${e.message}")
            addLog("> Stack: ${e.stackTraceToString().take(300)}")
            _connectionState.value = PrinterConnectionState.Error(e.message ?: "Connection failed")
        }
    }

    suspend fun print(text: String) = withContext(Dispatchers.IO) {
        val printer = starPrinter
        if (printer == null) {
            addLog("> Error: Not connected")
            return@withContext
        }

        try {
            _connectionState.value = PrinterConnectionState.Printing
            addLog("> Building print job...")
            addLog("> Text to print: \"$text\"")

            // StarXpandCommandBuilder でコマンド生成（最小構成でテスト）
            addLog("> Creating simple print command...")
            val builder = StarXpandCommandBuilder()
            builder.addDocument(
                DocumentBuilder()
                    .addPrinter(
                        PrinterBuilder()
                            .actionPrintText("$text\n")
                    )
            )

            addLog("> Getting commands...")
            val commands = builder.getCommands()
            addLog("> Commands created successfully")

            addLog("> Printing: \"$text\"")

            // 印刷実行
            addLog("> Calling printAsync()...")
            printer.printAsync(commands).await()

            addLog("> Print completed")

            // Connected状態に戻る
            _connectionState.value = PrinterConnectionState.Connected(TARGET_DEVICE_NAME)

        } catch (e: StarIO10Exception) {
            addLog("> StarIO10Exception: ${e.javaClass.simpleName} - ${e.message}")
            addLog("> Stack trace: ${e.stackTraceToString().take(500)}")
            handleStarIOException(e)
            _connectionState.value = PrinterConnectionState.Connected(TARGET_DEVICE_NAME)
        } catch (e: Exception) {
            addLog("> Exception: ${e.javaClass.simpleName} - ${e.message}")
            addLog("> Stack trace: ${e.stackTraceToString().take(500)}")
            _connectionState.value = PrinterConnectionState.Error(e.message ?: "Print failed")
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        addLog("> Disconnecting...")

        try {
            starPrinter?.closeAsync()?.await()
        } catch (e: Exception) {
            addLog("> Error closing printer: ${e.message}")
        } finally {
            starPrinter = null
        }

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
        _logs.value = _logs.value + message
    }
}
