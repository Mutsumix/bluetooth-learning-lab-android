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

    private val _scannedPrinters = MutableStateFlow<List<ScannedPrinter>>(emptyList())
    val scannedPrinters: StateFlow<List<ScannedPrinter>> = _scannedPrinters.asStateFlow()

    companion object {
        const val TARGET_DEVICE_NAME = "SM-S210i"
    }

    suspend fun discoverPrinters() = withContext(Dispatchers.IO) {
        try {
            addLog("> Searching for printers...")
            _connectionState.value = PrinterConnectionState.Discovering
            _scannedPrinters.value = emptyList()

            val printers = discoverAllPrinters()

            if (printers.isEmpty()) {
                _connectionState.value = PrinterConnectionState.Error("No printers found")
                addLog("> Error: No printers found")
                return@withContext
            }

            _connectionState.value = PrinterConnectionState.Idle
            addLog("> Discovery complete. Found ${printers.size} printer(s)")

        } catch (e: Exception) {
            addLog("> Exception: ${e.javaClass.simpleName} - ${e.message}")
            _connectionState.value = PrinterConnectionState.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun connectToPrinter(scannedPrinter: ScannedPrinter) = withContext(Dispatchers.IO) {
        try {
            addLog("> Connecting to ${scannedPrinter.name}...")

            val settings = StarConnectionSettings(
                InterfaceType.Bluetooth,
                scannedPrinter.identifier
            )

            printerSettings = settings

            _connectionState.value = PrinterConnectionState.DeviceFound(
                scannedPrinter.name,
                scannedPrinter.identifier
            )
            addLog("> Found: ${scannedPrinter.name} (${scannedPrinter.identifier})")

            _connectionState.value = PrinterConnectionState.Connected(scannedPrinter.name)
            addLog("> Ready to print")

        } catch (e: Exception) {
            addLog("> Exception: ${e.javaClass.simpleName} - ${e.message}")
            _connectionState.value = PrinterConnectionState.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun discoverAndConnect() = withContext(Dispatchers.IO) {
        try {
            addLog("> Searching for printers...")
            _connectionState.value = PrinterConnectionState.Discovering

            val printerInfo = discoverPrinter()

            if (printerInfo == null) {
                _connectionState.value = PrinterConnectionState.Error("SM-S210i not found")
                addLog("> Error: SM-S210i not found")
                return@withContext
            }

            printerSettings = printerInfo.connectionSettings

            _connectionState.value = PrinterConnectionState.DeviceFound(
                TARGET_DEVICE_NAME,
                printerInfo.connectionSettings.identifier
            )
            addLog("> Found: $TARGET_DEVICE_NAME (${printerInfo.connectionSettings.identifier})")

            _connectionState.value = PrinterConnectionState.Connected(TARGET_DEVICE_NAME)
            addLog("> Ready to print")

        } catch (e: Exception) {
            addLog("> Exception: ${e.javaClass.simpleName} - ${e.message}")
            _connectionState.value = PrinterConnectionState.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun discoverAllPrinters(): List<ScannedPrinter> = suspendCancellableCoroutine { continuation ->
        val manager = StarDeviceDiscoveryManagerFactory.create(
            listOf(InterfaceType.Bluetooth),
            context
        )

        discoveryManager = manager
        val foundPrinters = mutableListOf<ScannedPrinter>()

        manager.discoveryTime = 10000

        manager.callback = object : StarDeviceDiscoveryManager.Callback {
            override fun onPrinterFound(printer: StarPrinter) {
                val settings = printer.connectionSettings
                if (settings != null) {
                    val scannedPrinter = ScannedPrinter(
                        name = settings.identifier,
                        identifier = settings.identifier,
                        modelName = printer.information?.model?.name ?: "Unknown Model"
                    )
                    foundPrinters.add(scannedPrinter)
                    _scannedPrinters.value = foundPrinters.toList()
                    addLog("> Found device: ${scannedPrinter.name}")
                }
            }

            override fun onDiscoveryFinished() {
                addLog("> Discovery finished")
                if (continuation.isActive) {
                    continuation.resume(foundPrinters.toList())
                }
            }
        }

        continuation.invokeOnCancellation {
            manager.stopDiscovery()
        }

        manager.startDiscovery()
    }

    private suspend fun discoverPrinter(): StarPrinter? = suspendCancellableCoroutine { continuation ->
        val manager = StarDeviceDiscoveryManagerFactory.create(
            listOf(InterfaceType.Bluetooth),
            context
        )

        discoveryManager = manager
        var foundPrinter: StarPrinter? = null

        manager.discoveryTime = 10000

        manager.callback = object : StarDeviceDiscoveryManager.Callback {
            override fun onPrinterFound(printer: StarPrinter) {
                val deviceName = printer.connectionSettings?.identifier ?: "Unknown"
                addLog("> Found device: $deviceName")

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
            addLog("> Printing: \"$text\"")

            val commands = StarXpandCommandBuilder().apply {
                addDocument(DocumentBuilder().apply {
                    addPrinter(PrinterBuilder().apply {
                        styleSecondPriorityCharacterEncoding(CharacterEncodingType.Japanese)
                        actionPrintText("\n\n$text\n\n\n")
                    })
                })
            }.getCommands()

            addLog("> Opening printer...")
            printer.openAsync().await()

            addLog("> Sending print command...")
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
