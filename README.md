# BT Learning Lab

Bluetooth学習アプリ - Android版

## プロジェクト概要

技術同人誌用のBluetooth通信学習Androidアプリ。3種類のデバイスとの通信を通じて、BLE・Bluetooth Classic・HTTP通信を体験できる。

### 対象デバイス

| デバイス | 通信方式 | SDK/ライブラリ |
|---------|---------|---------------|
| Decent Scale | BLE (Bluetooth LE) | Android Bluetooth LE API |
| SM-S210i | Bluetooth Classic | StarXpand SDK v1.7.0 |
| Gicisky 2.9" E-Paper | HTTP (ESP32経由) | OkHttp 4.12.0 |

## 技術スタック

- **言語**: Kotlin
- **パッケージ名**: `com.musumix.btlearninglab`
- **アーキテクチャ**: MVVM (ViewModel + StateFlow + UiState)
- **UI**: Jetpack Compose (Material 3)
- **ナビゲーション**: Navigation Compose
- **状態管理**: Kotlin Coroutines + Flow
- **BLE**: Android Bluetooth LE API
- **プリンター**: StarXpand SDK
- **HTTP**: OkHttp
- **Min SDK**: 31 / **Target SDK**: 35

## プロジェクト構成

```
app/src/main/java/com/musumix/btlearninglab/
├── MainActivity.kt
├── navigation/
│   └── NavGraph.kt
├── data/
│   ├── ble/
│   │   ├── BluetoothManager.kt
│   │   ├── DecentScaleBleClient.kt
│   │   ├── DecentScaleDataParser.kt
│   │   ├── BleConnectionState.kt
│   │   ├── PermissionHelper.kt
│   │   ├── ScaleDeviceRepository.kt
│   │   └── ScannedDevice.kt
│   ├── printer/
│   │   ├── StarXpandPrinterClient.kt
│   │   ├── PrinterConnectionState.kt
│   │   ├── PrinterDeviceRepository.kt
│   │   └── ScannedPrinter.kt
│   ├── epaper/
│   │   ├── EPaperTagRepository.kt
│   │   └── EPaperTag.kt
│   ├── http/
│   │   ├── EPaperHttpClient.kt
│   │   └── ImageGenerator.kt
│   └── scale/
│       └── WeightRepository.kt
└── ui/
    ├── components/
    │   └── BottomNavigationBar.kt
    ├── theme/
    │   ├── Color.kt
    │   ├── Theme.kt
    │   └── Type.kt
    ├── home/
    │   ├── HomeScreen.kt
    │   ├── HomeViewModel.kt
    │   └── HomeUiState.kt
    ├── scale/
    │   ├── ScaleScreen.kt
    │   ├── ScaleViewModel.kt
    │   ├── ScaleViewModelFactory.kt
    │   └── ScaleUiState.kt
    ├── printer/
    │   ├── PrinterScreen.kt
    │   ├── PrinterViewModel.kt
    │   ├── PrinterViewModelFactory.kt
    │   └── PrinterUiState.kt
    ├── epaper/
    │   ├── EPaperScreen.kt
    │   ├── EPaperViewModel.kt
    │   ├── EPaperViewModelFactory.kt
    │   └── EPaperUiState.kt
    └── log/
        ├── LogScreen.kt
        ├── LogViewModel.kt
        └── LogUiState.kt
```

## 画面構成

- **Home**: デバイス一覧・接続ステータス表示
- **Scale**: Decent Scaleのリアルタイム重量表示・Tare（ゼロリセット）・BLEスキャン
- **Printer**: SM-S210iデバイス検出・接続・日本語テキスト印刷
- **E-Paper**: Giciskyタグ管理・IP/MACアドレス設定・画像プレビュー・送信
- **Log**: 全デバイスの通信ログ（タイムスタンプ・操作ラベル・サービス詳細付き）

## 実装状況

- **Decent Scale (BLE)**: 実装済み - BLEスキャン・接続・リアルタイム重量取得・Tareコマンド・通信ログ
- **SM-S210i (Printer)**: 実装済み - StarXpand SDKによるBluetooth接続・日本語印刷対応
- **Gicisky E-Paper (HTTP)**: 実装済み - ESP32 AP経由のHTTP通信・画像送信

## セットアップ

1. Android Studioでプロジェクトを開く
2. Gradle Syncを実行
3. 実機でビルド（BLE機能はエミュレーター非対応）

## ビルド方法

```bash
# デバッグビルド
./gradlew assembleDebug

# リリースビルド（署名付きAAB）
./gradlew bundleRelease
```

リリースビルドには `release-keystore.jks` が必要です（`.gitignore`に含まれているためリポジトリには含まれません）。

## 必要なパーミッション

- `BLUETOOTH_CONNECT` - Bluetooth接続操作
- `BLUETOOTH_SCAN` - デバイス検出（位置情報不要）
- `INTERNET` - E-Paper HTTP通信
- `ACCESS_NETWORK_STATE` - ネットワーク状態確認

## ライセンス

Private Project
