# Privacy Policy — "2048 Numbers" (Di2048)

**Effective date:** June 20, 2026

This Privacy Policy describes how the developer of the "2048 Numbers" application (hereinafter "the App", package name `ru.electronikas.diagonal`) handles user data. The App is distributed through the RuStore and Google Play app stores.

## 1. What data is collected

### 1.1 Data collected directly by the App

**The App does NOT collect, store, or transmit to the developer the following categories of data:**
- Personal data (full name, email, phone number, address)
- Financial data (card numbers, payment information)
- Contact data
- User messages
- User photos and images
- Geolocation

**The App stores locally on the user's device (and does not transmit to the developer's servers):**
- Current game state (tile positions on the board) — to allow continuing the game after closing
- Best score for each board size
- Settings (sound volume, selected board size)

This data is stored in Android's local preferences (`SharedPreferences`) and is deleted when the App is uninstalled.

### 1.2 Data collected by the advertising SDK

The App uses the **Yandex Mobile Ads SDK** (Yandex Advertising Network) to display ads (banner, interstitial, rewarded). The Yandex Mobile Ads SDK may collect the following data:

- **Advertising ID (GAID)** — used to display relevant ads and measure their effectiveness
- **IP address** — for transmitting ad materials
- **Device information** — model, operating system, OS version, language, screen size, carrier
- **Network information** — connection type (Wi-Fi / mobile), carrier
- **Approximate geolocation** (country/region level, by IP) — for ad targeting

Yandex Advertising Network privacy policy: https://legal.yandex.com/partner_ch/

### 1.3 Data collected by the analytics SDK

The App uses the **MyTracker SDK** (Mail.ru Group / VK) for aggregated usage analytics. MyTracker may collect:

- **Advertising ID (GAID)**
- **Device identifier** (Android ID)
- **IP address**
- **Device information** — model, OS, version
- **In-app events** — launches, user actions (e.g. tapping "Undo move", "Delete 2s")
- **Approximate geolocation** (country/region level, by IP)

MyTracker privacy policy: https://tracker.my.com/legal/privacy/en

## 2. How collected data is used

### 2.1 Processing purposes

- Displaying and targeting ads via Yandex Mobile Ads
- Measuring ad effectiveness (eCPM, impressions, clicks)
- Collecting aggregated App usage analytics via MyTracker
- Improving the user experience based on anonymous analytics
- Fraud protection and security

### 2.2 Sharing with third parties

The developer **does not share** collected data with third parties, except:
- Sharing data with the Yandex Mobile Ads and MyTracker SDKs as described in sections 1.2 and 1.3
- Requirements of applicable law (upon official request from government authorities)

### 2.3 Data retention

- Local data (game state, settings) is stored on the user's device until the App is uninstalled
- Data collected by SDKs is retained in accordance with the policies of the respective services (see links in sections 1.2 and 1.3)

## 3. Advertising and personalization

### 3.1 Ad formats

The App displays the following ad formats:
- **Banner** — at the bottom of the screen during gameplay
- **Interstitial** — after a game ends (frequency-capped: no more than one display per three completed games, no more than once every 2 minutes)
- **Rewarded** — initiated by the user, when tapping "Undo move" or "Delete 2s" buttons

### 3.2 Ad personalization

Ads in the App may be personalized based on data collected by Yandex Mobile Ads, in accordance with the user's ad personalization settings on the device.

The user can disable ad personalization:
- **On the Android device:** Settings → Google → Ads → "Opt out of Ads Personalization" (or "Reset advertising ID")
- **Via Yandex settings:** https://yandex.com/adv/personalized_advertising

## 4. User rights

### 4.1 Permission management

The user has the right to:
- Stop using the App at any time
- Manage the advertising ID via device settings
- Disable ad personalization (see section 3.2)
- Uninstall the App, which will delete all local data

### 4.2 Access and deletion of data

- Local data (game state, settings): the user can delete it by uninstalling the App
- Data collected by SDKs: the user can request access/deletion from the respective services:
  - Yandex: https://yandex.com/support/privacy/requests.html
  - MyTracker: via VK's contact form

### 4.3 Rights under GDPR (for users in the EU) and the Russian Federal Law "On Personal Data"

Users have the right to:
- Access their personal data
- Rectify inaccurate data
- Erase data ("right to be forgotten")
- Restrict processing
- Data portability
- Withdraw consent

To exercise these rights regarding data collected by Yandex Mobile Ads and MyTracker, the user should contact the respective services directly (see section 4.2).

## 5. Data security

The developer applies reasonable technical and organizational measures to protect the user's local data. However, no method of transmission over the Internet or electronic storage is 100% secure, so we cannot guarantee absolute security.

## 6. Children

The App is **not intended** for children under 13 and is not addressed to them. The developer does not knowingly collect personal data from children. If you believe a child has provided us with personal data, please contact us (section 8) and we will delete such information.

## 7. Changes to the Privacy Policy

The developer may update this Privacy Policy at any time. The updated version will be published on this page with an updated effective date. We recommend periodically checking this page for changes.

## 8. Contacts

If you have questions about this Privacy Policy or data processing in the App, contact the developer:

- **Email:** nikita_avdonin@mail.ru
- **Subject:** "2048 Numbers — Privacy Policy"

---

## 9. Summary (TL;DR)

| What | Where stored | Who has access |
|---|---|---|
| Game state, records, settings | On device (SharedPreferences) | Only the user |
| Advertising ID, IP, device info | Transmitted to Yandex Mobile Ads | Yandex |
| In-app events, device info | Transmitted to MyTracker | VK / Mail.ru |
| Personal data (name, email, phone) | **Not collected** | — |
| Financial data | **Not collected** (billing removed in P0) | — |
| Geolocation | **Not collected** (only country/region by IP, for ad targeting) | Yandex, MyTracker |

The App does not request or use the `ACCESS_FINE_LOCATION`, `READ_CONTACTS`, `CAMERA`, `MICROPHONE`, `READ_EXTERNAL_STORAGE` or other sensitive permissions.

---

*This Privacy Policy is available in two languages: [Русский](PRIVACY_POLICY.md) | [English](PRIVACY_POLICY.en.md)*
