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
- UI: Jetpack Compose (Material 3)
- ナビゲーション: Navigation Compose
- BLE: Android Bluetooth LE API
- プリンター: StarXpand SDK
- HTTP: OkHttp

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

### Phase 2: UI実装 (次回)
- [ ] HomeScreen
- [ ] ScaleScreen
- [ ] PrinterScreen
- [ ] EPaperScreen

### Phase 3: 機能実装
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
