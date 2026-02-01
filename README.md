# BT Learning Lab

Bluetooth学習アプリ - Android版

## プロジェクト概要

技術同人誌用のBluetooth通信学習Androidアプリ。3種類のデバイスとの通信を通じて、BLE・Bluetooth Classic・HTTP通信を体験できる。

### 対象デバイス

- Decent Scale (BLE)
- SM-S210i (StarXpand SDK)
- Gicisky 2.9" E-Paper (HTTP)

## 技術スタック

- 言語: Kotlin
- アーキテクチャ: MVVM (ViewModel + StateFlow + UiState)
- UI: Jetpack Compose (Material 3)
- ナビゲーション: Navigation Compose
- 状態管理: Kotlin Coroutines + Flow
- BLE: Android Bluetooth LE API (予定)
- プリンター: StarXpand SDK (予定)
- HTTP: OkHttp (予定)

## プロジェクト構成

```
app/src/main/java/com/example/btlearninglab/
├── MainActivity.kt
├── navigation/
│   └── NavGraph.kt
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
    │   └── ScaleUiState.kt
    ├── printer/
    │   ├── PrinterScreen.kt
    │   ├── PrinterViewModel.kt
    │   └── PrinterUiState.kt
    ├── epaper/
    │   ├── EPaperScreen.kt
    │   ├── EPaperViewModel.kt
    │   └── EPaperUiState.kt
    └── log/
        ├── LogScreen.kt
        ├── LogViewModel.kt
        └── LogUiState.kt
```

## 現在の実装

### アーキテクチャ
- **MVVM**: 各画面はScreen (View)、ViewModel、UiState (Model) で構成
- **単一方向データフロー**: StateFlowによる状態管理
- **型安全な状態表現**: sealed interfaceによるUiState定義

### UI/UX
- **Material 3デザイン**: Jetpack Composeによる最新UI
- **統一されたデザインシステム**: AppColorsによるカラーパレット管理
- **レスポンシブレイアウト**: 縦スクロール対応、適切な余白設定
- **視覚的フィードバック**: ローディング状態、エラー表示、ボタン無効化

### モック実装
現在は実際のBluetooth/HTTP通信は実装されておらず、以下の機能がモックで動作:
- Scale: 接続・切断・Tare（重量リセット）
- Printer: 接続・切断・印刷（テキスト送信）
- EPaper: HTTP送信（画像アップロード）
- 各画面で通信ログを表示

## セットアップ

1. Android Studioでプロジェクトを開く
2. Gradle Syncを実行
3. エミュレーターまたは実機でビルド

## 開発状況

### Phase 1: 基盤構築 ✅
- [x] Androidプロジェクト作成
- [x] 依存関係追加
- [x] パーミッション設定
- [x] 画面遷移の骨組み

### Phase 2: UI実装 ✅
- [x] MVVMアーキテクチャ導入 (ViewModel + StateFlow + UiState)
- [x] デザインシステム構築 (AppColors, 共通コンポーネント)
- [x] HomeScreen (デバイス一覧・ステータス表示)
- [x] ScaleScreen (重量表示・Tare機能)
- [x] PrinterScreen (テキスト入力・印刷機能)
- [x] EPaperScreen (画像プレビュー・送信機能)
- [x] LogScreen (通信ログ表示)
- [x] BottomNavigationBar (画面遷移)
- [x] モック実装による動作確認

### Phase 3: 機能実装 (次回)
- [ ] Decent Scale（Android BLE API）
- [ ] SM-S210i（StarXpand SDK）
- [ ] Gicisky（HTTP API）

## ビルド方法

```bash
./gradlew assembleDebug
```

## 必要なパーミッション

- Bluetooth関連 (BLUETOOTH, BLUETOOTH_ADMIN, BLUETOOTH_CONNECT, BLUETOOTH_SCAN)
- 位置情報 (ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
- ネットワーク (INTERNET, ACCESS_NETWORK_STATE)

## ライセンス

Private Project
