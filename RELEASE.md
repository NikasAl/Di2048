# Release — сборка подписанного APK/AAB для публикации

> Применимо к Di2048 «2048 Цифры» (applicationId `ru.electronikas.diagonal`)
> После P0-модернизации: Gradle 8.9, AGP 8.7.3, libGDX 1.12.1, compileSdk/targetSdk 35, Java 17, Yandex MobileAds 8.1

---

## 1. Предварительные требования

- JDK 17 или новее (требование AGP 8.7+)
- Android SDK с `platforms;android-35` и `build-tools;35.0.0`
- Keystore для подписи (см. раздел 2)
- `android/local.properties` с путём к Android SDK:
  ```
  sdk.dir=/home/nikas/Android/Sdk
  ```

---

## 2. Создание keystore (один раз, ~5 минут)

### 2.1 Сгенерировать keystore

```bash
keytool -genkeypair \
        -v \
        -keystore ~/keystores/di2048.keystore \
        -alias di2048 \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -storepass ВАШ_STORE_ПАРОЛЬ \
        -keypass ВАШ_KEY_ПАРОЛЬ \
        -dname "CN=Nikas Al, OU=Mobile, O=Electronikas, L=Saratov, ST=Saratov, C=RU"
```

**ВАЖНО:**
- Пароли должны быть стойкими (≥12 символов)
- Сохрани пароли в менеджер паролей — **если потеряешь, больше не сможешь публиковать обновления** того же приложения в Google Play / RuStore
- Файл `~/keystores/di2048.keystore` храни в надёжном месте (можно зашифровать, можно положить в облако с приватным доступом)

### 2.2 Создать `android/keystore.properties`

```bash
cp android/keystore.properties.example android/keystore.properties
```

Отредактируй `android/keystore.properties`, подставив свои значения:

```properties
storeFile=../keystores/di2048.keystore
storePassword=ВАШ_STORE_ПАРОЛЬ
keyAlias=di2048
keyPassword=ВАШ_KEY_ПАРОЛЬ
```

`keystore.properties` уже в `.gitignore` — он **не должен попасть в git**.

### 2.3 Проверить, что keystore работает

```bash
keytool -list -v -keystore ~/keystores/di2048.keystore -storepass ВАШ_STORE_ПАРОЛЬ
```

Должна появиться запись с алиасом `di2048`, типом `PrivateKeyEntry` и сроком действия 10000 дней.

---

## 3. Сборка

### 3.1 Release APK (для RuStore)

RuStore принимает и APK, и AAB. APK проще тестировать (можно сразу установить на устройство):

```bash
./gradlew :android:assembleRelease
```

**Результат:** `android/build/outputs/apk/release/android-release.apk`

**Размер:** ~10-13 МБ (R8 + shrinkResources ужимают ~30% по сравнению с debug)

### 3.2 Release AAB (для Google Play)

Google Play с августа 2021 требует именно `.aab` (App Bundle) для новых приложений. AAB позволяет GP генерировать оптимизированные APK под каждое устройство:

```bash
./gradlew :android:bundleRelease
```

**Результат:** `android/build/outputs/bundle/release/android-release.aab`

### 3.3 Очистка перед сборкой (если что-то странное)

```bash
./gradlew clean
./gradlew :android:assembleRelease
```

---

## 4. Проверка подписи

```bash
$ANDROID_HOME/build-tools/35.0.0/apksigner verify --verbose \
        android/build/outputs/apk/release/android-release.apk
```

Ожидаемый вывод:
```
Verifies
Verified using v1 scheme (JAR signing): true
Verified using v2 scheme (APK Signature Scheme v2): true
Verified using v3 scheme (APK Signature Scheme v3): true
Number of signers: 1
```

---

## 5. Установка на устройство для финального теста

```bash
adb install -r android/build/outputs/apk/release/android-release.apk
```

Запусти приложение, проверь:
- Запускается без крэша
- Баннер Yandex показывает рекламу (не пустой)
- Rewarded видео запускается по тапу на Undo/Del 2s после подтверждения
- Interstitial показывается после 3-го game over
- Звук включается/выключается
- Settings меню открывается и поле не двигается под ним

---

## 6. Публикация

### 6.1 RuStore

1. Зайди на https://console.rustore.ru/
2. Открой приложение «2048 Цифры» (если уже опубликовано — выбери «Обновить»)
3. Загрузи `android-release.apk`
4. Заполни/обнови листинг:
   - Иконка 512×512 (используй `Materials/icon512_new.png`)
   - Промо 1080×607 (используй `Materials/Promo_1080x607_new.png`)
   - Скриншоты (5+ PNG 1080×1920)
   - Описание (есть в `Materials/ASO_materials.md`)
   - Политика конфиденциальности (URL на `Materials/PRIVACY_POLICY.md` после публикации на GitHub Pages)
5. Отправь на модерацию (обычно 1-3 дня)

### 6.2 Google Play

1. Зайди на https://play.google.com/console/
2. Создай новое приложение (если ещё нет) или открой существующее
3. **Production → Create new release**
4. Загрузи `android-release.aab`
5. Заполни:
   - **App name:** «2048 Цифры» (локализованное)
   - **Description:** из `Materials/ASO_materials.md`
   - **App icon:** 512×512 PNG (`Materials/icon512_new.png`)
   - **Feature graphic:** 1024×500 (можно обрезать из `Materials/Promo_1080x607_new.png`)
   - **Screenshots:** 5+ PNG 1080×1920
   - **Privacy Policy URL:** ссылка на опубликованный `PRIVACY_POLICY.md`
6. **Data Safety Form:**
   - «Does your app collect or share any of the required user data types?» → **No** (Yandex MobileAds и MyTracker собирают AD_ID, но это не personal data в терминах GP)
   - «Does your app use advertising ID?» → **Yes**
   - «Is your app designed for children?» → **No** (target audience 13+)
7. **Target audience:** 13+ (из-за рекламы)
8. **Content rating:** заполнить questionnaire (получишь "Everyone")
9. Отправь на ревью (1-7 дней)

---

## 7. После публикации

### 7.1 Запросить отзывы

Попроси 5-10 знакомых:
- Установить приложение
- Поставить 5 звёзд
- Написать короткий отзыв («Затягивает», «Просто и со вкусом» и т.п.)

Это критически важно для ASO — приложения с рейтингом 0.0 имеют конверсию ~1%, с рейтингом 4.5+ — 3-5%.

### 7.2 Мониторинг

- **RuStore Console** → просмотры / установки / CR (через 7 дней)
- **Google Play Console** → Crashes / ANRs / Vitals (ежедневно первую неделю)
- **Yandex Advertising Network** → показы / eCPM / доход (ежедневно)
- **MyTracker** → события `UndoMoveOnClBut`, `Del2sOnClBut`, `Sound_On/Off`, `ContinueAfterGameOver` (через неделю)

### 7.3 Сравнение с baseline (до P0+P1)

| Метрика | До (2024) | Цель через 30 дней |
|---|---|---|
| CR RuStore | ~1% | 2.5-3.5% |
| Установок/день | ~1.5 | 4-6 |
| Rewarded показов/день | ~3 | 8-15 |
| Interstitial показов/день | ~6 | 10-15 |
| Доход/день | ~31 ₽ | 80-150 ₽ |
| eCPM общий | ~50 ₽ | 100-150 ₽ |

---

## 8. Откат / экстренная публикация

Если后发现 критический баг после публикации:

1. Откатить release в RuStore Console (Google Play не позволяет откатывать версии, можно только «unpublish»)
2. Создать hotfix-ветку от master:
   ```bash
   git checkout -b hotfix/v1.7.1 master
   ```
3. Исправить, поднять `versionCode` в `android/build.gradle` (например с 41 на 42)
4. Собрать release, опубликовать как новую версию
5. Замержить hotfix обратно в master:
   ```bash
   git checkout master
   git merge --no-ff hotfix/v1.7.1
   git tag v1.7.1
   git push origin master --tags
   ```

---

## 9. Частые проблемы

### `Keystore file not set for signing config release`

`android/keystore.properties` не существует или путь в нём неправильный. Создай файл из шаблона `keystore.properties.example`.

### `Failed to read key from keystore`

Неверный `keyAlias` или `keyPassword`. Проверь:
```bash
keytool -list -keystore ~/keystores/di2048.keystore -storepass ВАШ_STORE_ПАРОЛЬ
```

### `BUILD FAILED: minifyReleaseWithR8`

Проблема с ProGuard-правилами. Временно отключи минификацию для отладки:
```gradle
release {
    minifyEnabled false  // временно
    shrinkResources false
    ...
}
```
Если помогло — добавь недостающие `-keep` правила в `android/proguard-project.txt` для SDK, который падает.

### `apksigner: failed to verify`

Подпись некорректна. Пересобери с нуля:
```bash
./gradlew clean
./gradlew :android:assembleRelease
```

### APK устанавливается, но падает на старте

Скорее всего R8 вырезал нужный класс. Посмотри `adb logcat | grep -i "diagonal\|FATAL"` — там будет ClassNotFoundError. Добавь `-keep` правило для этого класса в `proguard-project.txt`.

---

## 10. Версионирование

В `android/build.gradle`:
```gradle
defaultConfig {
    applicationId "ru.electronikas.diagonal"
    minSdkVersion 24
    targetSdkVersion 35
    multiDexEnabled true
    versionCode 41          // целое число, монотонно растёт
    versionName "1.7.0"     // строка, semantic versioning
}
```

- **versionCode** — обязательное целое, увеличивай на 1 при каждом релизе (RuStore/GP не дают загрузить APK с тем же versionCode, что уже опубликован)
- **versionName** — показывается пользователям, формат `MAJOR.MINOR.PATCH`
  - MAJOR — breaking changes (например полный rewrite)
  - MINOR — новые фичи (P1: undo, del2s в HUD, и т.п.)
  - PATCH — багфиксы

Текущий релиз после P0+P1+UI-fixes: **versionCode=41, versionName=1.7.0**

Следующий hotfix: versionCode=42, versionName=1.7.1
Следующий minor: versionCode=43, versionName=1.8.0
